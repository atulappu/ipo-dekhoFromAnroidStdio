package com.example.ipotracker.data.remote

import com.example.ipotracker.data.model.*

object MockIpoDataSource {

    val marketIndices = listOf(
        MarketIndex("NIFTY 50", "25,790.95", "+112.40", 0.44, true),
        MarketIndex("SENSEX", "84,299.90", "+356.10", 0.42, true),
        MarketIndex("BANK NIFTY", "53,820.50", "-88.30", -0.16, false),
        MarketIndex("IPO MARKET", "18,945.30", "+184.20", 0.98, true)
    )

    // Master Registrars / RTAs with live URLs, Issues Managed & Total Amount (Rs. Cr.)
    val registrarList: MutableList<RegistrarItem> = mutableListOf(
        RegistrarItem("kfin-tech", "Kfin Technologies Ltd.", "https://ipostatus.kfintech.com/", 82, 53478.51, "Major registrar for Mainboard & SME", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("bigshare-services", "Bigshare Services Pvt.Ltd.", "https://www.bigshareonline.com/ipo_allotment.html", 59, 7558.67, "Leading registrar for SME & Mainboard", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("mufg-intime", "MUFG Intime India Pvt.Ltd.", "https://in.mpms.mufg.com/Initial_Offer/public-issues.html", 54, 56269.35, "Formerly Link Intime India Pvt Ltd", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("maashitla-sec", "Maashitla Securities Pvt.Ltd.", "https://maashitla.com/allotment-status/public-issues", 32, 1186.40, "Popular SME IPO registrar", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("skyline-financial", "Skyline Financial Services Pvt.Ltd.", "https://www.skylinerta.com/display_ipo_rightissue_allotment.php", 17, 578.23, "SME & Mainboard registrar", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("purva-sharegistry", "Purva Sharegistry (India) Pvt.Ltd.", "https://www.purvashare.com/investor-service/ipo-query", 8, 683.62, "RTA service provider", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("cameo-corporate", "Cameo Corporate Services Ltd.", "https://ipo.cameoindia.com/", 8, 505.16, "Established South Indian RTA", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("integrated-registry", "Integrated Registry Management Services Pvt.Ltd.", "https://www.integratedregistry.in/IRMS_V2/IPO.aspx", 5, 226.31, "Registry management", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("mas-services", "MAS Services Ltd.", "https://www.masserv.com/opt.asp", 4, 255.89, "RTA & corporate services", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("mudra-rta", "Mudra RTA Ventures Private Limited", "https://mudrarta.com/display_ipo_rightissue_allotment.php", 4, 181.69, "Specialized SME RTA", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("alankit-assignments", "Alankit Assignments Ltd.", "https://ipo.alankit.com/", 1, 38.45, "Citizen & registry services", "2026-09-30", "2026-09-30", "Admin"),
        RegistrarItem("abhipra-capital", "Abhipra Capital Limited", "https://www.abhipra.com/ipo-status", 1, 31.48, "Capital market intermediary", "2026-09-30", "2026-09-30", "Admin")
    )

    fun getRegistrarUrl(name: String): String {
        val lower = name.lowercase().trim()
        return registrarList.firstOrNull { reg ->
            val regLower = reg.name.lowercase()
            reg.id == lower ||
            regLower.contains(lower) ||
            lower.contains(regLower) ||
            (lower.contains("link intime") && (regLower.contains("mufg") || regLower.contains("intime"))) ||
            (lower.contains("mufg") && regLower.contains("intime")) ||
            (lower.contains("kfin") && regLower.contains("kfin")) ||
            (lower.contains("bigshare") && regLower.contains("bigshare")) ||
            (lower.contains("maashitla") && regLower.contains("maashitla")) ||
            (lower.contains("skyline") && regLower.contains("skyline")) ||
            (lower.contains("purva") && regLower.contains("purva")) ||
            (lower.contains("cameo") && regLower.contains("cameo")) ||
            (lower.contains("integrated") && regLower.contains("integrated")) ||
            (lower.contains("mas") && regLower.contains("mas")) ||
            (lower.contains("mudra") && regLower.contains("mudra")) ||
            (lower.contains("alankit") && regLower.contains("alankit")) ||
            (lower.contains("abhipra") && regLower.contains("abhipra"))
        }?.url ?: "https://in.mpms.mufg.com/Initial_Offer/public-issues.html"
    }

    fun updateRegistrarUrl(id: String, newUrl: String, comments: String? = null, modifiedBy: String = "Admin"): Boolean {
        val index = registrarList.indexOfFirst { it.id == id }
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH).format(java.util.Date())
        if (index != -1) {
            val old = registrarList[index]
            registrarList[index] = old.copy(
                url = newUrl.trim(),
                comments = comments ?: old.comments,
                modifiedDate = todayDate,
                modifiedBy = modifiedBy
            )
            return true
        }
        return false
    }

    fun addOrUpdateRegistrar(item: RegistrarItem): Boolean {
        val index = registrarList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            registrarList[index] = item
        } else {
            registrarList.add(item)
        }
        return true
    }

    val ipoList: List<IpoItem> = listOf(
        // =========================================================================
        // 1. OPEN MAINBOARD IPO: SRIT India Ltd (NSE & BSE)
        // Data verified against user's NSE, BSE & InvestorGain live screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-srit",
            name = "SRIT India Ltd",
            symbol = "SRIT",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.OPEN,
            priceBandMin = 123.0,
            priceBandMax = 130.0,
            lotSize = 110,
            minInvestment = 14300.0,
            issueSizeCr = 152.88,
            freshIssueCr = 120.00,
            ofsCr = 32.88,
            openDate = "28-Sep-2026",
            closeDate = "30-Sep-2026",
            allotmentDate = "01-Oct-2026",
            listingDate = "06-Oct-2026",
            currentGmp = 28.0,
            estimatedListingPrice = 158.0,
            estimatedGainPercent = 21.54,
            lastGmpUpdated = "30-Sep-2026 12:30 IST (InvestorGain Live)",
            currentSubscriptionTimes = 2.89,
            qibTimes = 1.12,
            niiTimes = 4.25,
            retailTimes = 3.30,
            description = "SRIT India Ltd provides enterprise healthcare automation, telecom IT infrastructure, and digital governance solutions across India and international markets.",
            sector = "Information Technology & Healthcare IT",
            listingExchanges = "BSE, NSE",
            faceValue = 10.0,
            leadManagers = "SBI Capital Markets, Anand Rathi Advisors",
            registrar = "MUFG Intime India Pvt.Ltd.",
            promoterHoldingPre = 76.5,
            promoterHoldingPost = 58.2,
            objectsOfIssue = listOf(
                "Funding expansion of smart healthcare cloud integration centers.",
                "Investment in AI-driven telecom billing architecture.",
                "Working capital requirements and general corporate purposes."
            ),
            subscriptionDetails = SubscriptionDetails(
                overallTimes = 2.89,
                qibTimes = 1.12,
                niiTimes = 4.25,
                retailTimes = 3.30,
                employeeTimes = 1.85,
                categoryRows = listOf(
                    SubscriptionRow("Qualified Institutional (QIB)", 2400000, 2688000, 1.12),
                    SubscriptionRow("Non-Institutional (NII / HNI)", 1800000, 7650000, 4.25),
                    SubscriptionRow("Retail Individual (RII)", 4200000, 13860000, 3.30),
                    SubscriptionRow("Total Issue (Bids: 3.40 Cr shares)", 8400000, 24198000, 2.89)
                ),
                dayProgress = listOf(
                    SubscriptionDayProgress("Day 1", "28-Sep-2026", 0.95, 0.20, 1.40, 1.25),
                    SubscriptionDayProgress("Day 2", "29-Sep-2026", 1.82, 0.65, 2.80, 2.10),
                    SubscriptionDayProgress("Day 3", "30-Sep-2026", 2.89, 1.12, 4.25, 3.30)
                ),
                lastUpdated = "30-Sep-2026 13:00 IST (Day 3 Closing Tracker)"
            ),
            gmpHistory = listOf(
                GmpHistoryItem("27-Sep-2026", 22.0, 152.0, 16.92, GmpTrend.POSITIVE),
                GmpHistoryItem("28-Sep-2026", 25.0, 155.0, 19.23, GmpTrend.POSITIVE),
                GmpHistoryItem("29-Sep-2026", 27.0, 157.0, 20.77, GmpTrend.POSITIVE),
                GmpHistoryItem("30-Sep-2026", 28.0, 158.0, 21.54, GmpTrend.POSITIVE)
            ),
            financials = listOf(
                FinancialYearData("FY2023", 285.0, 42.0, 22.0, 5.80, 140.0, 290.0, 45.0, 18.2, 20.1, 0.32, 33.6),
                FinancialYearData("FY2024", 395.0, 68.0, 38.5, 9.40, 185.0, 380.0, 40.0, 21.5, 23.8, 0.22, 20.7),
                FinancialYearData("FY2025", 540.0, 102.0, 64.0, 15.20, 260.0, 510.0, 32.0, 24.6, 26.9, 0.12, 12.8)
            ),
            importantDates = listOf(
                ImportantDateItem("DRHP Filed", "12-Apr-2026", DateStatus.COMPLETED),
                ImportantDateItem("RHP Filed", "22-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Opens", "28-Sep-2026", DateStatus.COMPLETED, "10:00 AM IST"),
                ImportantDateItem("IPO Closes", "30-Sep-2026", DateStatus.ACTIVE, "5:00 PM IST (Today)"),
                ImportantDateItem("Basis of Allotment", "01-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "06-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "MUFG Intime India Pvt.Ltd.",
                registrarUrl = "https://in.mpms.mufg.com/Initial_Offer/public-issues.html",
                allotmentDate = "01-Oct-2026",
                isAvailable = false,
                note = "Allotment will be declared on 01-Oct-2026 by MUFG Intime."
            ),
            analysisReport = AnalysisReport(
                businessSummary = "SRIT India is an enterprise IT solutions provider specializing in government e-governance and healthcare systems.",
                financialSnapshot = "Three-year revenue CAGR of 37.8% with consistent margin expansion. ROE exceeds 24%.",
                valuationSnapshot = "P/E of 12.8x based on FY25 EPS, offering comfortable upside on listing.",
                keyPositives = listOf("Long-term mission critical enterprise contracts", "Low debt with high return on equity"),
                keyRisks = listOf("Receivables cycle from government and public sector entities", "Competition from large tier-1 IT services"),
                importantChecks = listOf("Execution pace of Smart City and e-Hospital rollouts", "Private sector revenue diversification"),
                isAiGenerated = true,
                generatedDate = "30-Sep-2026"
            )
        ),

        // =========================================================================
        // 2. OPEN MAINBOARD IPO: Shah Investor's Home Ltd (NSE & BSE)
        // Data verified against user's NSE, BSE & InvestorGain live screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-shah-investor",
            name = "Shah Investor's Home Ltd",
            symbol = "SHAHINV",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.OPEN,
            priceBandMin = 159.0,
            priceBandMax = 167.0,
            lotSize = 90,
            minInvestment = 15030.0,
            issueSizeCr = 63.12,
            freshIssueCr = 63.12,
            ofsCr = 0.0,
            openDate = "28-Sep-2026",
            closeDate = "30-Sep-2026",
            allotmentDate = "01-Oct-2026",
            listingDate = "06-Oct-2026",
            currentGmp = 12.0,
            estimatedListingPrice = 179.0,
            estimatedGainPercent = 7.19,
            lastGmpUpdated = "30-Sep-2026 12:15 IST (InvestorGain Live)",
            currentSubscriptionTimes = 0.86,
            qibTimes = 0.50,
            niiTimes = 1.10,
            retailTimes = 0.98,
            description = "Shah Investor's Home Ltd is a diversified financial services brokerage providing equity broking, wealth management, depository services, and mutual fund distribution.",
            sector = "Financial Services - Capital Markets & Broking",
            listingExchanges = "BSE, NSE",
            faceValue = 10.0,
            leadManagers = "Beeline Capital Advisors Pvt Ltd",
            registrar = "Bigshare Services Pvt.Ltd.",
            promoterHoldingPre = 88.0,
            promoterHoldingPost = 65.4,
            subscriptionDetails = SubscriptionDetails(
                overallTimes = 0.86,
                qibTimes = 0.50,
                niiTimes = 1.10,
                retailTimes = 0.98,
                categoryRows = listOf(
                    SubscriptionRow("Qualified Institutional (QIB)", 1100000, 550000, 0.50),
                    SubscriptionRow("Non-Institutional (NII / HNI)", 800000, 880000, 1.10),
                    SubscriptionRow("Retail Individual (RII)", 1900000, 1862000, 0.98),
                    SubscriptionRow("Total Issue (Bids: 32.60 L shares)", 3800000, 3292000, 0.86)
                ),
                lastUpdated = "30-Sep-2026 13:00 IST (Day 3 Bidding Tracker)"
            ),
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "28-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "30-Sep-2026", DateStatus.ACTIVE, "Closes 5:00 PM IST Today"),
                ImportantDateItem("Basis of Allotment", "01-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "06-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "01-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 3. OPEN MAINBOARD IPO: Nityas Gems & Jewellery Ltd (NSE & BSE)
        // Data verified against user's NSE, BSE & InvestorGain screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-nityas",
            name = "Nityas Gems & Jewellery Ltd",
            symbol = "NITYAS",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.OPEN,
            priceBandMin = 75.0,
            priceBandMax = 75.0,
            lotSize = 200,
            minInvestment = 15000.0,
            issueSizeCr = 108.35,
            freshIssueCr = 108.35,
            ofsCr = 0.0,
            openDate = "30-Sep-2026",
            closeDate = "05-Oct-2026",
            allotmentDate = "06-Oct-2026",
            listingDate = "09-Oct-2026",
            currentGmp = 5.0,
            estimatedListingPrice = 80.0,
            estimatedGainPercent = 6.67,
            lastGmpUpdated = "30-Sep-2026 11:30 IST (InvestorGain Live)",
            currentSubscriptionTimes = 0.65,
            qibTimes = 0.20,
            niiTimes = 0.85,
            retailTimes = 0.90,
            description = "Nityas Gems & Jewellery Ltd manufactures and retails studded diamond jewelry, hallmarked gold ornaments, and precious gemstones through retail showrooms and B2B export channels.",
            sector = "Consumer Discretionary - Gems & Jewellery",
            listingExchanges = "BSE, NSE",
            faceValue = 10.0,
            leadManagers = "Fast Track Finsec Pvt Ltd",
            registrar = "Bigshare Services Pvt.Ltd.",
            promoterHoldingPre = 92.5,
            promoterHoldingPost = 68.0,
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "30-Sep-2026", DateStatus.ACTIVE, "Opens Today"),
                ImportantDateItem("IPO Closes", "05-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Basis of Allotment", "06-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "09-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "06-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 4. OPEN SME IPO: Acme India Industries Ltd (BSE SME)
        // Data verified against user's BSE & InvestorGain live screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-acme-india",
            name = "Acme India Industries Ltd",
            symbol = "ACMEIND",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 186.0,
            priceBandMax = 196.0,
            lotSize = 600,
            minInvestment = 117600.0,
            issueSizeCr = 121.69,
            freshIssueCr = 121.69,
            ofsCr = 0.0,
            openDate = "30-Sep-2026",
            closeDate = "06-Oct-2026",
            allotmentDate = "07-Oct-2026",
            listingDate = "10-Oct-2026",
            currentGmp = 30.0,
            estimatedListingPrice = 226.0,
            estimatedGainPercent = 15.31,
            lastGmpUpdated = "30-Sep-2026 12:45 IST (InvestorGain Live)",
            currentSubscriptionTimes = 1.15,
            qibTimes = 0.40,
            niiTimes = 1.60,
            retailTimes = 1.45,
            description = "Acme India Industries Ltd specializes in high-precision precision engineered mechanical parts, industrial sheet metal stamping, and specialized alloy fabrication for automotive and aerospace OEMs.",
            sector = "Industrial Engineering & Auto Components",
            listingExchanges = "BSE SME",
            faceValue = 10.0,
            leadManagers = "GYR Capital Advisors Pvt Ltd",
            registrar = "MUFG Intime India Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "30-Sep-2026", DateStatus.ACTIVE, "Day 1 Bidding"),
                ImportantDateItem("IPO Closes", "06-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Basis of Allotment", "07-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "10-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "MUFG Intime India Pvt.Ltd.",
                registrarUrl = "https://in.mpms.mufg.com/Initial_Offer/public-issues.html",
                allotmentDate = "07-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 5. OPEN SME IPO: TNA Solutions Ltd (BSE SME)
        // Data verified against user's BSE & InvestorGain live screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-tna-solutions",
            name = "TNA Solutions Ltd",
            symbol = "TNASOL",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 70.0,
            priceBandMax = 70.0,
            lotSize = 2000,
            minInvestment = 140000.0,
            issueSizeCr = 37.86,
            freshIssueCr = 37.86,
            ofsCr = 0.0,
            openDate = "30-Sep-2026",
            closeDate = "06-Oct-2026",
            allotmentDate = "07-Oct-2026",
            listingDate = "10-Oct-2026",
            currentGmp = 7.0,
            estimatedListingPrice = 77.0,
            estimatedGainPercent = 10.00,
            lastGmpUpdated = "30-Sep-2026 12:00 IST (InvestorGain Live)",
            currentSubscriptionTimes = 0.95,
            qibTimes = 0.50,
            niiTimes = 1.20,
            retailTimes = 1.15,
            description = "TNA Solutions Ltd delivers turnkey industrial packaging machinery, food processing automation lines, and robotic material handling solutions.",
            sector = "Industrial Machinery & Automation",
            listingExchanges = "BSE SME",
            faceValue = 10.0,
            leadManagers = "Interactive Financial Services Ltd",
            registrar = "Kfin Technologies Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "30-Sep-2026", DateStatus.ACTIVE, "Opens Today"),
                ImportantDateItem("IPO Closes", "06-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Basis of Allotment", "07-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "10-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Kfin Technologies Ltd.",
                registrarUrl = "https://ipostatus.kfintech.com/",
                allotmentDate = "07-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 6. OPEN SME IPO: Paramount Syntex Ltd (BSE SME)
        // Data verified against user's BSE & InvestorGain live screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-paramount-syntex",
            name = "Paramount Syntex Ltd",
            symbol = "PARAMSNT",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 119.0,
            priceBandMax = 127.0,
            lotSize = 1000,
            minInvestment = 127000.0,
            issueSizeCr = 81.79,
            freshIssueCr = 81.79,
            ofsCr = 0.0,
            openDate = "30-Sep-2026",
            closeDate = "06-Oct-2026",
            allotmentDate = "07-Oct-2026",
            listingDate = "10-Oct-2026",
            currentGmp = 0.0,
            estimatedListingPrice = 127.0,
            estimatedGainPercent = 0.0,
            lastGmpUpdated = "30-Sep-2026 (InvestorGain: ₹0 GMP)",
            currentSubscriptionTimes = 0.42,
            description = "Paramount Syntex Ltd manufactures synthetic yarns, dyed polyester filament, and technical blended fabrics for apparel manufacturers across domestic markets.",
            sector = "Textiles & Synthetic Filaments",
            listingExchanges = "BSE SME",
            faceValue = 10.0,
            leadManagers = "Fedex Securities Pvt Ltd",
            registrar = "Bigshare Services Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "30-Sep-2026", DateStatus.ACTIVE, "Opens Today"),
                ImportantDateItem("IPO Closes", "06-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Basis of Allotment", "07-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "10-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "07-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 7. OPEN MAINBOARD IPO: Vishal Nirmiti Limited (NSE EQ)
        // Data verified against user's NSE All Upcoming Issues screenshot
        // =========================================================================
        IpoItem(
            id = "ipo-vishal-nirmiti",
            name = "Vishal Nirmiti Limited",
            symbol = "VISHALNIRM",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.OPEN,
            priceBandMin = 145.0,
            priceBandMax = 152.0,
            lotSize = 95,
            minInvestment = 14440.0,
            issueSizeCr = 98.40,
            freshIssueCr = 98.40,
            ofsCr = 0.0,
            openDate = "30-Sep-2026",
            closeDate = "05-Oct-2026",
            allotmentDate = "06-Oct-2026",
            listingDate = "09-Oct-2026",
            currentGmp = 12.0,
            estimatedListingPrice = 164.0,
            estimatedGainPercent = 7.89,
            lastGmpUpdated = "30-Sep-2026 11:45 IST (InvestorGain Live)",
            currentSubscriptionTimes = 0.78,
            qibTimes = 0.35,
            niiTimes = 0.90,
            retailTimes = 1.10,
            description = "Vishal Nirmiti Limited is a premier infrastructure concrete product manufacturer specializing in pre-stressed railway concrete sleepers, specialized track turnout components, and civil infrastructure girders.",
            sector = "Infrastructure & Railway Components",
            listingExchanges = "NSE",
            faceValue = 10.0,
            leadManagers = "Pantomath Capital Advisors Pvt Ltd",
            registrar = "Kfin Technologies Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "30-Sep-2026", DateStatus.ACTIVE, "Opens Today on NSE"),
                ImportantDateItem("IPO Closes", "05-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Basis of Allotment", "06-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "09-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Kfin Technologies Ltd.",
                registrarUrl = "https://ipostatus.kfintech.com/",
                allotmentDate = "06-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 8. OPEN SME IPO: Acme Universal Safezone9 Ltd (BSE SME)
        // Data verified against user's BSE & InvestorGain live screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-acme-safezone",
            name = "Acme Universal Safezone9 Ltd",
            symbol = "ACMESAFE",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 65.0,
            priceBandMax = 71.0,
            lotSize = 2000,
            minInvestment = 142000.0,
            issueSizeCr = 18.46,
            freshIssueCr = 18.46,
            ofsCr = 0.0,
            openDate = "28-Sep-2026",
            closeDate = "30-Sep-2026",
            allotmentDate = "01-Oct-2026",
            listingDate = "06-Oct-2026",
            currentGmp = 3.0,
            estimatedListingPrice = 74.0,
            estimatedGainPercent = 4.23,
            lastGmpUpdated = "30-Sep-2026 12:30 IST",
            currentSubscriptionTimes = 4.82,
            description = "Acme Universal Safezone9 Ltd develops fire prevention, industrial occupational safety equipment, flame retardant safety garments, and hazardous environment containment gear.",
            sector = "Industrial Safety Equipment",
            listingExchanges = "BSE SME",
            faceValue = 10.0,
            leadManagers = "Inventure Merchant Banker Services",
            registrar = "Maashitla Securities Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "28-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "30-Sep-2026", DateStatus.ACTIVE, "Closes Today"),
                ImportantDateItem("Basis of Allotment", "01-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "06-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Maashitla Securities Pvt.Ltd.",
                registrarUrl = "https://maashitla.com/allotment-status/public-issues",
                allotmentDate = "01-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 9. OPEN SME IPO: Shivchem Agro Limited (BSE SME)
        // Data verified against user's BSE & InvestorGain live screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-shivchem",
            name = "Shivchem Agro Limited",
            symbol = "SHIVCHEM",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 59.0,
            priceBandMax = 62.0,
            lotSize = 2000,
            minInvestment = 124000.0,
            issueSizeCr = 14.88,
            freshIssueCr = 14.88,
            ofsCr = 0.0,
            openDate = "28-Sep-2026",
            closeDate = "30-Sep-2026",
            allotmentDate = "01-Oct-2026",
            listingDate = "06-Oct-2026",
            currentGmp = 4.0,
            estimatedListingPrice = 66.0,
            estimatedGainPercent = 6.45,
            lastGmpUpdated = "30-Sep-2026 12:15 IST",
            currentSubscriptionTimes = 3.15,
            description = "Shivchem Agro Limited manufactures crop protection solutions, bio-fertilizers, micro-nutrients, and plant growth regulators serving rural agricultural belts in Western and Northern India.",
            sector = "Agro Chemicals & Crop Protection",
            listingExchanges = "BSE SME",
            faceValue = 10.0,
            leadManagers = "Finshore Management Services Ltd",
            registrar = "Bigshare Services Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "28-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "30-Sep-2026", DateStatus.ACTIVE, "Closes Today"),
                ImportantDateItem("Basis of Allotment", "01-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "06-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "01-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 10. OPEN SME IPO: PIND Hospitality Limited (BSE SME)
        // Data verified against user's BSE & InvestorGain live screenshots
        // =========================================================================
        IpoItem(
            id = "ipo-pind",
            name = "PIND Hospitality Limited",
            symbol = "PINDHOSP",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 93.0,
            priceBandMax = 99.0,
            lotSize = 1200,
            minInvestment = 118800.0,
            issueSizeCr = 24.75,
            freshIssueCr = 24.75,
            ofsCr = 0.0,
            openDate = "28-Sep-2026",
            closeDate = "30-Sep-2026",
            allotmentDate = "01-Oct-2026",
            listingDate = "06-Oct-2026",
            currentGmp = 2.0,
            estimatedListingPrice = 101.0,
            estimatedGainPercent = 2.02,
            lastGmpUpdated = "30-Sep-2026 12:00 IST",
            currentSubscriptionTimes = 1.95,
            description = "PIND Hospitality operates theme-based ethnic Punjabi casual dining restaurants, heritage boutique resorts, and highway banquet facilities across North India.",
            sector = "Hospitality & Restaurants",
            listingExchanges = "BSE SME",
            faceValue = 10.0,
            leadManagers = "Swastika Investmart Ltd",
            registrar = "Skyline Financial Services Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "28-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "30-Sep-2026", DateStatus.ACTIVE, "Closes Today"),
                ImportantDateItem("Basis of Allotment", "01-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "06-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Skyline Financial Services Pvt.Ltd.",
                registrarUrl = "https://www.skylinerta.com/display_ipo_rightissue_allotment.php",
                allotmentDate = "01-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // OPEN SME IPO: VANS Electroengineerings Ltd (BSE SME)
        // Data verified against BSE SME & InvestorGain live feed
        // =========================================================================
        IpoItem(
            id = "ipo-vans-electroengineerings",
            name = "VANS Electroengineerings Ltd",
            symbol = "VANSELEC",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 118.0,
            priceBandMax = 118.0,
            lotSize = 1200,
            minInvestment = 141600.0,
            issueSizeCr = 33.98,
            freshIssueCr = 33.98,
            ofsCr = 0.0,
            openDate = "29-Sep-2026",
            closeDate = "01-Oct-2026",
            allotmentDate = "05-Oct-2026",
            listingDate = "07-Oct-2026",
            currentGmp = 90.0,
            estimatedListingPrice = 208.0,
            estimatedGainPercent = 76.27,
            lastGmpUpdated = "30-Sep-2026 16:37 IST",
            currentSubscriptionTimes = 53.79,
            description = "VANS Electroengineerings Ltd is a specialized provider of electroengineering solutions, power infrastructure, and electrical switchgear assemblies.",
            sector = "Capital Goods - Electrical Equipment",
            listingExchanges = "BSE SME",
            faceValue = 10.0,
            leadManagers = "Pantomath Capital Advisors Pvt Ltd",
            registrar = "MUFG Intime India Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "29-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "01-Oct-2026", DateStatus.ACTIVE, "Bidding Active"),
                ImportantDateItem("Basis of Allotment", "05-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "07-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "MUFG Intime India Pvt.Ltd.",
                registrarUrl = "https://in.mpms.mufg.com/Initial_Offer/public-issues.html",
                allotmentDate = "05-Oct-2026",
                isAvailable = false
            ),
            isDemoData = false
        ),

        // =========================================================================
        // 11. OPEN SME IPO: Papadmalji Agro Foods Ltd (NSE SME)
        // Data verified against user's NSE All Upcoming Issues screenshot
        // =========================================================================
        IpoItem(
            id = "ipo-papadmalji",
            name = "Papadmalji Agro Foods Ltd",
            symbol = "PAPADMAL",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 108.0,
            priceBandMax = 114.0,
            lotSize = 1200,
            minInvestment = 136800.0,
            issueSizeCr = 31.95,
            freshIssueCr = 31.95,
            ofsCr = 0.0,
            openDate = "29-Sep-2026",
            closeDate = "01-Oct-2026",
            allotmentDate = "03-Oct-2026",
            listingDate = "07-Oct-2026",
            currentGmp = 8.0,
            estimatedListingPrice = 122.0,
            estimatedGainPercent = 7.02,
            lastGmpUpdated = "30-Sep-2026 12:15 IST",
            currentSubscriptionTimes = 1.33,
            description = "Papadmalji Agro Foods Ltd processes and packages traditional ready-to-cook savory snacks, handmade roasted papads, spiced namkeens, and culinary flour mixes.",
            sector = "FMCG - Packaged Foods",
            listingExchanges = "NSE SME",
            faceValue = 10.0,
            leadManagers = "Hem Securities Ltd",
            registrar = "Bigshare Services Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "29-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "01-Oct-2026", DateStatus.ACTIVE, "Bidding Active"),
                ImportantDateItem("Basis of Allotment", "03-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "07-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "03-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 12. OPEN SME IPO: Green Asia Impex Limited (NSE SME)
        // Data verified against user's NSE All Upcoming Issues screenshot
        // =========================================================================
        IpoItem(
            id = "ipo-green-asia",
            name = "Green Asia Impex Limited",
            symbol = "GREENASIA",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 84.0,
            priceBandMax = 90.0,
            lotSize = 1600,
            minInvestment = 144000.0,
            issueSizeCr = 63.51,
            freshIssueCr = 63.51,
            ofsCr = 0.0,
            openDate = "24-Sep-2026",
            closeDate = "01-Oct-2026",
            allotmentDate = "03-Oct-2026",
            listingDate = "07-Oct-2026",
            currentGmp = 0.0,
            estimatedListingPrice = 90.0,
            estimatedGainPercent = 0.00,
            lastGmpUpdated = "30-Sep-2026 (InvestorGain: ₹0 GMP)",
            currentSubscriptionTimes = 0.52,
            description = "Green Asia Impex Limited is an import-export trading house distributing agricultural spices, organic pulses, timber, and sustainable building materials across Southeast Asia and the Middle East.",
            sector = "Trading & Global Commodities",
            listingExchanges = "NSE SME",
            faceValue = 10.0,
            leadManagers = "First Overseas Capital Ltd",
            registrar = "Kfin Technologies Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "24-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "01-Oct-2026", DateStatus.ACTIVE),
                ImportantDateItem("Basis of Allotment", "03-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "07-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Kfin Technologies Ltd.",
                registrarUrl = "https://ipostatus.kfintech.com/",
                allotmentDate = "03-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 13. OPEN SME IPO: Eventions Limited (NSE SME)
        // Data verified against user's NSE screenshot
        // =========================================================================
        IpoItem(
            id = "ipo-eventions",
            name = "Eventions Limited",
            symbol = "EVENTIONS",
            category = IpoCategory.SME,
            status = IpoStatus.OPEN,
            priceBandMin = 95.0,
            priceBandMax = 100.0,
            lotSize = 1200,
            minInvestment = 120000.0,
            issueSizeCr = 32.30,
            freshIssueCr = 32.30,
            ofsCr = 0.0,
            openDate = "30-Sep-2026",
            closeDate = "05-Oct-2026",
            allotmentDate = "06-Oct-2026",
            listingDate = "09-Oct-2026",
            currentGmp = 5.0,
            estimatedListingPrice = 105.0,
            estimatedGainPercent = 5.00,
            lastGmpUpdated = "30-Sep-2026 11:30 IST",
            currentSubscriptionTimes = 0.82,
            description = "Eventions Limited creates integrated corporate event management, MICE logistics, commercial experiential brand activations, and large-scale exhibition infrastructure.",
            sector = "Media & Experiential Marketing",
            listingExchanges = "NSE SME",
            faceValue = 10.0,
            leadManagers = "Beeline Capital Advisors Pvt Ltd",
            registrar = "Purva Sharegistry (India) Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "30-Sep-2026", DateStatus.ACTIVE, "Opens Today on NSE"),
                ImportantDateItem("IPO Closes", "05-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Basis of Allotment", "06-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "09-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Purva Sharegistry (India) Pvt.Ltd.",
                registrarUrl = "https://www.purvashare.com/investor-service/ipo-query",
                allotmentDate = "06-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 14. OPEN MAINBOARD IPO: Cubical Financial Services Ltd (BSE)
        // Data verified against user's BSE Public Issues screenshot
        // =========================================================================
        IpoItem(
            id = "ipo-cubical",
            name = "Cubical Financial Services Ltd",
            symbol = "CUBICAL",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.OPEN,
            priceBandMin = 2.50,
            priceBandMax = 2.50,
            lotSize = 10000,
            minInvestment = 25000.0,
            issueSizeCr = 25.00,
            freshIssueCr = 25.00,
            ofsCr = 0.0,
            openDate = "17-Sep-2026",
            closeDate = "30-Sep-2026",
            allotmentDate = "01-Oct-2026",
            listingDate = "06-Oct-2026",
            currentGmp = 0.15,
            estimatedListingPrice = 2.65,
            estimatedGainPercent = 6.00,
            lastGmpUpdated = "30-Sep-2026 (Live Bidding)",
            currentSubscriptionTimes = 2.10,
            description = "Cubical Financial Services provides loan syndication, merchant advisory, trade finance assistance, and working capital advisory to micro and medium enterprises.",
            sector = "Financial Services - Advisory",
            listingExchanges = "BSE",
            faceValue = 1.0,
            leadManagers = "Inventure Merchant Banker Services",
            registrar = "Bigshare Services Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "17-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "30-Sep-2026", DateStatus.ACTIVE, "Closes Today"),
                ImportantDateItem("Basis of Allotment", "01-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "06-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "01-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 15. UPCOMING SME IPO: R.K. Fashion Accessories Ltd (NSE SME)
        // Data verified against user's NSE screenshot (Dates: 05-Oct to 07-Oct)
        // =========================================================================
        IpoItem(
            id = "ipo-rk-fashion",
            name = "R.K. Fashion Accessories Ltd",
            symbol = "RKFASHION",
            category = IpoCategory.SME,
            status = IpoStatus.UPCOMING,
            priceBandMin = 77.0,
            priceBandMax = 82.0,
            lotSize = 1600,
            minInvestment = 131200.0,
            issueSizeCr = 34.99,
            freshIssueCr = 34.99,
            ofsCr = 0.0,
            openDate = "05-Oct-2026",
            closeDate = "07-Oct-2026",
            allotmentDate = "08-Oct-2026",
            listingDate = "12-Oct-2026",
            currentGmp = 0.0,
            estimatedListingPrice = 82.0,
            estimatedGainPercent = 0.0,
            lastGmpUpdated = "Upcoming (Awaiting Grey Market Quotes)",
            currentSubscriptionTimes = 0.0,
            description = "R.K. Fashion Accessories Ltd manufactures metal zippers, premium garment buttons, designer hooks, and fashion jewelry hardware accessories for export garment export houses.",
            sector = "Textiles & Garment Accessories",
            listingExchanges = "NSE SME",
            faceValue = 10.0,
            leadManagers = "Gretex Corporate Services Ltd",
            registrar = "Bigshare Services Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "05-Oct-2026", DateStatus.UPCOMING, "Opens 05-Oct-2026"),
                ImportantDateItem("IPO Closes", "07-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Basis of Allotment", "08-Oct-2026", DateStatus.UPCOMING),
                ImportantDateItem("Listing Date", "12-Oct-2026", DateStatus.UPCOMING)
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "08-Oct-2026",
                isAvailable = false
            )
        ),

        // =========================================================================
        // 16. HISTORICAL MAINBOARD IPO: Garuda Construction and Engineering Ltd (Listed Oct 2024)
        // =========================================================================
        IpoItem(
            id = "ipo-garuda",
            name = "Garuda Construction and Engineering Ltd",
            symbol = "GARUDA",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.LISTED,
            listingStatus = ListingStatus.LISTED,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            priceBandMin = 90.0,
            priceBandMax = 95.0,
            lotSize = 157,
            minInvestment = 14915.0,
            issueSizeCr = 264.10,
            freshIssueCr = 173.85,
            ofsCr = 90.25,
            openDate = "08-Oct-2024",
            closeDate = "10-Oct-2024",
            allotmentDate = "11-Oct-2024",
            listingDate = "15-Oct-2024",
            listingPrice = 105.0,
            listingGainPercent = 10.53,
            currentMarketPrice = 98.40,
            currentReturnPercent = 3.58,
            currentGmp = 0.0,
            estimatedListingPrice = 105.0,
            estimatedGainPercent = 10.53,
            lastGmpUpdated = "Listed on NSE / BSE",
            currentSubscriptionTimes = 7.55,
            description = "Garuda Construction provides end-to-end civil construction for residential, commercial, industrial, and infrastructure projects across India.",
            sector = "Construction & Infrastructure",
            listingExchanges = "BSE, NSE",
            faceValue = 5.0,
            leadManagers = "Corpwis Advisors Pvt Ltd",
            registrar = "MUFG Intime India Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "08-Oct-2024", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "10-Oct-2024", DateStatus.COMPLETED),
                ImportantDateItem("Basis of Allotment", "11-Oct-2024", DateStatus.COMPLETED),
                ImportantDateItem("Listing Date", "15-Oct-2024", DateStatus.COMPLETED, "Listed at ₹105 (+10.5%)")
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "MUFG Intime India Pvt.Ltd.",
                registrarUrl = "https://in.mpms.mufg.com/Initial_Offer/public-issues.html",
                allotmentDate = "11-Oct-2024",
                isAvailable = true,
                note = "Allotment declared on MUFG Intime portal."
            ),
            sourceId = "SRC_EXCHANGE_HISTORICAL",
            isSourceVerified = true
        ),

        // =========================================================================
        // 17. HISTORICAL MEGA MAINBOARD IPO: Hyundai Motor India Ltd (Listed Oct 2024)
        // =========================================================================
        IpoItem(
            id = "ipo-hyundai",
            name = "Hyundai Motor India Ltd",
            symbol = "HYUNDAI",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.LISTED,
            listingStatus = ListingStatus.LISTED,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            priceBandMin = 1865.0,
            priceBandMax = 1960.0,
            lotSize = 7,
            minInvestment = 13720.0,
            issueSizeCr = 27870.16,
            freshIssueCr = 0.0,
            ofsCr = 27870.16,
            openDate = "15-Oct-2024",
            closeDate = "17-Oct-2024",
            allotmentDate = "18-Oct-2024",
            listingDate = "22-Oct-2024",
            listingPrice = 1934.0,
            listingGainPercent = -1.33,
            currentMarketPrice = 1790.0,
            currentReturnPercent = -8.67,
            currentGmp = 0.0,
            estimatedListingPrice = 1934.0,
            estimatedGainPercent = -1.33,
            lastGmpUpdated = "Listed on NSE / BSE",
            currentSubscriptionTimes = 2.37,
            description = "Hyundai Motor India is India's second largest passenger vehicle manufacturer with popular models like Creta, Venue, Verna, and Ioniq 5 EV.",
            sector = "Automobile - 4 Wheeler OEM",
            listingExchanges = "BSE, NSE",
            faceValue = 10.0,
            leadManagers = "Kotak Mahindra, Morgan Stanley, Citigroup, HSBC",
            registrar = "Kfin Technologies Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "15-Oct-2024", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "17-Oct-2024", DateStatus.COMPLETED),
                ImportantDateItem("Basis of Allotment", "18-Oct-2024", DateStatus.COMPLETED),
                ImportantDateItem("Listing Date", "22-Oct-2024", DateStatus.COMPLETED, "Listed at ₹1934 (-1.3%)")
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Kfin Technologies Ltd.",
                registrarUrl = "https://ipostatus.kfintech.com/",
                allotmentDate = "18-Oct-2024",
                isAvailable = true,
                note = "Allotment declared on Kfin Technologies portal."
            ),
            sourceId = "SRC_EXCHANGE_HISTORICAL",
            isSourceVerified = true
        ),

        // =========================================================================
        // 18. ALLOTMENT OUT IPO: Manba Finance Ltd (Listing Today)
        // =========================================================================
        IpoItem(
            id = "ipo-manba",
            name = "Manba Finance Ltd",
            symbol = "MANBA",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.ALLOTMENT_AVAILABLE,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            priceBandMin = 114.0,
            priceBandMax = 120.0,
            lotSize = 125,
            minInvestment = 15000.0,
            issueSizeCr = 150.84,
            freshIssueCr = 150.84,
            ofsCr = 0.0,
            openDate = "23-Sep-2026",
            closeDate = "25-Sep-2026",
            allotmentDate = "26-Sep-2026",
            listingDate = "30-Sep-2026",
            currentGmp = 60.0,
            estimatedListingPrice = 180.0,
            estimatedGainPercent = 50.0,
            lastGmpUpdated = "30-Sep-2026 (Listing Day Discovery)",
            currentSubscriptionTimes = 73.05,
            qibTimes = 148.55,
            niiTimes = 172.16,
            retailTimes = 70.18,
            description = "Manba Finance Ltd is a Mumbai-based non-banking finance company (NBFC) specializing in two-wheeler, three-wheeler, and used car vehicle financing.",
            sector = "Financial Services - NBFC",
            listingExchanges = "BSE, NSE",
            faceValue = 10.0,
            leadManagers = "Hem Securities Ltd",
            registrar = "MUFG Intime India Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "23-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "25-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("Basis of Allotment", "26-Sep-2026", DateStatus.COMPLETED, "Allotment Declared"),
                ImportantDateItem("Listing Date", "30-Sep-2026", DateStatus.ACTIVE, "Listing Today on NSE & BSE")
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "MUFG Intime India Pvt.Ltd.",
                registrarUrl = "https://in.mpms.mufg.com/Initial_Offer/public-issues.html",
                allotmentDate = "26-Sep-2026",
                isAvailable = true,
                note = "Allotment status declared. Check directly on MUFG Intime portal."
            )
        ),

        // =========================================================================
        // 19. RECENTLY LISTED MAINBOARD IPO: KRN Heat Exchanger and Refrigeration Ltd
        // =========================================================================
        IpoItem(
            id = "ipo-krn",
            name = "KRN Heat Exchanger and Refrigeration Ltd",
            symbol = "KRNHEAT",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.LISTED,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            listingStatus = ListingStatus.LISTED,
            priceBandMin = 209.0,
            priceBandMax = 220.0,
            lotSize = 65,
            minInvestment = 14300.0,
            issueSizeCr = 341.95,
            freshIssueCr = 341.95,
            ofsCr = 0.0,
            openDate = "25-Sep-2024",
            closeDate = "27-Sep-2024",
            allotmentDate = "30-Sep-2024",
            listingDate = "03-Oct-2024",
            currentGmp = 0.0,
            listingPrice = 480.0,
            listingGainPercent = 118.18,
            currentMarketPrice = 465.0,
            currentReturnPercent = 111.36,
            lastGmpUpdated = "Listed on NSE / BSE",
            currentSubscriptionTimes = 214.42,
            qibTimes = 253.04,
            niiTimes = 431.63,
            retailTimes = 98.29,
            liveMarketData = LiveMarketData(
                companyName = "KRN Heat Exchanger and Refrigeration Ltd",
                symbol = "KRNHEAT",
                listingPrice = 480.0,
                currentPrice = 465.0,
                change = 245.0,
                changePercent = 111.36,
                volume = 8900000L,
                week52High = 510.0,
                week52Low = 420.0,
                issuePrice = 220.0,
                listingGainLoss = 260.0,
                listingGainLossPercent = 118.18,
                isLive = true,
                lastUpdated = "BSE / NSE Real-time"
            ),
            description = "KRN Heat Exchanger and Refrigeration Ltd manufactures specialized copper and aluminum fins and tubes, condensing coils, and evaporator coils for HVAC systems.",
            sector = "Industrial Equipment & HVAC",
            listingExchanges = "BSE, NSE",
            faceValue = 10.0,
            leadManagers = "Holani Consultants Pvt Ltd",
            registrar = "Bigshare Services Pvt Ltd",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "25-Sep-2024", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "27-Sep-2024", DateStatus.COMPLETED),
                ImportantDateItem("Basis of Allotment", "30-Sep-2024", DateStatus.COMPLETED),
                ImportantDateItem("Listing Date", "03-Oct-2024", DateStatus.COMPLETED, "Listed at ₹480 (+118.2%)")
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "30-Sep-2024",
                isAvailable = true,
                note = "Allotment declared on the Bigshare portal."
            )
        ),

        // =========================================================================
        // 20. RECENTLY LISTED MAINBOARD IPO: Diffusion Engineers Ltd
        // =========================================================================
        IpoItem(
            id = "ipo-diffusion",
            name = "Diffusion Engineers Ltd",
            symbol = "DIFFUSION",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.LISTED,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            listingStatus = ListingStatus.LISTED,
            priceBandMin = 159.0,
            priceBandMax = 168.0,
            lotSize = 88,
            minInvestment = 14784.0,
            issueSizeCr = 158.0,
            freshIssueCr = 158.0,
            ofsCr = 0.0,
            openDate = "26-Sep-2024",
            closeDate = "30-Sep-2024",
            allotmentDate = "01-Oct-2024",
            listingDate = "04-Oct-2024",
            currentGmp = 0.0,
            listingPrice = 188.0,
            listingGainPercent = 11.90,
            currentMarketPrice = 282.50,
            currentReturnPercent = 68.15,
            lastGmpUpdated = "Listed on NSE / BSE",
            currentSubscriptionTimes = 122.30,
            qibTimes = 85.78,
            niiTimes = 207.60,
            retailTimes = 85.61,
            liveMarketData = LiveMarketData(
                companyName = "Diffusion Engineers Ltd",
                symbol = "DIFFUSION",
                listingPrice = 188.0,
                currentPrice = 282.50,
                change = 94.50,
                changePercent = 50.27,
                volume = 4320000L,
                week52High = 320.0,
                week52Low = 180.0,
                issuePrice = 168.0,
                listingGainLoss = 20.0,
                listingGainLossPercent = 11.90,
                isLive = true,
                lastUpdated = "BSE / NSE Real-time"
            ),
            description = "Diffusion Engineers Ltd is an engineering solutions provider engaged in manufacturing specialized welding consumables, wear plates, and heavy machinery components.",
            sector = "Heavy Engineering & Manufacturing",
            listingExchanges = "BSE, NSE",
            faceValue = 10.0,
            leadManagers = "Unistone Capital Pvt Ltd",
            registrar = "Bigshare Services Pvt.Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "26-Sep-2024", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "30-Sep-2024", DateStatus.COMPLETED),
                ImportantDateItem("Basis of Allotment", "01-Oct-2024", DateStatus.COMPLETED),
                ImportantDateItem("Listing Date", "04-Oct-2024", DateStatus.COMPLETED, "Listed at ₹188 (+11.9%)")
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Bigshare Services Pvt.Ltd.",
                registrarUrl = "https://www.bigshareonline.com/ipo_allotment.html",
                allotmentDate = "01-Oct-2024",
                isAvailable = true,
                note = "Allotment declared on Bigshare Services portal."
            )
        ),

        // =========================================================================
        // 21. MEGA LISTED MAINBOARD IPO: Bajaj Housing Finance Ltd
        // =========================================================================
        IpoItem(
            id = "ipo-bajaj-housing",
            name = "Bajaj Housing Finance Ltd",
            symbol = "BAJAJHFL",
            category = IpoCategory.MAINBOARD,
            status = IpoStatus.LISTED,
            allotmentStatus = AllotmentStatus.AVAILABLE,
            listingStatus = ListingStatus.LISTED,
            priceBandMin = 66.0,
            priceBandMax = 70.0,
            lotSize = 214,
            minInvestment = 14980.0,
            issueSizeCr = 6560.0,
            freshIssueCr = 3560.0,
            ofsCr = 3000.0,
            openDate = "09-Sep-2026",
            closeDate = "11-Sep-2026",
            allotmentDate = "12-Sep-2026",
            listingDate = "16-Sep-2026",
            currentGmp = 0.0,
            listingPrice = 150.0,
            listingGainPercent = 114.29,
            currentMarketPrice = 162.80,
            currentReturnPercent = 132.57,
            lastGmpUpdated = "Listed on NSE / BSE",
            currentSubscriptionTimes = 63.61,
            qibTimes = 209.36,
            niiTimes = 41.51,
            retailTimes = 7.02,
            liveMarketData = LiveMarketData(
                companyName = "Bajaj Housing Finance Ltd",
                symbol = "BAJAJHFL",
                listingPrice = 150.0,
                currentPrice = 162.80,
                change = 92.80,
                changePercent = 132.57,
                volume = 68420000L,
                week52High = 188.50,
                week52Low = 145.0,
                issuePrice = 70.0,
                listingGainLoss = 80.0,
                listingGainLossPercent = 114.29,
                isLive = true,
                lastUpdated = "BSE / NSE Real-time"
            ),
            description = "Bajaj Housing Finance is India's second largest housing finance company (HFC) providing home loans, loan against property, and lease rental discounting.",
            sector = "Financial Services - Housing Finance",
            listingExchanges = "BSE, NSE",
            faceValue = 10.0,
            leadManagers = "Kotak Mahindra, BofA Securities, Axis Capital, Goldman Sachs, SBI Capital",
            registrar = "Kfin Technologies Ltd.",
            importantDates = listOf(
                ImportantDateItem("IPO Opens", "09-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("IPO Closes", "11-Sep-2026", DateStatus.COMPLETED),
                ImportantDateItem("Listing Date", "16-Sep-2026", DateStatus.COMPLETED, "Listed at ₹150 (+114.3%)")
            ),
            allotmentInfo = AllotmentInfo(
                registrarName = "Kfin Technologies Ltd.",
                registrarUrl = "https://ipostatus.kfintech.com/",
                allotmentDate = "12-Sep-2026",
                isAvailable = true,
                note = "Allotment declared on KFintech portal."
            )
        )
    )
}
