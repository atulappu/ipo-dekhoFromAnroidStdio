# IPODekho Backend - ASP.NET Core 8 & Microsoft SQL Server

Production-grade REST API backend built in **C# (.NET 8)** and **Microsoft SQL Server** to supply live Indian IPO listings, Grey Market Premiums (GMP), day-wise subscription data, and market indices to the **IPODekho** Android application.

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **.NET 8 SDK** ([Download .NET 8](https://dotnet.microsoft.com/download/dotnet/8.0))
- **Microsoft SQL Server** (2019, 2022, or Azure SQL Database) OR **Docker**
- **Visual Studio 2022** / **VS Code** / **Rider**

### 2. Start Microsoft SQL Server (via Docker or Local Instance)
If using Docker, run this command:
```bash
docker run -e "ACCEPT_EULA=Y" \
           -e "MSSQL_SA_PASSWORD=YourStrong@Password123" \
           -p 1433:1433 \
           --name ipodekho-sql \
           -d mcr.microsoft.com/mssql/server:2022-latest
```

Or open `Database/schema.sql` in **SQL Server Management Studio (SSMS)** or **Azure Data Studio** and execute it on your local server.

### 3. Configure Connection String
In `appsettings.json`:
```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost,1433;Database=IpoDekhoDb;User Id=sa;Password=YourStrong@Password123;TrustServerCertificate=True;MultipleActiveResultSets=True;"
  }
}
```

### 4. Run the Web API
In terminal inside `backend-aspnetcore/`:
```bash
dotnet restore
dotnet run
```
By default, the server listens at:
- HTTP: `http://localhost:5000`
- HTTPS: `https://localhost:5001`
- Interactive Swagger UI: `http://localhost:5000/`

---

## 📱 Connecting with the IPODekho Android App

When running the Android app on an Android Emulator, `10.0.2.2` automatically routes to your host machine's `localhost`:

1. Open `IpoConfig.kt` in the Android app.
2. Set:
```kotlin
IpoConfig.DATA_SOURCE = DataSourceType.API
IpoConfig.API_BASE_URL = "http://10.0.2.2:5000/api/v1/"
```
3. Re-run the Android app. It will fetch live data directly from your ASP.NET Core backend!

---

## 📡 REST API Endpoints Specification

| Method | Endpoint | Description | Query Parameters |
|---|---|---|---|
| `GET` | `/api/v1/ipos` | Get filtered IPO list | `status` (OPEN, UPCOMING, CLOSED), `category` (MAINBOARD, SME), `q` (search) |
| `GET` | `/api/v1/ipos/{id}` | Get complete IPO details | `id` (e.g. `ipo_swiggy_2024`) |
| `GET` | `/api/v1/ipos/{id}/subscription` | Day-by-day bidding subscription details | `id` |
| `GET` | `/api/v1/ipos/{id}/gmp-history` | Grey Market Premium history list | `id` |
| `GET` | `/api/v1/ipos/search` | Search IPOs by company name or ticker symbol | `q` (search query) |
| `GET` | `/api/v1/market/indices` | Live benchmark indices (Nifty 50, Sensex, etc.) | None |

---

## ⚙️ Automated Scraper Worker (`IpoSyncBackgroundWorker`)
The background service runs automatically inside the ASP.NET Core host:
- Executes periodic sync (every 30 mins configurable in `appsettings.json`)
- Parses NSE/BSE updates and Grey Market Premium tables
- Updates the `Ipos`, `Subscriptions`, and `GmpHistories` tables with clean transaction rollbacks.
