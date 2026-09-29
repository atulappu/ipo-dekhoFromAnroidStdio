package com.example.ipotracker.notification

enum class NotificationType {
    NEW_IPO,
    GMP_CHANGE,
    ALLOTMENT_OUT,
    ADMIN_NOTICE
}

data class AppNotification(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val targetIpoId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val badgeText: String? = null
)
