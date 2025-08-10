package com.example.appusageoverlay

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class UsageMonitorService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var store: AppLimitStore
    private lateinit var overlayManager: OverlayManager
    private var currentApp: String? = null
    private var isMonitoring = false

    override fun onCreate() {
        super.onCreate()
        try {
            store = AppLimitStore(this)
            overlayManager = OverlayManager(this)
            startForegroundService()
            startMonitoring()
        } catch (e: Exception) {
            // Log error and stop service if initialization fails
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        isMonitoring = false
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // If service is killed, restart it
        return START_STICKY
    }

    private fun startForegroundService() {
        try {
            val channelId = "UsageMonitorServiceChannel"
            val channelName = "App Usage Monitor"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val chan = NotificationChannel(
                    channelId, 
                    channelName, 
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Monitors app usage and enforces time limits"
                }
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(chan)
            }
            
            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.service_notification_title))
                .setContentText(getString(R.string.service_notification_text))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build()
                
            startForeground(1, notification)
        } catch (e: Exception) {
            // Handle notification creation error
        }
    }

    private fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        serviceScope.launch {
            while (isMonitoring && isActive) {
                try {
                    val foregroundApp = getForegroundApp()
                    if (foregroundApp != null) {
                        val limits = store.loadLimits()
                        val appLimit = limits.find { it.packageName == foregroundApp }
                        
                        if (appLimit != null) {
                            if (currentApp == foregroundApp) {
                                appLimit.usedMinutesToday++
                                store.saveLimits(limits)
                            } else {
                                currentApp = foregroundApp
                            }
                            
                            if (appLimit.usedMinutesToday >= appLimit.timeLimitMinutes) {
                                try {
                                    overlayManager.showOverlay(appLimit.appName)
                                } catch (e: Exception) {
                                    // Handle overlay error
                                }
                            }
                        }
                    }
                    delay(TimeUnit.MINUTES.toMillis(1))
                } catch (e: Exception) {
                    // Log error but continue monitoring
                    delay(TimeUnit.MINUTES.toMillis(1))
                }
            }
        }
    }

    private fun getForegroundApp(): String? {
        return try {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 10000
            val events = usageStatsManager.queryEvents(beginTime, endTime)
            var lastUsedApp: String? = null
            val event = UsageEvents.Event()
            
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    lastUsedApp = event.packageName
                }
            }
            lastUsedApp
        } catch (e: Exception) {
            null
        }
    }
}
