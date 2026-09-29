package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ipotracker.IpoApplication
import com.example.ipotracker.navigation.AppNavigation
import com.example.ipotracker.notification.IpoChangeDetectionManager
import com.example.ipotracker.notification.IpoNotificationManager
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private var changeDetector: IpoChangeDetectionManager? = null
    private var deepLinkDestination by mutableStateOf<String?>(null)
    private var deepLinkIpoId by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Permission granted or denied handled gracefully
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as IpoApplication).container

        // 1. Create native notification channels for New IPO, GMP, Allotment, Admin notices
        IpoNotificationManager.createChannels(this)

        // 2. Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 3. Start 5-minute periodic background change detection
        changeDetector = IpoChangeDetectionManager(
            context = applicationContext,
            repository = appContainer.ipoRepository
        ).apply {
            startMonitoring()
        }

        // 4. Handle incoming notification deep link
        handleNotificationIntent(intent)

        setContent {
            MyApplicationTheme {
                AppNavigation(
                    appContainer = appContainer,
                    changeDetector = changeDetector,
                    initialDestination = deepLinkDestination,
                    initialIpoId = deepLinkIpoId
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.let {
            deepLinkDestination = it.getStringExtra(IpoNotificationManager.EXTRA_DESTINATION)
            deepLinkIpoId = it.getStringExtra(IpoNotificationManager.EXTRA_IPO_ID)
        }
    }
}
