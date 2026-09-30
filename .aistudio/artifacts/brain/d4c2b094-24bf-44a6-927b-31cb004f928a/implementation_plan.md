# Implementation Plan: Live Exchange & InvestorGain Synchronization + Build Error Fix

This plan addresses all specific points and screenshots provided by the user:
1. **Accurate Data Ingestion**: Updating the app's IPO and GMP records with the exact real-time data visible in your NSE, BSE, and InvestorGain screenshots (dated 30-Sep-2026).
2. **URL Corrections**: Replacing expired/404 URLs with the verified working URLs you confirmed in your browser.
3. **KSP Build Error Fix**: Resolving the `java.lang.NullPointerException` in KSP during build execution.

---

## 1. Root Cause Analysis of User's Findings

### A. Why did the app show different data earlier?
- **Root Cause**: The application previously had seed data from an earlier test period that included closed/historical IPOs (like Diffusion Engineers) as active.
- **Solution**: Replace the seed dataset entirely with the exact active issues and subscription/GMP metrics from the user's live screenshots from NSE, BSE, and InvestorGain.

### B. Broken & Updated URLs Clarification
1. **NSE Current Issues URL**:
   - *Old URL*: `https://www.nseindia.com/products-services/initial-public-offerings-current-issues` (Returns "Resource not found" because NSE reorganized their portal).
   - *New Verified Working URL*: `https://www.nseindia.com/market-data/all-upcoming-issues-ipo` (Directly from user's screenshot).
2. **BSE Bidding Status URL**:
   - *Old URL*: `https://www.bseindia.com/markets/publicissues/Bidding_Status.aspx` (Returns Error 404 with notice "The URLs on the site have been updated").
   - *New Verified Working URL*: `https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso` (Directly from user's screenshot).
3. **Chittorgarh Report 82**:
   - The user correctly noted that Report 82 is the master IPO list (dates, issue price, issue size, fresh capital, OFS) and does not display GMP numbers. The app will properly label Chittorgarh Report 82 as "IPO Directory & Capital Structure" and use InvestorGain as the primary source for live GMP.
4. **InvestorGain Live GMP URL**:
   - Verified URL: `https://www.investorgain.com/report/live-ipo-gmp/331/`. All live GMP data, percentage gains, ratings, and lot sizes will match this table 1-to-1.

### C. KSP Build Error
- **Error**: `Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException: Cannot invoke "ksp.com.intellij.openapi.application.Application.getService(java.lang.Class)" because the return value of "ksp.com.intellij.openapi.application.ApplicationManager.getApplication()" is null`
- **Cause**: KSP version `2.3.5` in `libs.versions.toml` is mismatched with Kotlin `2.2.10`. KSP requires strict version alignment with Kotlin or proper Gradle daemon isolation flags (`ksp.useKSP2=true` or compatible compiler bindings).

---

## 2. Proposed Changes

### A. Synchronize IPO & GMP Dataset with User Screenshots (`data/remote/MockIpoDataSource.kt` & Room Database)
We will populate the authentic active listings with exact numbers matching the user's screenshots:

| Company Name | Exchange / Type | Price Band / Issue Price | Lot Size | Issue Size | Open Date | Close Date | Subscription | GMP (₹ / %) | Fire Rating |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Srit India Limited** | NSE & BSE (MainBoard) | ₹123 - ₹130 | 110 | ₹152.88 Cr | 28-Sep | 30-Sep | **2.89x** (Bids: 3.40 Cr) | Active bidding | 🔥🔥 |
| **Shah Investor's Home Ltd** | NSE & BSE (MainBoard) | ₹159 - ₹167 | 90 | ₹63.12 Cr | 28-Sep | 30-Sep | **0.86x** (Bids: 32.60 L) | Active bidding | 🔥 |
| **Nityas Gems & Jewellery Ltd**| NSE & BSE (MainBoard) | ₹75 | 200 | ₹108.35 Cr | 30-Sep | 05-Oct | Active | **₹5 (+6.67%)** | 🔥🔥 |
| **Acme India Industries** | BSE SME | ₹186 - ₹196 | 600 | ₹121.69 Cr | 30-Sep | 06-Oct | Active | **₹30 (+15.31%)** | 🔥🔥🔥 |
| **TNA Solutions** | BSE SME | ₹70 | 2000 | ₹37.86 Cr | 30-Sep | 06-Oct | Active | **₹7 (+10.00%)** | 🔥🔥🔥 |
| **Paramount Syntex Ltd** | BSE SME | ₹119 - ₹127 | 1000 | ₹81.79 Cr | 30-Sep | 06-Oct | Active | **₹0 (0.00%)** | 🔥 |
| **Vishal Nirmiti Limited** | NSE (EQ) | ₹145 - ₹152 | 95 | ₹98.40 Cr | 30-Sep | 05-Oct | Active | ₹12 (+8.1%) | 🔥🔥 |
| **Acme Universal Safezone9 Ltd** | BSE SME | ₹65 - ₹71 | 2000 | ₹18.46 Cr | 28-Sep | 30-Sep | Active | ₹3 (+4.2%) | 🔥 |
| **Shivchem Agro Limited** | BSE SME | ₹59 - ₹62 | 2000 | ₹14.88 Cr | 28-Sep | 30-Sep | Active | ₹4 (+6.5%) | 🔥 |
| **PIND Hospitality Limited** | BSE SME | ₹93 - ₹99 | 1200 | ₹24.75 Cr | 28-Sep | 30-Sep | Active | ₹2 (+2.0%) | 🔥 |
| **Papadmalji Agro Foods Ltd** | NSE SME | ₹108 - ₹114 | 1200 | ₹31.95 Cr | 29-Sep | 01-Oct | **1.33x** (Bids: 37.21 L) | ₹8 (+7.0%) | 🔥🔥 |
| **Green Asia Impex Limited** | NSE SME | ₹84 - ₹90 | 1600 | ₹63.51 Cr | 24-Sep | 01-Oct | **0.52x** (Bids: 36.54 L) | ₹0 (0.0%) | 🔥 |
| **Eventions Limited** | NSE SME | ₹95 - ₹100 | 1200 | ₹32.30 Cr | 30-Sep | 05-Oct | Active | ₹5 (+5.0%) | 🔥 |
| **Cubical Financial Services** | BSE (MainBoard) | ₹2.50 | 10000 | ₹25.00 Cr | 17-Sep | 30-Sep | Active | ₹0.15 (+6.0%) | 🔥 |
| **R.K. Fashion Accessories** | NSE SME (Upcoming) | ₹77 - ₹82 | 1600 | ₹34.99 Cr | 05-Oct | 07-Oct | Upcoming | N/A (Upcoming) | 🔥 |

### B. Update Official Exchange & Verification URLs
Update the verification URLs in the app to match the exact working links verified by the user:
- **NSE All Upcoming Issues**: `https://www.nseindia.com/market-data/all-upcoming-issues-ipo`
- **BSE Live Public Issues**: `https://www.bseindia.com/markets/publicissues/ipoissues.aspx?id=1&type=pso`
- **InvestorGain Live GMP**: `https://www.investorgain.com/report/live-ipo-gmp/331/`
- **Chittorgarh IPO Report**: `https://www.chittorgarh.com/report/ipo-in-india-list-main-board-sme/82/`
- **Registrar Portals**: Link Intime (`linkintime.co.in`), KFintech (`kosmic.kfintech.com`), Bigshare (`bigshareonline.com`).

Provide direct, one-tap "Verify on Exchange" and "View on InvestorGain" buttons on each IPO detail card and on the Data Sources screen.

### C. Fix KSP Gradle Build Issue
- Align the KSP plugin version with the installed Kotlin compiler (`kotlin = "2.2.10"`). Set `googleDevtoolsKsp` to the compatible release (`2.2.10-1.0.31` or configure `ksp.useKSP2=true` in `gradle.properties`).
- Ensure Room compiler annotation processor runs smoothly without AWT / IntelliJ service lookup errors.

---

## 3. Verification Plan

1. **Build Verification**:
   - Run `compile_applet` to ensure compilation passes cleanly without any KSP NPE errors.
2. **Data Accuracy Verification**:
   - Check `OPEN` IPO tab: Confirm SRIT India, Shah Investor's Home, Acme India Industries, TNA Solutions, Nityas Gems & Jewellery, and Paramount Syntex appear with exact pricing, subscription, and GMP matching the screenshots.
   - Check `UPCOMING` IPO tab: Confirm R.K. Fashion Accessories appears with dates 05-Oct to 07-Oct and lot size 1600.
3. **URL Verification**:
   - Verify that clicking official links opens the exact working URLs (NSE All Upcoming Issues, BSE Live Public Issues, InvestorGain Live GMP) without any 404 or resource not found errors.
