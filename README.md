# IPODekho - Real-Time Indian IPO Tracker & Live GMP Platform

A complete Full-Stack solution featuring an **Android App (Kotlin + Jetpack Compose)** and **ASP.NET Core Web API (C# + SQL Server)** with automated 5-minute scraping and push notification diff detection.

---

## 📱 1. Android Application (`/app`)
- **Tech Stack:** Kotlin 2.2, Jetpack Compose (Material 3), Coroutines, Room DB, Retrofit & Moshi, Gemini AI.
- **Key Features:**
  - **Real-Time Push Notifications (4 Distinct Channels):**
    - 🆕 `channel_ipo_new`: New IPO Announcements
    - 📈 `channel_ipo_gmp`: Instant alerts on GMP rate movements & % gain
    - 🎯 `channel_ipo_allotment`: Out announcement & registrar check links
    - 📢 `channel_ipo_admin`: Admin announcements & SEBI notices
  - **In-App Notification Center:** Channel switches, history viewer, and instant emulator test buttons.
  - **One-Tap Deep Linking:** Tapping an alert opens that specific IPO detail or allotment screen.
  - **Smart Pull-to-Refresh:** Clean swipe down to fetch latest prices from SQL Server without screen banners.

---

## 💻 2. Web Backend (`/backend/IpoDekho.Backend`)
- **Tech Stack:** ASP.NET Core 8.0 Web API, Entity Framework Core, Microsoft SQL Server.
- **Automated Ingestion Worker:** `IpoDataIngestionWorker.cs` runs every 5 minutes in background to fetch exchange feeds & GMP aggregators, auto-updating SQL Server without manual entry.
- **Database Schema:** Located in `/backend/database/schema.sql` (Tables: `Ipos`, `GmpHistory`, `Subscriptions`, `AdminNotices`).

---

## 🚀 How to Run

### Running Backend:
```bash
cd backend/IpoDekho.Backend
dotnet restore
dotnet run
```
Swagger UI will be available at: `http://localhost:5000/swagger`

### Running Android App:
Open the root directory in Android Studio, connect your emulator/device, and click **Run (▶)**.
