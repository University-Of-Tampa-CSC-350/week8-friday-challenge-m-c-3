package com.example.fc_006

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                R.drawable.ic_stat_informational
            )
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
                R.drawable.ic_stat_warning
            )
        }, 3_000L)
    }

    /** Trigger 3: random outcome — hazard (warning) or evaded (resolved). */
    private fun postRandomAsteroidEvent() {
        val evaded = Random.nextBoolean()
        if (evaded) {
            MissionControlNotifications.show(
                this,
                nextNotificationId(),
                MissionControlNotifications.CHANNEL_EVADED,
                getString(R.string.notif_evaded_title),
                getString(R.string.notif_evaded_message),
                R.drawable.ic_stat_evaded
            )
        } else {
            MissionControlNotifications.show(
                this,
                nextNotificationId(),
                MissionControlNotifications.CHANNEL_WARNING,
                getString(R.string.notif_hazard_title),
                getString(R.string.notif_hazard_message),
                R.drawable.ic_stat_warning
            )
        }
    }

    private fun nextNotificationId(): Int = notificationId++
}
