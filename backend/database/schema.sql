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

-- Sample Initial Data Seed
INSERT INTO dbo.Ipos (Id, Name, Symbol, Type, Status, PriceBandMin, PriceBandMax, LotSize, IssueSizeCr, CurrentGmp, OpenDate, CloseDate, AllotmentDate, ListingDate, RegistrarName, RegistrarUrl, IsAllotmentOut)
VALUES 
('solar-tech', 'Solaris Clean Energy Tech Ltd', 'SOLARIS', 'MAINBOARD', 'OPEN', 215.0, 228.0, 65, 850.0, 48.0, DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), DATEADD(day, 4, GETDATE()), DATEADD(day, 6, GETDATE()), 'Link Intime India Pvt Ltd', 'https://linkintime.co.in/initial_offer/public-issues.html', 0),
('apex-robotics', 'Apex Robotics India Ltd', 'APEXROBO', 'MAINBOARD', 'ALLOTMENT_AVAILABLE', 450.0, 475.0, 30, 1200.0, 125.0, DATEADD(day, -5, GETDATE()), DATEADD(day, -3, GETDATE()), DATEADD(day, -1, GETDATE()), DATEADD(day, 2, GETDATE()), 'KFin Technologies Ltd', 'https://kosmic.kfintech.com/ipostatus', 1),
('nexgen-ai', 'NexGen AI Cloud Technologies', 'NEXGEN', 'SME', 'UPCOMING', 95.0, 102.0, 1200, 45.0, 28.0, DATEADD(day, 3, GETDATE()), DATEADD(day, 6, GETDATE()), DATEADD(day, 8, GETDATE()), DATEADD(day, 11, GETDATE()), 'Bigshare Services Pvt Ltd', 'https://www.bigshareonline.com/ipo_Allotment.html', 0);
GO
