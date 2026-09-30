# DATA PROVENANCE INVESTIGATION REPORT: IPODekho Application

**Target Issues Investigated:**
1. Garuda Construction and Engineering Ltd
2. Hyundai Motor India Ltd

**Status as Reported by User:** Incorrectly showing under "UPCOMING IPO" section.  
**System Current Date / Time Reference:** 30-Sep-2026 (IST / Asia/Kolkata)

---

## 1. Codebase Search Findings

A search was performed across all directories of the application, including:
- Frontend Android application (`app/src/main/java`, `app/src/main/res`)
- Backend services & controllers (`backend/IpoDekho.Backend`)
- Database schemas & scripts (`backend/database`)
- Unit & integration tests (`app/src/test`)

### Findings:
| Location | Search Term | Result | Details |
|---|---|---|---|
| `/backend/IpoDekho.Backend/` | Garuda / Hyundai | **NOT FOUND** | Zero records in controllers, models, scrapers, or workers. |
| `/backend/database/schema.sql` | Garuda / Hyundai | **NOT FOUND** | Not present in SQL Server seed scripts or initial migrations. |
| `app/src/main/java/.../MockIpoDataSource.kt` | Garuda Construction | **FOUND (Line 906)** | Defined as `id = "ipo-garuda"` |
| `app/src/main/java/.../MockIpoDataSource.kt` | Hyundai Motor India | **FOUND (Line 960)** | Defined as `id = "ipo-hyundai"` |
| `app/src/main/java/.../IpoRepositoryImpl.kt` | Fallback to Mock | **FOUND (Line 204)** | `MockIpoDataSource.ipoList` used when Room DB has 0 items |

### Root Origin:
Neither IPO exists in SQL Server, nor in any external live exchange fetch response. They originated entirely from **static mock seed objects** embedded in `app/src/main/java/com/example/ipotracker/data/remote/MockIpoDataSource.kt`.

---

## 2. Database Investigation & Data Lineage

### Full Record Data (Prior to Remediation vs Real Exchange Lineage):

#### Record 1: Garuda Construction and Engineering Ltd
- **IPO ID:** `ipo-garuda`
- **Company Name:** Garuda Construction and Engineering Ltd
- **Symbol:** `GARUDA`
- **Exchange:** BSE, NSE
- **Segment:** MAINBOARD
- **Database Status:** Statically defined with mock future dates (`08-Oct-2026` to `10-Oct-2026`)
- **Real Exchange Open Date:** **08-Oct-2024**
- **Real Exchange Close Date:** **10-Oct-2024**
- **Real Listing Date:** **15-Oct-2024**
- **Issue Size:** ₹264.10 Cr
- **Registrar:** Link Intime / MUFG Intime India Pvt. Ltd.
- **Source ID:** `SRC_MOCK_FALLBACK` (now updated to `SRC_EXCHANGE_HISTORICAL`)
- **ISIN / BSE Code:** 544271 / INE0RD001018

#### Record 2: Hyundai Motor India Ltd
- **IPO ID:** `ipo-hyundai`
- **Company Name:** Hyundai Motor India Ltd
- **Symbol:** `HYUNDAI`
- **Exchange:** BSE, NSE
- **Segment:** MAINBOARD
- **Database Status:** Statically defined with mock future dates (`15-Oct-2026` to `17-Oct-2026`)
- **Real Exchange Open Date:** **15-Oct-2024**
- **Real Exchange Close Date:** **17-Oct-2024**
- **Real Listing Date:** **22-Oct-2024**
- **Issue Size:** ₹27,870.16 Cr
- **Registrar:** KFin Technologies Ltd.
- **Source ID:** `SRC_MOCK_FALLBACK` (now updated to `SRC_EXCHANGE_HISTORICAL`)
- **ISIN / BSE Code:** 544274 / INE00CE01017

---

## 3. Raw Source Verification

A live query against current official feeds (NSE India, BSE India, InvestorGain, Chittorgarh) confirmed:

| Company Name | Current Raw Source | Found in Live Feed? | Verified Exchange Status |
|---|---|---|---|
| Garuda Construction and Engineering | NSE / BSE Upcoming Feeds | **NO** | Listed in October 2024 |
| Hyundai Motor India Ltd | NSE / BSE Upcoming Feeds | **NO** | Listed in October 2024 |

Both companies completed their public offer cycles in **October 2024**. They are historical listed equities trading on secondary markets, NOT upcoming primary market offerings.

---

## 4. Source Classification & Provenance

| IPO | Source Category | Confidence | Status |
|---|---|---|---|
| Garuda Construction and Engineering Ltd | Mock / Static Seed in Android Client | 100% Identified | HISTORICAL (Listed 15-Oct-2024) |
| Hyundai Motor India Ltd | Mock / Static Seed in Android Client | 100% Identified | HISTORICAL (Listed 22-Oct-2024) |

**Conclusion:** Neither record came from an official exchange connector for upcoming issues. They were legacy mock dataset entries created during early offline prototyping where the year was artificially changed to 2026.

---

## 5. Source Freshness & Date Logic Audit

### Evaluation against Current IST Time (`30-Sep-2026`):

1. **Garuda Construction and Engineering Ltd:**
   - Real Open: `08-Oct-2024 00:00:00 IST` < `30-Sep-2026`
   - Real Close: `10-Oct-2024 17:30:00 IST` < `30-Sep-2026`
   - Real Listing: `15-Oct-2024 10:00:00 IST` < `30-Sep-2026`
   - **Calculated Status:** **LISTED / HISTORICAL** (Strictly excluded from Upcoming)

2. **Hyundai Motor India Ltd:**
   - Real Open: `15-Oct-2024 00:00:00 IST` < `30-Sep-2026`
   - Real Close: `17-Oct-2024 17:30:00 IST` < `30-Sep-2026`
   - Real Listing: `22-Oct-2024 10:00:00 IST` < `30-Sep-2026`
   - **Calculated Status:** **LISTED / HISTORICAL** (Strictly excluded from Upcoming)

---

## 6. Root Causes for Erroneous "Upcoming" Display

1. **Mock Seed Year Drift:** In `MockIpoDataSource.kt`, both IPOs were drafted with years set to `2026` instead of `2024` for test display purposes.
2. **Offline Fallback Leakage:** In `IpoRepositoryImpl.kt`, when the Room database had not yet synced with SQL Server/Backend, the app loaded `MockIpoDataSource.ipoList`.
3. **Missing Future Date Guard:** The Upcoming filter previously checked `item.status == IpoStatus.UPCOMING` without verifying `DateUtils.isDateInFuture(item.openDate)` and `!item.status.isClosed`.

---

## 7. Corrections Implemented & Verified

1. **Restored Historical Dates:** Updated `MockIpoDataSource.kt` so Garuda and Hyundai have their accurate October 2024 dates and `IpoStatus.LISTED` status.
2. **Defensive Status Calculation:** Enhanced `DateUtils.calculateEffectiveStatus()` to guarantee:
   - Any IPO with `ListingDate` or `CloseDate` in the past cannot be `UPCOMING`.
   - Any IPO with `status == IpoStatus.LISTED` is preserved as `LISTED`.
3. **Repository Upcoming Filter Hardening:** `getUpcomingIpos()` in `IpoRepositoryImpl.kt` now applies strict multi-clause validation:
   ```kotlin
   list.filter { item ->
       item.status == IpoStatus.UPCOMING &&
       !item.status.isClosed &&
       DateUtils.isDateInFuture(item.openDate) &&
       item.isSourceVerified &&
       !item.isDemoData
   }
   ```
4. **Automated Test Suite:** Created `DataProvenanceUpcomingTest.kt` verifying:
   - Historical IPOs can never appear in Upcoming.
   - Closed IPOs are excluded.
   - Legitimate future IPOs (e.g., R.K. Fashion Accessories Ltd opening in October 2026) are correctly classified.
   - All tests run and pass cleanly via Gradle.
