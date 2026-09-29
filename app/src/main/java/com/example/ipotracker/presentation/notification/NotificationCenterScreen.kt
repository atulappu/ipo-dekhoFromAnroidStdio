package com.example.ipotracker.presentation.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ipotracker.notification.AppNotification
import com.example.ipotracker.notification.IpoChangeDetectionManager
import com.example.ipotracker.notification.IpoNotificationManager
import com.example.ipotracker.notification.NotificationType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAllotment: (String?) -> Unit,
    changeDetector: IpoChangeDetectionManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notifications by IpoNotificationManager.notifications.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf<NotificationType?>(null) }
    var showPreferences by remember { mutableStateOf(false) }

    var prefNewIpo by remember { mutableStateOf(IpoNotificationManager.isNewIpoAlertsEnabled) }
    var prefGmp by remember { mutableStateOf(IpoNotificationManager.isGmpAlertsEnabled) }
    var prefAllotment by remember { mutableStateOf(IpoNotificationManager.isAllotmentAlertsEnabled) }
    var prefAdmin by remember { mutableStateOf(IpoNotificationManager.isAdminNoticesEnabled) }

    val filteredList = remember(notifications, selectedFilter) {
        if (selectedFilter == null) notifications
        else notifications.filter { it.type == selectedFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Notification Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Real-time 5-min market & GMP alerts",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPreferences = !showPreferences }) {
                        Icon(
                            if (showPreferences) Icons.Filled.Tune else Icons.Outlined.Tune,
                            contentDescription = "Preferences",
                            tint = if (showPreferences) PrimaryOrange else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { IpoNotificationManager.clearHistory() }) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = "Clear All", tint = NeutralGray)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Expandable Preferences Sheet
            if (showPreferences) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Notification Channels",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        PreferenceToggleRow(
                            label = "🆕 New IPO Announcements",
                            checked = prefNewIpo,
                            onCheckedChange = {
                                prefNewIpo = it
                                IpoNotificationManager.isNewIpoAlertsEnabled = it
                            }
                        )
                        PreferenceToggleRow(
                            label = "📈 GMP Rate Movement Alerts",
                            checked = prefGmp,
                            onCheckedChange = {
                                prefGmp = it
                                IpoNotificationManager.isGmpAlertsEnabled = it
                            }
                        )
                        PreferenceToggleRow(
                            label = "🎯 Allotment Status Out Alerts",
                            checked = prefAllotment,
                            onCheckedChange = {
                                prefAllotment = it
                                IpoNotificationManager.isAllotmentAlertsEnabled = it
                            }
                        )
                        PreferenceToggleRow(
                            label = "📢 Admin Notices & Market News",
                            checked = prefAdmin,
                            onCheckedChange = {
                                prefAdmin = it
                                IpoNotificationManager.isAdminNoticesEnabled = it
                            }
                        )
                    }
                }
            }

            // Test Alert Bar (Allows immediate verification in emulator)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                color = PrimaryOrangeLight,
                border = BorderStroke(1.dp, PrimaryOrangeContainer)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Instant Test Alerts (Try Now)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryOrangeDark
                        )
                        Text(
                            text = "Auto-checks every 5 min",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = NeutralGray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TestPillButton(
                            text = "+ New IPO",
                            color = MarketGreen,
                            onClick = { changeDetector?.sendTestNotification(NotificationType.NEW_IPO) },
                            modifier = Modifier.weight(1f)
                        )
                        TestPillButton(
                            text = "+ GMP Jump",
                            color = PrimaryOrange,
                            onClick = { changeDetector?.sendTestNotification(NotificationType.GMP_CHANGE) },
                            modifier = Modifier.weight(1f)
                        )
                        TestPillButton(
                            text = "+ Allotment",
                            color = SecondaryBlue,
                            onClick = { changeDetector?.sendTestNotification(NotificationType.ALLOTMENT_OUT) },
                            modifier = Modifier.weight(1f)
                        )
                        TestPillButton(
                            text = "+ Notice",
                            color = NeutralGray,
                            onClick = { changeDetector?.sendTestNotification(NotificationType.ADMIN_NOTICE) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text("All (${notifications.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == NotificationType.GMP_CHANGE,
                    onClick = { selectedFilter = if (selectedFilter == NotificationType.GMP_CHANGE) null else NotificationType.GMP_CHANGE },
                    label = { Text("📈 GMP", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == NotificationType.NEW_IPO,
                    onClick = { selectedFilter = if (selectedFilter == NotificationType.NEW_IPO) null else NotificationType.NEW_IPO },
                    label = { Text("🆕 New IPO", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedFilter == NotificationType.ALLOTMENT_OUT,
                    onClick = { selectedFilter = if (selectedFilter == NotificationType.ALLOTMENT_OUT) null else NotificationType.ALLOTMENT_OUT },
                    label = { Text("🎯 Allotment", fontSize = 11.sp) }
                )
            }

            // Notification List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = NeutralGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Notifications Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You'll be alerted whenever a new IPO is added, GMP updates, or allotment status is published.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        NotificationItemCard(
                            notification = item,
                            onClick = {
                                when (item.type) {
                                    NotificationType.NEW_IPO, NotificationType.GMP_CHANGE -> {
                                        item.targetIpoId?.let { onNavigateToDetail(it) }
                                    }
                                    NotificationType.ALLOTMENT_OUT -> {
                                        onNavigateToAllotment(item.targetIpoId)
                                    }
                                    NotificationType.ADMIN_NOTICE -> {
                                        // Stay in center
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryOrange, checkedTrackColor = PrimaryOrangeContainer)
        )
    }
}

@Composable
private fun TestPillButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp, fontWeight = FontWeight.Bold),
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val badgeColor = when (notification.type) {
        NotificationType.NEW_IPO -> MarketGreen
        NotificationType.GMP_CHANGE -> PrimaryOrange
        NotificationType.ALLOTMENT_OUT -> SecondaryBlue
        NotificationType.ADMIN_NOTICE -> Color(0xFF673AB7)
    }

    val icon = when (notification.type) {
        NotificationType.NEW_IPO -> Icons.Filled.FiberNew
        NotificationType.GMP_CHANGE -> Icons.Filled.TrendingUp
        NotificationType.ALLOTMENT_OUT -> Icons.Filled.AssignmentTurnedIn
        NotificationType.ADMIN_NOTICE -> Icons.Filled.Campaign
    }

    val timeFormatted = remember(notification.timestamp) {
        SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(notification.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    notification.badgeText?.let { badge ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = NeutralGray
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
