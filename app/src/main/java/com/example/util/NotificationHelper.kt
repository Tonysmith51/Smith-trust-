package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.text.NumberFormat
import java.util.Locale

object NotificationHelper {
    const val CHANNEL_SOVEREIGN_ALERTS = "smith_trust_sovereign_alerts"
    const val CHANNEL_FLEET_MAINTENANCE = "smith_trust_fleet_maintenance"
    const val CHANNEL_CARD_ALERTS = "smith_trust_card_alerts"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alertsChannel = NotificationChannel(
                CHANNEL_SOVEREIGN_ALERTS,
                "Sovereign Treasury & Sweep Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority alerts for interest sweeps, surplus distributions, and vault milestones."
                enableVibration(true)
            }

            val fleetChannel = NotificationChannel(
                CHANNEL_FLEET_MAINTENANCE,
                "Fleet & Aviation Telemetry",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Maintenance schedule reminders, annual flight checks, and marina notices."
            }

            val cardChannel = NotificationChannel(
                CHANNEL_CARD_ALERTS,
                "Family Sapphire Cards & Gifting",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Authorized family card utilization and minor custodial escrow notices."
            }

            notificationManager.createNotificationChannels(listOf(alertsChannel, fleetChannel, cardChannel))
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun sendNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        bigText: String? = null
    ): Boolean {
        initNotificationChannels(context)

        if (!hasNotificationPermission(context)) {
            return false
        }

        try {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(getPendingIntent(context))
                .setAutoCancel(true)

            if (!bigText.isNullOrBlank()) {
                builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            }

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun sendSweepNotification(context: Context, amount: Double, accountName: String): Boolean {
        val fmt = NumberFormat.getCurrencyInstance(Locale.US).format(amount)
        return sendNotification(
            context = context,
            channelId = CHANNEL_SOVEREIGN_ALERTS,
            notificationId = 1001,
            title = "Treasury Sweep Completed",
            message = "$fmt credited to $accountName.",
            bigText = "Anthony Smith Master Trust automated treasury sweep of $fmt has successfully cleared and been allocated to $accountName."
        )
    }

    fun sendMaintenanceDueNotification(context: Context, assetName: String, task: String, dueDate: String): Boolean {
        return sendNotification(
            context = context,
            channelId = CHANNEL_FLEET_MAINTENANCE,
            notificationId = 2001,
            title = "Fleet Maintenance Due: $assetName",
            message = "$task scheduled for $dueDate.",
            bigText = "$assetName has an upcoming certified service requirement ($task) scheduled on $dueDate. Station crew has been alerted."
        )
    }

    fun sendCardSpendNotification(context: Context, cardholder: String, spend: Double, remaining: Double): Boolean {
        val fmtSpend = NumberFormat.getCurrencyInstance(Locale.US).format(spend)
        val fmtRem = NumberFormat.getCurrencyInstance(Locale.US).format(remaining)
        return sendNotification(
            context = context,
            channelId = CHANNEL_CARD_ALERTS,
            notificationId = 3001,
            title = "Chase Sapphire Reserve® Alert: $cardholder",
            message = "YTD Spend: $fmtSpend • Available: $fmtRem.",
            bigText = "$cardholder's Chase Sapphire Reserve® card has reached $fmtSpend YTD spend. Remaining annual gift exclusion capacity is $fmtRem."
        )
    }
}
