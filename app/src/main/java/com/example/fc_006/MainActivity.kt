package com.example.fc_006

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var notificationId = 2000

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, R.string.notification_permission_denied, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Avoid enableEdgeToEdge + manual insets here: on some API 36 emulator builds they can
        // interact badly with system bars and leave the content area empty (black screen).
        setContentView(R.layout.activity_main)

        MissionControlNotifications.createChannels(this)
        requestNotificationPermissionIfNeeded()

        findViewById<MaterialButton>(R.id.btn_scan_complete).setOnClickListener {
            if (!canPostNotifications()) return@setOnClickListener
            scheduleDelayedScanComplete()
        }

        findViewById<MaterialButton>(R.id.btn_incoming_signal).setOnClickListener {
            if (!canPostNotifications()) return@setOnClickListener
            scheduleIncomingSignal()
        }

        findViewById<MaterialButton>(R.id.btn_random_event).setOnClickListener {
            if (!canPostNotifications()) return@setOnClickListener
            postRandomAsteroidEvent()
        }

        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_FROM_NOTIFICATION, false) != true) return
        val kindName = intent.getStringExtra(EXTRA_ALERT_KIND) ?: return
        val kind = runCatching { AlertKind.valueOf(kindName) }.getOrNull() ?: return

        val summaryRes = when (kind) {
            AlertKind.SCAN_COMPLETE -> R.string.status_from_scan_title
            AlertKind.INCOMING_SIGNAL -> R.string.status_from_signal_title
            AlertKind.HAZARD -> R.string.status_from_hazard_title
            AlertKind.EVADED -> R.string.status_from_evaded_title
        }
        val detailRes = when (kind) {
            AlertKind.SCAN_COMPLETE -> R.string.status_from_scan_detail
            AlertKind.INCOMING_SIGNAL -> R.string.status_from_signal_detail
            AlertKind.HAZARD -> R.string.status_from_hazard_detail
            AlertKind.EVADED -> R.string.status_from_evaded_detail
        }
        findViewById<TextView>(R.id.text_status_summary).setText(summaryRes)
        findViewById<TextView>(R.id.text_status_detail).setText(detailRes)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> { }

                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            Toast.makeText(this, R.string.notifications_disabled, Toast.LENGTH_SHORT).show()
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, R.string.notification_permission_required, Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return false
            }
        }
        return true
    }

    /** Trigger 1: button + delay — emulates scan finishing (informational). */
    private fun scheduleDelayedScanComplete() {
        handler.postDelayed({
            MissionControlNotifications.show(
                this,
                nextNotificationId(),
                MissionControlNotifications.CHANNEL_INFORMATIONAL,
                getString(R.string.notif_scan_complete_title),
                getString(R.string.notif_scan_complete_message),
                R.drawable.ic_stat_informational,
                AlertKind.SCAN_COMPLETE
            )
            Toast.makeText(this, R.string.toast_notification_posted, Toast.LENGTH_LONG).show()
        }, 5_000L)
    }

    /** Trigger 2: timer-style delay — emulates external signal (warning). */
    private fun scheduleIncomingSignal() {
        handler.postDelayed({
            MissionControlNotifications.show(
                this,
                nextNotificationId(),
                MissionControlNotifications.CHANNEL_WARNING,
                getString(R.string.notif_signal_title),
                getString(R.string.notif_signal_message),
                R.drawable.ic_stat_warning,
                AlertKind.INCOMING_SIGNAL
            )
            Toast.makeText(this, R.string.toast_notification_posted, Toast.LENGTH_LONG).show()
        }, 3_000L)
    }

    /** Trigger 3: short delay + random outcome — hazard (warning) or evaded (Task 6). */
    private fun postRandomAsteroidEvent() {
        handler.postDelayed({
            val evaded = Random.nextBoolean()
            if (evaded) {
                MissionControlNotifications.show(
                    this,
                    nextNotificationId(),
                    MissionControlNotifications.CHANNEL_EVADED,
                    getString(R.string.notif_evaded_title),
                    getString(R.string.notif_evaded_message),
                    R.drawable.ic_stat_evaded,
                    AlertKind.EVADED
                )
            } else {
                MissionControlNotifications.show(
                    this,
                    nextNotificationId(),
                    MissionControlNotifications.CHANNEL_WARNING,
                    getString(R.string.notif_hazard_title),
                    getString(R.string.notif_hazard_message),
                    R.drawable.ic_stat_warning,
                    AlertKind.HAZARD
                )
            }
            Toast.makeText(this, R.string.toast_notification_posted, Toast.LENGTH_LONG).show()
        }, 2_000L)
    }

    private fun nextNotificationId(): Int = notificationId++

    companion object {
        const val EXTRA_FROM_NOTIFICATION = "com.example.fc_006.EXTRA_FROM_NOTIFICATION"
        const val EXTRA_ALERT_KIND = "com.example.fc_006.EXTRA_ALERT_KIND"
    }
}
