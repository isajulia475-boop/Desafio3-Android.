package com.example.desafio3

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SincronizacaoService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "canal_sincronizacao"
    }

    private lateinit var notificationManager: NotificationManager

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var contadorPacotes = 0
    private var sincronizacaoJob: Job? = null

    override fun onCreate() {
        super.onCreate()

        notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        criarCanalNotificacao()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (sincronizacaoJob?.isActive == true) {
            return START_STICKY
        }

        contadorPacotes = 0

        val notificacao = criarNotificacao()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notificacao,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(
                NOTIFICATION_ID,
                notificacao
            )
        }

        sincronizacaoJob = serviceScope.launch {

            while (isActive) {

                delay(5000)

                contadorPacotes++

                val novaNotificacao = criarNotificacao()

                notificationManager.notify(
                    NOTIFICATION_ID,
                    novaNotificacao
                )
            }
        }

        return START_STICKY
    }

    private fun criarCanalNotificacao() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val canal = NotificationChannel(
                CHANNEL_ID,
                "Sincronização",
                NotificationManager.IMPORTANCE_LOW
            )

            canal.description =
                "Notificações do serviço de sincronização"

            notificationManager.createNotificationChannel(canal)
        }
    }

    private fun criarNotificacao(): Notification {

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Sincronização em andamento")
            .setContentText(
                "Pacotes processados: $contadorPacotes"
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {

        sincronizacaoJob?.cancel()
        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
