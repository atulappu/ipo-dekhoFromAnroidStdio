package com.example.ipotracker.notification

import android.content.Context
import android.util.Log
import com.example.ipotracker.data.model.AllotmentStatus
import com.example.ipotracker.data.model.IpoItem
import com.example.ipotracker.data.model.IpoStatus
import com.example.ipotracker.domain.repository.IpoRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class IpoChangeDetectionManager(
    private val context: Context,
    private val repository: IpoRepository
) {
    private val tag = "IpoChangeDetector"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private data class IpoSnapshot(
        val id: String,
        val name: String,
        val gmp: Double,
        val status: IpoStatus,
        val allotmentAvailable: Boolean,
        val priceBandMax: Double
    )

    private val previousSnapshots = mutableMapOf<String, IpoSnapshot>()
    private var isFirstRun = true
    private var isMonitoringActive = false

    companion object {
        const val CHECK_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes interval
    }

    fun startMonitoring() {
        if (isMonitoringActive) return
        isMonitoringActive = true

        scope.launch {
            Log.d(tag, "Starting 5-minute IPO & GMP change detection service...")
            while (isActive) {
                try {
                    checkAndNotifyDiffs()
                } catch (e: Exception) {
                    Log.e(tag, "Error during 5-minute diff check", e)
                }
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    suspend fun checkAndNotifyDiffs() = withContext(Dispatchers.IO) {
        // Fetch latest IPO list
        repository.refreshData()
        val currentIpos = repository.getAllIpos().first()

        if (currentIpos.isEmpty()) return@withContext

        if (isFirstRun) {
            // Seed initial state without spamming notifications on first cold boot
            currentIpos.forEach { ipo ->
                previousSnapshots[ipo.id] = ipo.toSnapshot()
            }
            isFirstRun = false
            Log.d(tag, "Initial snapshot populated with ${previousSnapshots.size} IPOs.")
            return@withContext
        }

        // 1. Detect New IPOs added
        currentIpos.forEach { currentIpo ->
            val prev = previousSnapshots[currentIpo.id]
            if (prev == null) {
                // Brand new IPO detected!
                val priceStr = "₹${currentIpo.priceBandMin.toInt()} - ₹${currentIpo.priceBandMax.toInt()}"
                val sizeStr = if (currentIpo.issueSizeCr > 0) "₹${currentIpo.issueSizeCr.toInt()} Cr" else "TBA"
                IpoNotificationManager.notifyNewIpo(
                    context = context,
                    ipoId = currentIpo.id,
                    ipoName = currentIpo.name,
                    priceBand = priceStr,
                    issueSize = sizeStr
                )
            } else {
                // 2. Detect GMP Changes
                if (currentIpo.currentGmp != prev.gmp && currentIpo.currentGmp > 0) {
                    val gainPercent = if (currentIpo.priceBandMax > 0) {
                        (currentIpo.currentGmp / currentIpo.priceBandMax) * 100.0
                    } else 0.0

                    IpoNotificationManager.notifyGmpChange(
                        context = context,
                        ipoId = currentIpo.id,
                        ipoName = currentIpo.name,
                        oldGmp = prev.gmp,
                        newGmp = currentIpo.currentGmp,
                        gainPercent = gainPercent
                    )
                }

                // 3. Detect Allotment Status Out
                val isNowAllotmentOut = currentIpo.allotmentStatus == AllotmentStatus.AVAILABLE ||
                        currentIpo.status == IpoStatus.ALLOTMENT_AVAILABLE ||
                        currentIpo.allotmentInfo?.isAvailable == true

                if (!prev.allotmentAvailable && isNowAllotmentOut) {
                    val registrar = currentIpo.allotmentInfo?.registrarName?.ifBlank { "Registrar" } ?: "Registrar"
                    IpoNotificationManager.notifyAllotmentOut(
                        context = context,
                        ipoId = currentIpo.id,
                        ipoName = currentIpo.name,
                        registrar = registrar
                    )
                }
            }

            // Update snapshot
            previousSnapshots[currentIpo.id] = currentIpo.toSnapshot()
        }
    }

    private fun IpoItem.toSnapshot(): IpoSnapshot {
        val isAllotmentOut = allotmentStatus == AllotmentStatus.AVAILABLE ||
                status == IpoStatus.ALLOTMENT_AVAILABLE ||
                allotmentInfo?.isAvailable == true
        return IpoSnapshot(
            id = id,
            name = name,
            gmp = currentGmp,
            status = status,
            allotmentAvailable = isAllotmentOut,
            priceBandMax = priceBandMax
        )
    }

    /**
     * Helper to test any notification type on demand
     */
    fun sendTestNotification(type: NotificationType) {
        when (type) {
            NotificationType.NEW_IPO -> {
                IpoNotificationManager.notifyNewIpo(
                    context = context,
                    ipoId = "test_ipo_1",
                    ipoName = "Solaris Clean Energy Tech IPO",
                    priceBand = "₹215 - ₹228",
                    issueSize = "₹850 Cr"
                )
            }
            NotificationType.GMP_CHANGE -> {
                IpoNotificationManager.notifyGmpChange(
                    context = context,
                    ipoId = "test_ipo_2",
                    ipoName = "TechInfra Global IPO",
                    oldGmp = 45.0,
                    newGmp = 72.0,
                    gainPercent = 48.0
                )
            }
            NotificationType.ALLOTMENT_OUT -> {
                IpoNotificationManager.notifyAllotmentOut(
                    context = context,
                    ipoId = "test_ipo_3",
                    ipoName = "Apex Robotics India IPO",
                    registrar = "Link Intime India"
                )
            }
            NotificationType.ADMIN_NOTICE -> {
                IpoNotificationManager.notifyAdminNotice(
                    context = context,
                    noticeId = "notice_${System.currentTimeMillis()}",
                    title = "📢 Admin Notice: SEBI T+3 Listing Rule",
                    message = "All mainboard IPO allotments will now be finalized within 2 working days. Check your verified registrar links in app."
                )
            }
        }
    }
}
