package com.example.idlegame

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class CoinGenerationService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var generationJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "coin_generation_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.example.idlegame.ACTION_START"
        const val ACTION_STOP = "com.example.idlegame.ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                // サービスを開始
                startForegroundService()
                startCoinGeneration()
                return START_STICKY
            }
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🪙 コインクリッカー")
            .setContentText("バックグラウンドでコイン生成中...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "コイン生成",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "バックグラウンドでのコイン生成通知"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startCoinGeneration() {
        if (generationJob?.isActive == true) return

        generationJob = serviceScope.launch {
            while (isActive) {
                try {
                    delay(1000)
                    // GameViewModel への通知をブロードキャストで行う
                    val intent = Intent(ACTION_COIN_GENERATION)
                    sendBroadcast(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        generationJob?.cancel()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

// グローバル定数の定義
const val ACTION_COIN_GENERATION = "com.example.idlegame.ACTION_COIN_GENERATION"
