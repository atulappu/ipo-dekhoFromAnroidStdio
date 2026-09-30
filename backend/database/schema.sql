-- =============================================
-- Database: IpoDekhoDb
-- SQL Server Schema for Real-Time IPO Tracker
-- =============================================

CREATE DATABASE IpoDekhoDb;
GO

USE IpoDekhoDb;
GO

-- 1. Main IPO Master Table
CREATE TABLE dbo.Ipos (
    Id NVARCHAR(100) PRIMARY KEY,
    Name NVARCHAR(250) NOT NULL,
    Symbol NVARCHAR(50) NULL,
    Type NVARCHAR(20) NOT NULL DEFAULT 'MAINBOARD', -- MAINBOARD / SME
    Status NVARCHAR(30) NOT NULL DEFAULT 'UPCOMING', -- UPCOMING / OPEN / CLOSED / LISTED / ALLOTMENT_AVAILABLE
    PriceBandMin DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
    PriceBandMax DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
    LotSize INT NOT NULL DEFAULT 1,
    IssueSizeCr DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
    CurrentGmp DECIMAL(18, 2) NOT NULL DEFAULT 0.0,
    OpenDate DATETIMEOFFSET NULL,
    CloseDate DATETIMEOFFSET NULL,
    AllotmentDate DATETIMEOFFSET NULL,
    ListingDate DATETIMEOFFSET NULL,
    ListingPrice DECIMAL(18, 2) NULL,
    ListingGainPercent DECIMAL(8, 2) NULL,
    RegistrarName NVARCHAR(200) NULL,
    RegistrarUrl NVARCHAR(500) NULL,
    IsAllotmentOut BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET(),
    UpdatedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
);
GO

-- 2. GMP Historical Ticks Table
CREATE TABLE dbo.GmpHistory (
    Id BIGINT IDENTITY(1,1) PRIMARY KEY,
    IpoId NVARCHAR(100) NOT NULL FOREIGN KEY REFERENCES dbo.Ipos(Id) ON DELETE CASCADE,
    Gmp DECIMAL(18, 2) NOT NULL,
    GainPercent DECIMAL(8, 2) NOT NULL,
    RecordedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
);
GO

-- 3. Live Subscription Tallies Table (BSE / NSE Bidding)
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
GO

-- 4. Admin Notices & Announcements Table (Push broadcast)
CREATE TABLE dbo.AdminNotices (
    Id NVARCHAR(100) PRIMARY KEY,
    Title NVARCHAR(300) NOT NULL,
    Message NVARCHAR(MAX) NOT NULL,
    Category NVARCHAR(50) NOT NULL DEFAULT 'ANNOUNCEMENT', -- ANNOUNCEMENT / MARKET_NEWS / ALERT
    TargetIpoId NVARCHAR(100) NULL FOREIGN KEY REFERENCES dbo.Ipos(Id) ON DELETE SET NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIMEOFFSET NOT NULL DEFAULT SYSDATETIMEOFFSET()
);
GO

-- Real-World Initial Data Seed
INSERT INTO dbo.Ipos (Id, Name, Symbol, Type, Status, PriceBandMin, PriceBandMax, LotSize, IssueSizeCr, CurrentGmp, OpenDate, CloseDate, AllotmentDate, ListingDate, RegistrarName, RegistrarUrl, IsAllotmentOut)
VALUES 
('srit-india', 'SRIT India Ltd', 'SRIT', 'MAINBOARD', 'OPEN', 123.0, 130.0, 115, 218.40, 28.0, '2026-09-28', '2026-09-30', '2026-10-01', '2026-10-06', 'Link Intime India Pvt Ltd', 'https://in.mpms.mufg.com/Initial_Offer/public-issues.html', 0),
('diffusion-engineers', 'Diffusion Engineers Ltd', 'DIFFUSION', 'MAINBOARD', 'OPEN', 159.0, 168.0, 88, 158.00, 58.0, '2026-09-26', '2026-09-30', '2026-10-01', '2026-10-04', 'Bigshare Services Pvt Ltd', 'https://www.bigshareonline.com/ipo_allotment.html', 0),
('krn-heat', 'KRN Heat Exchanger and Refrigeration Ltd', 'KRNHEAT', 'MAINBOARD', 'ALLOTMENT_PENDING', 209.0, 220.0, 65, 341.95, 235.0, '2026-09-25', '2026-09-27', '2026-09-30', '2026-10-03', 'Bigshare Services Pvt Ltd', 'https://www.bigshareonline.com/ipo_allotment.html', 0),
('manba-finance', 'Manba Finance Ltd', 'MANBA', 'MAINBOARD', 'ALLOTMENT_AVAILABLE', 114.0, 120.0, 125, 150.84, 60.0, '2026-09-23', '2026-09-25', '2026-09-26', '2026-09-30', 'Link Intime India Pvt Ltd', 'https://in.mpms.mufg.com/Initial_Offer/public-issues.html', 1);

-- 5. Registrars / RTAs Master Table with Admin Editable URLs and Statistics
-- Fields: Name, URL, Issues Managed, Issue Amount in Cr, Created Date, Modify Date, Modify By, Comment
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
GO

-- Seed Official RTAs / Registrars with Verified Allotment URLs
INSERT INTO dbo.Registrars (Id, Name, Url, IssuesManaged, IssueAmountCr, ModifyBy, Comment)
VALUES
('kfin-tech', 'Kfin Technologies Ltd.', 'https://ipostatus.kfintech.com/', 82, 53478.51, 'Admin', 'Major registrar for Mainboard and SME IPOs'),
('bigshare-services', 'Bigshare Services Pvt.Ltd.', 'https://www.bigshareonline.com/ipo_allotment.html', 59, 7558.67, 'Admin', 'Leading registrar for SME and Mainboard issues'),
('mufg-intime', 'MUFG Intime India Pvt.Ltd.', 'https://in.mpms.mufg.com/Initial_Offer/public-issues.html', 54, 56269.35, 'Admin', 'Formerly Link Intime India Pvt Ltd, premier RTA'),
('maashitla-sec', 'Maashitla Securities Pvt.Ltd.', 'https://maashitla.com/allotment-status/public-issues', 32, 1186.40, 'Admin', 'Popular SME IPO registrar'),
('skyline-financial', 'Skyline Financial Services Pvt.Ltd.', 'https://www.skylinerta.com/display_ipo_rightissue_allotment.php', 17, 578.23, 'Admin', 'SME & Mainboard registrar'),
('purva-sharegistry', 'Purva Sharegistry (India) Pvt.Ltd.', 'https://www.purvashare.com/investor-service/ipo-query', 8, 683.62, 'Admin', 'RTA service provider'),
('cameo-corporate', 'Cameo Corporate Services Ltd.', 'https://ipo.cameoindia.com/', 8, 505.16, 'Admin', 'South India established RTA'),
('integrated-registry', 'Integrated Registry Management Services Pvt.Ltd.', 'https://www.integratedregistry.in/IRMS_V2/IPO.aspx', 5, 226.31, 'Admin', 'Financial services and registry'),
('mas-services', 'MAS Services Ltd.', 'https://www.masserv.com/opt.asp', 4, 255.89, 'Admin', 'RTA and corporate services'),
('mudra-rta', 'Mudra RTA Ventures Private Limited', 'https://mudrarta.com/display_ipo_rightissue_allotment.php', 4, 181.69, 'Admin', 'Specialized SME RTA'),
('alankit-assignments', 'Alankit Assignments Ltd.', 'https://ipo.alankit.com/', 1, 38.45, 'Admin', 'National registry & citizen services'),
('abhipra-capital', 'Abhipra Capital Limited', 'https://www.abhipra.com/ipo-status', 1, 31.48, 'Admin', 'Capital market intermediary and registrar');
GO
