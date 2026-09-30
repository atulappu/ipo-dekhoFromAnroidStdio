-- =============================================
-- Database: IpoDekhoDb
-- SQL Server Schema for Real-Time IPO Ingestion Pipeline
-- Compliant with IPODekho Data Correctness Specification
-- =============================================

USE IpoDekhoDb;
GO

-- 1. Data Sources Registry Table
IF OBJECT_ID('dbo.DataSources', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.DataSources (
        DataSourceId NVARCHAR(50) PRIMARY KEY,
        SourceName NVARCHAR(100) NOT NULL,
        SourceType NVARCHAR(50) NOT NULL, -- EXCHANGE, GMP_AGGREGATOR, DIRECTORY, REGISTRAR
        BaseUrl NVARCHAR(500) NOT NULL,
        Endpoint NVARCHAR(500) NOT NULL,
        DataCategory NVARCHAR(50) NOT NULL, -- SCHEDULE, SUBSCRIPTION, GMP, ALLOTMENT
        MarketType NVARCHAR(50) NOT NULL DEFAULT 'ALL', -- MAINBOARD, SME, ALL
        Priority INT NOT NULL DEFAULT 1,
        IsActive BIT NOT NULL DEFAULT 1,
        RequiresAuthentication BIT NOT NULL DEFAULT 0,
        AuthType NVARCHAR(50) NULL DEFAULT 'NONE',
        PollingIntervalMinutes INT NOT NULL DEFAULT 15,
        LastSuccessfulFetch DATETIMEOFFSET NULL,
        LastFailedFetch DATETIMEOFFSET NULL,
        LastHttpStatus INT NULL,
        LastError NVARCHAR(MAX) NULL,
        CreatedDate DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
        ModifiedDate DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
END
GO

-- 2. Raw Source Responses Table (Data Provenance & Audit)
IF OBJECT_ID('dbo.RawSourceResponses', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.RawSourceResponses (
        RawSourceResponseId BIGINT IDENTITY(1,1) PRIMARY KEY,
        DataSourceId NVARCHAR(50) NOT NULL FOREIGN KEY REFERENCES dbo.DataSources(DataSourceId),
        RequestUrl NVARCHAR(1000) NOT NULL,
        RequestTime DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
        HttpStatus INT NOT NULL,
        ResponseHeadersSummary NVARCHAR(MAX) NULL,
        ResponseBody NVARCHAR(MAX) NULL,
        ContentHash NVARCHAR(128) NOT NULL,
        FetchedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
        IsSuccess BIT NOT NULL,
        ErrorMessage NVARCHAR(MAX) NULL
    );
    CREATE INDEX IX_RawSourceResponses_DataSource_Time ON dbo.RawSourceResponses(DataSourceId, FetchedAt DESC);
END
GO

-- 3. Master IPO Table
IF OBJECT_ID('dbo.Ipos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Ipos (
        Id NVARCHAR(100) PRIMARY KEY,
        Name NVARCHAR(250) NOT NULL,
        NormalizedName NVARCHAR(250) NOT NULL,
        Symbol NVARCHAR(50) NULL,
        Isin NVARCHAR(20) NULL,
        Type NVARCHAR(20) NOT NULL DEFAULT 'MAINBOARD', -- MAINBOARD / SME
        Status NVARCHAR(30) NOT NULL DEFAULT 'UPCOMING', -- UPCOMING / OPEN / CLOSED / ALLOTMENT_PENDING / ALLOTMENT_AVAILABLE / LISTED
        Exchange NVARCHAR(50) NOT NULL DEFAULT 'BSE, NSE',
        PriceBandMin DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
        PriceBandMax DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
        LotSize INT NOT NULL DEFAULT 1,
        IssueSizeCr DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
        FreshIssueCr DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
        OfsCr DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
        OpenDate DATETIMEOFFSET NULL,
        CloseDate DATETIMEOFFSET NULL,
        AllotmentDate DATETIMEOFFSET NULL,
        ListingDate DATETIMEOFFSET NULL,
        ListingPrice DECIMAL(18, 2) NULL,
        ListingGainPercent DECIMAL(8, 2) NULL,
        CurrentMarketPrice DECIMAL(18, 2) NULL,
        CurrentReturnPercent DECIMAL(8, 2) NULL,
        CurrentGmp DECIMAL(18, 2) NULL, -- NULLABLE: NULL indicates GMP is unavailable; never silently default to 0!
        EstimatedListingPrice DECIMAL(18, 2) NULL,
        EstimatedGainPercent DECIMAL(8, 2) NULL,
        GmpSource NVARCHAR(50) NULL,
        GmpFetchedAt DATETIMEOFFSET NULL,
        FireRating INT NOT NULL DEFAULT 0,
        RegistrarName NVARCHAR(200) NULL,
        RegistrarUrl NVARCHAR(500) NULL,
        IsAllotmentOut BIT NOT NULL DEFAULT 0,
        AllotmentStatus NVARCHAR(30) NOT NULL DEFAULT 'WAITING',
        ValidationStatus NVARCHAR(30) NOT NULL DEFAULT 'VALIDATED', -- VALIDATED, QUARANTINED
        CreatedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
        UpdatedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
    CREATE INDEX IX_Ipos_Status ON dbo.Ipos(Status);
    CREATE INDEX IX_Ipos_NormalizedName ON dbo.Ipos(NormalizedName);
    CREATE INDEX IX_Ipos_Symbol ON dbo.Ipos(Symbol);
END
GO

-- 4. External Identifiers for Master Reconciliation
IF OBJECT_ID('dbo.IPOExternalIdentifiers', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.IPOExternalIdentifiers (
        IPOExternalIdentifierId BIGINT IDENTITY(1,1) PRIMARY KEY,
        IPOId NVARCHAR(100) NOT NULL FOREIGN KEY REFERENCES dbo.Ipos(Id) ON DELETE CASCADE,
        DataSourceId NVARCHAR(50) NOT NULL FOREIGN KEY REFERENCES dbo.DataSources(DataSourceId),
        ExternalId NVARCHAR(150) NOT NULL,
        Symbol NVARCHAR(50) NULL,
        ISIN NVARCHAR(20) NULL,
        ExternalName NVARCHAR(250) NOT NULL,
        ExternalOpenDate DATETIMEOFFSET NULL,
        ExternalCloseDate DATETIMEOFFSET NULL,
        LastSeenAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
    CREATE UNIQUE INDEX UQ_IPOExternalIdentifiers ON dbo.IPOExternalIdentifiers(DataSourceId, ExternalId);
END
GO

-- 5. Validation Errors Log (Quarantine)
IF OBJECT_ID('dbo.IpoValidationErrors', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.IpoValidationErrors (
        ValidationErrorId BIGINT IDENTITY(1,1) PRIMARY KEY,
        RawSourceResponseId BIGINT NULL FOREIGN KEY REFERENCES dbo.RawSourceResponses(RawSourceResponseId),
        IPOId NVARCHAR(100) NULL,
        FieldName NVARCHAR(100) NOT NULL,
        RawValue NVARCHAR(MAX) NULL,
        ValidationRule NVARCHAR(200) NOT NULL,
        ErrorMessage NVARCHAR(500) NOT NULL,
        LoggedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
END
GO

-- 6. GMP Historical Ticks Table
IF OBJECT_ID('dbo.GmpHistory', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.GmpHistory (
        Id BIGINT IDENTITY(1,1) PRIMARY KEY,
        IpoId NVARCHAR(100) NOT NULL FOREIGN KEY REFERENCES dbo.Ipos(Id) ON DELETE CASCADE,
        Gmp DECIMAL(18, 2) NOT NULL,
        GainPercent DECIMAL(8, 2) NOT NULL,
        Source NVARCHAR(50) NOT NULL DEFAULT 'InvestorGain',
        RecordedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
END
GO

-- 7. Live Subscription Tallies Table (BSE / NSE Bidding)
IF OBJECT_ID('dbo.Subscriptions', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Subscriptions (
        Id BIGINT IDENTITY(1,1) PRIMARY KEY,
        IpoId NVARCHAR(100) NOT NULL UNIQUE FOREIGN KEY REFERENCES dbo.Ipos(Id) ON DELETE CASCADE,
        Qib DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
        Nii DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
        Retail DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
        Employee DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
        Total DECIMAL(10, 2) NOT NULL DEFAULT 0.0,
        ApplicationsCount BIGINT NOT NULL DEFAULT 0,
        LastUpdated DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
END
GO

-- 8. Registrars / RTAs Master Table
IF OBJECT_ID('dbo.Registrars', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Registrars (
        Id NVARCHAR(100) PRIMARY KEY,
        Name NVARCHAR(250) NOT NULL,
        Url NVARCHAR(500) NOT NULL,
        IssuesManaged INT NOT NULL DEFAULT 0,
        IssueAmountCr DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
        CreatedDate DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
        ModifyDate DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
        ModifyBy NVARCHAR(100) NOT NULL DEFAULT 'Admin',
        Comment NVARCHAR(MAX) NULL,
        IsActive BIT NOT NULL DEFAULT 1
    );
END
GO

-- 9. Admin Notices & Announcements Table
IF OBJECT_ID('dbo.AdminNotices', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.AdminNotices (
        Id NVARCHAR(100) PRIMARY KEY,
        Title NVARCHAR(300) NOT NULL,
        Message NVARCHAR(MAX) NOT NULL,
        Category NVARCHAR(50) NOT NULL DEFAULT 'ANNOUNCEMENT',
        TargetIpoId NVARCHAR(100) NULL FOREIGN KEY REFERENCES dbo.Ipos(Id) ON DELETE SET NULL,
        IsActive BIT NOT NULL DEFAULT 1,
        CreatedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
END
GO

-- 10. Notifications Table (Idempotent Event Dispatch)
IF OBJECT_ID('dbo.Notifications', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.Notifications (
        NotificationId BIGINT IDENTITY(1,1) PRIMARY KEY,
        UserId NVARCHAR(100) NULL,
        IPOId NVARCHAR(100) NOT NULL FOREIGN KEY REFERENCES dbo.Ipos(Id) ON DELETE CASCADE,
        Type NVARCHAR(50) NOT NULL DEFAULT 'ALLOTMENT_AVAILABLE',
        Title NVARCHAR(300) NOT NULL,
        Message NVARCHAR(1000) NOT NULL,
        EventKey NVARCHAR(150) NOT NULL,
        IsRead BIT NOT NULL DEFAULT 0,
        CreatedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
    );
    CREATE UNIQUE INDEX UQ_Notifications_EventKey ON dbo.Notifications(EventKey);
END
GO

-- Seed Sources Registry
MERGE dbo.DataSources AS target
USING (VALUES
    ('SRC_NSE_LIVE', 'National Stock Exchange of India', 'EXCHANGE', 'https://www.nseindia.com', '/market-data/all-upcoming-issues-ipo', 'SCHEDULE_AND_SUBSCRIPTION', 'ALL', 1, 1, 1, 'COOKIE_HANDSHAKE', 15),
    ('SRC_BSE_PUBLIC', 'Bombay Stock Exchange Limited', 'EXCHANGE', 'https://www.bseindia.com', '/markets/publicissues/ipoissues.aspx?id=1&type=pso', 'SCHEDULE', 'ALL', 1, 1, 0, 'NONE', 15),
    ('SRC_INVESTORGAIN_GMP', 'InvestorGain Live GMP Tracker', 'GMP_AGGREGATOR', 'https://www.investorgain.com', '/report/live-ipo-gmp/331/', 'GMP_AND_SENTIMENT', 'ALL', 1, 1, 0, 'NONE', 5),
    ('SRC_CHITTORGARH_DIR', 'Chittorgarh IPO Directory', 'DIRECTORY', 'https://www.chittorgarh.com', '/report/ipo-in-india-list-main-board-sme/82/', 'DIRECTORY_AND_FINANCIALS', 'ALL', 2, 1, 0, 'NONE', 30)
) AS source (DataSourceId, SourceName, SourceType, BaseUrl, Endpoint, DataCategory, MarketType, Priority, IsActive, RequiresAuthentication, AuthType, PollingIntervalMinutes)
ON target.DataSourceId = source.DataSourceId
WHEN NOT MATCHED THEN
    INSERT (DataSourceId, SourceName, SourceType, BaseUrl, Endpoint, DataCategory, MarketType, Priority, IsActive, RequiresAuthentication, AuthType, PollingIntervalMinutes)
    VALUES (source.DataSourceId, source.SourceName, source.SourceType, source.BaseUrl, source.Endpoint, source.DataCategory, source.MarketType, source.Priority, source.IsActive, source.RequiresAuthentication, source.AuthType, source.PollingIntervalMinutes);
GO

-- Seed 12 Official RTAs
MERGE dbo.Registrars AS target
USING (VALUES
    ('kfin-tech', 'Kfin Technologies Ltd.', 'https://ipostatus.kfintech.com/', 82, 53478.51, 'Major registrar for Mainboard and SME IPOs'),
    ('bigshare-services', 'Bigshare Services Pvt.Ltd.', 'https://www.bigshareonline.com/ipo_allotment.html', 59, 7558.67, 'Leading registrar for SME and Mainboard issues'),
    ('mufg-intime', 'MUFG Intime India Pvt.Ltd.', 'https://in.mpms.mufg.com/Initial_Offer/public-issues.html', 54, 56269.35, 'Formerly Link Intime India Pvt Ltd, premier RTA'),
    ('maashitla-sec', 'Maashitla Securities Pvt.Ltd.', 'https://maashitla.com/allotment-status/public-issues', 32, 1186.40, 'Popular SME IPO registrar'),
    ('skyline-financial', 'Skyline Financial Services Pvt.Ltd.', 'https://www.skylinerta.com/display_ipo_rightissue_allotment.php', 17, 578.23, 'SME & Mainboard registrar'),
    ('purva-sharegistry', 'Purva Sharegistry (India) Pvt.Ltd.', 'https://www.purvashare.com/investor-service/ipo-query', 8, 683.62, 'RTA service provider'),
    ('cameo-corporate', 'Cameo Corporate Services Ltd.', 'https://ipo.cameoindia.com/', 8, 505.16, 'South India established RTA'),
    ('integrated-registry', 'Integrated Registry Management Services Pvt.Ltd.', 'https://www.integratedregistry.in/IRMS_V2/IPO.aspx', 5, 226.31, 'Financial services and registry'),
    ('mas-services', 'MAS Services Ltd.', 'https://www.masserv.com/opt.asp', 4, 255.89, 'RTA and corporate services'),
    ('mudra-rta', 'Mudra RTA Ventures Private Limited', 'https://mudrarta.com/display_ipo_rightissue_allotment.php', 4, 181.69, 'Specialized SME RTA'),
    ('alankit-assignments', 'Alankit Assignments Ltd.', 'https://ipo.alankit.com/', 1, 38.45, 'National registry & citizen services'),
    ('abhipra-capital', 'Abhipra Capital Limited', 'https://www.abhipra.com/ipo-status', 1, 31.48, 'Capital market intermediary and registrar')
) AS source (Id, Name, Url, IssuesManaged, IssueAmountCr, Comment)
ON target.Id = source.Id
WHEN NOT MATCHED THEN
    INSERT (Id, Name, Url, IssuesManaged, IssueAmountCr, Comment, ModifyBy)
    VALUES (source.Id, source.Name, source.Url, source.IssuesManaged, source.IssueAmountCr, source.Comment, 'Admin');
GO
