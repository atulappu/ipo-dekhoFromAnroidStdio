# Implementation Plan: Real-Time Alerts, 5-Minute Change Detection & Automated Ingestion

This plan implements real-time push notifications and automated change detection across Android and ASP.NET Core for:
1. **New IPO Announced / Added**
2. **GMP Rate Changed** (e.g. "GMP jumped to ₹150 (+25%) for Bajaj Housing")
3. **Allotment Status Released** (e.g. "Allotment is OUT for Tata Tech! Check now")
4. **Admin Broadcast Announcements & Special Notices**

---

## 1. System Architecture

```
┌────────────────────────────────────────────────────────┐
│  AUTOMATED BACKGROUND WORKER (ASP.NET Core & SQL Server)│
│                                                        │
│  - Runs automatically every 5-15 minutes               │
│  - Compares previous snapshot vs latest data:          │
│    • Is there a new IPO record?                        │
│    • Did CurrentGmp change? (Old GMP vs New GMP)       │
│    • Did Allotment change from PENDING to AVAILABLE?   │
│    • Is there a new Admin Announcement?                │
│  - Automatically updates SQL Server (`IpoDekhoDb`)     │
│  - Broadcasts notification payload to connected clients│
└───────────────────────────┬────────────────────────────┘
                            │ Real-time Push / Sync
                            ▼
┌────────────────────────────────────────────────────────┐
│  ANDROID CLIENT NOTIFICATION ENGINE (Kotlin & Compose) │
│                                                        │
│  - 4 Dedicated Android Notification Channels:          │
│    1. `channel_new_ipo` ("New IPO Alerts")             │
│    2. `channel_gmp_updates` ("GMP Price Alerts")       │
│    3. `channel_allotment` ("Allotment Status Alerts")  │
│    4. `channel_admin_notices` ("Admin Announcements")  │
│                                                        │
│  - Background Sync Worker (Diff Detector & Local Push) │
│  - Tap-to-Navigate: Clicking notification opens that   │
│    exact IPO's detail or allotment screen              │
│  - In-App Notification Center: View all past alerts    │
│  - User Alert Preferences: Toggle individual channels  │
│  - Android 13+ POST_NOTIFICATIONS runtime permission   │
└────────────────────────────────────────────────────────┘
```

---

## 2. Android App Implementation Details

### Step 1: Notification Channels & Permission
- Declare `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />` in `AndroidManifest.xml`.
- Create `IpoNotificationManager.kt` defining the 4 channels with high priority, system vibration, and sound.
- Request runtime permission cleanly in Compose using standard Android 13+ contracts.

### Step 2: Change Detection Engine (`IpoAlertSyncWorker.kt`)
- Stores last-seen state in DataStore / Room cache.
- Compares fresh API data every 5 minutes:
  - **New IPO:** Alerts when an IPO ID wasn't present in previous sync.
  - **GMP Change:** Alerts if `abs(newGmp - oldGmp) > 0`, highlighting direction (`🟢 +₹25` or `🔴 -₹10`).
  - **Allotment Out:** Alerts when status transitions to `ALLOTMENT_AVAILABLE`.
  - **Admin Notice:** Alerts when a new announcement ID is fetched.

### Step 3: Interactive Notification Tap & Deep Links
- PendingIntent configured so tapping an alert opens the app directly at:
  - IPO Detail screen for that specific company.
  - Allotment check screen with registrar link preloaded.
  - In-App Notification Center for Admin Notices.

### Step 4: Notification Center & Preferences UI
- Settings/Notification screen where users can toggle:
  - [x] New IPO Alerts
  - [x] GMP Price Movements
  - [x] Allotment Out Notifications
  - [x] Admin & Market Notices
- Notification History list showing recent notifications with timestamps and read/unread status.

---

## 3. ASP.NET Core Automated Background Ingestion

- Background worker `IpoDataIngestionWorker.cs`:
  - Fetches exchange data automatically.
  - Inserts/updates `IpoDekhoDb` tables without manual entry.
  - Provides `/api/v1/Admin/notices` and `/api/v1/Admin/broadcast` endpoints.

---

## 4. Verification & Testing
- Unit & Robolectric testing for diff detection logic.
- Test notification triggers directly inside the app with a developer "Test Notification" action to verify sound, icon, and deep link navigation immediately.
- Compile and verify build with `compile_applet`.
