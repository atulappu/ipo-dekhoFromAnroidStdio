package com.example.ipotracker.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object IpoNotificationManager {

    const val CHANNEL_NEW_IPO = "channel_ipo_new"
    const val CHANNEL_GMP_CHANGES = "channel_ipo_gmp"
    const val CHANNEL_ALLOTMENT = "channel_ipo_allotment"
    const val CHANNEL_ADMIN_NOTICES = "channel_ipo_admin"

    const val EXTRA_DESTINATION = "extra_destination"
    const val EXTRA_IPO_ID = "extra_ipo_id"

    // In-Memory notification history & preferences
    private val _notifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                id = "notif_orient_allotment",
                title = "🎯 Allotment Out: Orient Cables (India) Ltd.",
                message = "Orient Cables (India) Ltd. allotment status is now available on Kfin Technologies Ltd. Tap to check your application!",
                type = NotificationType.ALLOTMENT_OUT,
                targetIpoId = "ipo-orient-cables",
                timestamp = System.currentTimeMillis() - 1800000,
                badgeText = "ALLOTMENT",
                eventKey = "ALLOTMENT_AVAILABLE:ipo-orient-cables"
            ),
            AppNotification(
                id = "init_1",
                title = "🎉 Notification System Active",
                message = "You will receive real-time alerts for New IPOs, GMP shifts, Allotment status, and Admin notices every 5 minutes.",
                type = NotificationType.ADMIN_NOTICE,
                timestamp = System.currentTimeMillis() - 3600000,
                badgeText = "System"
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Notification user preferences
    var isNewIpoAlertsEnabled: Boolean = true
    var isGmpAlertsEnabled: Boolean = true
    var isAllotmentAlertsEnabled: Boolean = true
    var isAdminNoticesEnabled: Boolean = true

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_NEW_IPO,
                    "New IPO Announcements",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts when a new IPO opens or is announced in the market"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_GMP_CHANGES,
                    "GMP Rate Movements",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Instant alerts when Grey Market Premium (GMP) increases or drops"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_ALLOTMENT,
                    "Allotment Status Released",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Urgent alerts when IPO registrar publishes allotment results"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_ADMIN_NOTICES,
                    "Admin & Market Announcements",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Important market notices, SEBI circulars, and admin updates"
                    enableVibration(true)
                }
            )

            channels.forEach { notificationManager.createNotificationChannel(it) }
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun notifyNewIpo(
        context: Context,
        ipoId: String,
        ipoName: String,
        priceBand: String,
        issueSize: String
    ) {
        if (!isNewIpoAlertsEnabled) return

        val title = "🆕 New IPO Announced: $ipoName"
        val message = "Price Band: $priceBand • Issue Size: $issueSize. Tap to view dates & subscription."

        recordNotification(
            AppNotification(
                title = title,
                message = message,
                type = NotificationType.NEW_IPO,
                targetIpoId = ipoId,
                badgeText = "NEW"
            )
        )

        dispatchSystemNotification(
            context = context,
            notificationId = (ipoId.hashCode() and 0x7FFFFFFF) + 100,
            channelId = CHANNEL_NEW_IPO,
            title = title,
            message = message,
            destination = "detail",
            targetIpoId = ipoId
        )
    }

    fun notifyGmpChange(
        context: Context,
        ipoId: String,
        ipoName: String,
        oldGmp: Double,
        newGmp: Double,
        gainPercent: Double
    ) {
        if (!isGmpAlertsEnabled) return

        val diff = newGmp - oldGmp
        val sign = if (diff >= 0) "📈 Jumped (+₹${diff.toInt()})" else "📉 Dropped (-₹${(-diff).toInt()})"
        val title = "$sign: $ipoName GMP"
        val message = "Current GMP is ₹${newGmp.toInt()} (${String.format("%.1f", gainPercent)}% expected gain). Tap to view trend."

        recordNotification(
            AppNotification(
                title = title,
                message = message,
                type = NotificationType.GMP_CHANGE,
                targetIpoId = ipoId,
                badgeText = if (diff >= 0) "+₹${diff.toInt()}" else "-₹${(-diff).toInt()}"
            )
        )

        dispatchSystemNotification(
            context = context,
            notificationId = (ipoId.hashCode() and 0x7FFFFFFF) + 200,
            channelId = CHANNEL_GMP_CHANGES,
            title = title,
            message = message,
            destination = "detail",
            targetIpoId = ipoId
        )
    }

    fun notifyAllotmentOut(
        context: Context,
        ipoId: String,
        ipoName: String,
        registrar: String,
        eventKey: String = "ALLOTMENT_AVAILABLE:$ipoId"
    ) {
        if (!isAllotmentAlertsEnabled) return

        // Idempotent guard: do not notify or duplicate if event was already dispatched
        if (_notifications.value.any { it.eventKey == eventKey }) {
            return
        }

        val title = "🎯 Allotment Out: $ipoName"
        val message = "$ipoName allotment status is now available on $registrar. Tap to check your application!"

        recordNotification(
            AppNotification(
                title = title,
                message = message,
                type = NotificationType.ALLOTMENT_OUT,
                targetIpoId = ipoId,
                badgeText = "ALLOTMENT",
                eventKey = eventKey
            )
        )

        dispatchSystemNotification(
            context = context,
            notificationId = (ipoId.hashCode() and 0x7FFFFFFF) + 300,
            channelId = CHANNEL_ALLOTMENT,
            title = title,
            message = message,
            destination = "allotment",
            targetIpoId = ipoId
        )
    }

    fun notifyAdminNotice(
        context: Context,
        noticeId: String,
        title: String,
        message: String
    ) {
        if (!isAdminNoticesEnabled) return

        recordNotification(
            AppNotification(
                id = noticeId,
                title = title,
                message = message,
                type = NotificationType.ADMIN_NOTICE,
                badgeText = "NOTICE"
            )
        )

        dispatchSystemNotification(
            context = context,
            notificationId = (noticeId.hashCode() and 0x7FFFFFFF) + 400,
            channelId = CHANNEL_ADMIN_NOTICES,
            title = title,
            message = message,
            destination = "notifications",
            targetIpoId = null
        )
    }

    private fun dispatchSystemNotification(
        context: Context,
        notificationId: Int,
        channelId: String,
        title: String,
        message: String,
        destination: String,
        targetIpoId: String?
    ) {
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_DESTINATION, destination)
            if (targetIpoId != null) {
                putExtra(EXTRA_IPO_ID, targetIpoId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission revoked or not granted
        }
    }

    private fun recordNotification(notification: AppNotification) {
        _notifications.update { current ->
            if (notification.eventKey != null && current.any { it.eventKey == notification.eventKey }) {
                current
            } else {
                listOf(notification) + current.take(49)
            }
        }
    }

    fun markAllAsRead() {
        _notifications.update { current ->
            current.map { it.copy(isRead = true) }
        }
    }

    fun clearHistory() {
        _notifications.value = emptyList()
    }
}
