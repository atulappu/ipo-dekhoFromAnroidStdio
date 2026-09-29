-- ====================================================================
-- IPODekho: MS SQL Server Database Schema (Production Ready)
-- Database: IpoDekhoDb
-- Compatibility: Microsoft SQL Server 2019 / 2022 / Azure SQL Database
-- ====================================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'IpoDekhoDb')
BEGIN
    CREATE DATABASE [IpoDekhoDb];
END
GO

USE [IpoDekhoDb];
GO

-- 1. Table: Ipos (Master IPO Records)
IF OBJECT_ID(N'[dbo].[Ipos]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Ipos] (
        [Id] NVARCHAR(64) NOT NULL PRIMARY KEY,
        [Name] NVARCHAR(256) NOT NULL,
        [Symbol] NVARCHAR(32) NOT NULL,
        [Category] NVARCHAR(32) NOT NULL DEFAULT 'MAINBOARD', -- MAINBOARD | SME
        [Status] NVARCHAR(32) NOT NULL DEFAULT 'UPCOMING',    -- OPEN | UPCOMING | CLOSED | LISTED
        [PriceBandMin] DECIMAL(18, 2) NULL,
        [PriceBandMax] DECIMAL(18, 2) NULL,
        [LotSize] INT NULL,
        [MinInvestment] DECIMAL(18, 2) NULL,
        [IssueSizeCr] DECIMAL(18, 2) NULL,
        [FreshIssueCr] DECIMAL(18, 2) NULL,
        [OfsCr] DECIMAL(18, 2) NULL,
        [OpenDate] DATE NULL,
        [CloseDate] DATE NULL,
        [AllotmentDate] DATE NULL,
        [ListingDate] DATE NULL,
        [CurrentGmp] DECIMAL(18, 2) NULL,
        [EstimatedListingPrice] DECIMAL(18, 2) NULL,
        [EstimatedGainPercent] DECIMAL(18, 2) NULL,
        [LastGmpUpdated] DATETIME2 NULL,
        [CurrentSubscriptionTimes] DECIMAL(18, 2) NULL,
        [QibTimes] DECIMAL(18, 2) NULL,
        [NiiTimes] DECIMAL(18, 2) NULL,
        [RetailTimes] DECIMAL(18, 2) NULL,
        [ListingPrice] DECIMAL(18, 2) NULL,
        [ListingGainPercent] DECIMAL(18, 2) NULL,
        [CurrentMarketPrice] DECIMAL(18, 2) NULL,
        [CurrentReturnPercent] DECIMAL(18, 2) NULL,
        [Description] NVARCHAR(MAX) NULL,
        [Sector] NVARCHAR(128) NULL,
        [ListingExchanges] NVARCHAR(64) NULL,
        [FaceValue] DECIMAL(18, 2) NULL,
        [LeadManagers] NVARCHAR(MAX) NULL,
        [Registrar] NVARCHAR(256) NULL,
        [PromoterHoldingPre] DECIMAL(18, 2) NULL,
        [PromoterHoldingPost] DECIMAL(18, 2) NULL,
        [CreatedAt] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [UpdatedAt] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Ipos_Status' AND object_id = OBJECT_ID('dbo.Ipos'))
        CREATE NONCLUSTERED INDEX IX_Ipos_Status ON [dbo].[Ipos]([Status]);
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Ipos_Category' AND object_id = OBJECT_ID('dbo.Ipos'))
        CREATE NONCLUSTERED INDEX IX_Ipos_Category ON [dbo].[Ipos]([Category]);
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Ipos_OpenDate' AND object_id = OBJECT_ID('dbo.Ipos'))
        CREATE NONCLUSTERED INDEX IX_Ipos_OpenDate ON [dbo].[Ipos]([OpenDate] DESC);
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Ipos_Symbol' AND object_id = OBJECT_ID('dbo.Ipos'))
        CREATE NONCLUSTERED INDEX IX_Ipos_Symbol ON [dbo].[Ipos]([Symbol]);
END
GO

-- 2. Table: GmpHistories (Grey Market Premium Snapshots)
IF OBJECT_ID(N'[dbo].[GmpHistories]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[GmpHistories] (
        [Id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [IpoId] NVARCHAR(64) NOT NULL,
        [Date] NVARCHAR(32) NOT NULL,
        [GmpAmount] DECIMAL(18, 2) NOT NULL,
        [EstimatedPrice] DECIMAL(18, 2) NULL,
        [EstimatedGainPercent] DECIMAL(18, 2) NULL,
        [Rating] NVARCHAR(32) NULL,
        [RecordedAt] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_GmpHistories_Ipos FOREIGN KEY ([IpoId]) REFERENCES [dbo].[Ipos]([Id]) ON DELETE CASCADE
    );

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_GmpHistories_IpoId' AND object_id = OBJECT_ID('dbo.GmpHistories'))
        CREATE NONCLUSTERED INDEX IX_GmpHistories_IpoId ON [dbo].[GmpHistories]([IpoId]);
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_GmpHistories_Date' AND object_id = OBJECT_ID('dbo.GmpHistories'))
        CREATE NONCLUSTERED INDEX IX_GmpHistories_Date ON [dbo].[GmpHistories]([Date]);
END
GO

-- 3. Table: Subscriptions (Day-wise Bidding Updates)
IF OBJECT_ID(N'[dbo].[Subscriptions]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Subscriptions] (
        [Id] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [IpoId] NVARCHAR(64) NOT NULL,
        [DayLabel] NVARCHAR(32) NOT NULL, -- e.g. Day 1, Day 2, Day 3 (Final)
        [Date] NVARCHAR(32) NOT NULL,
        [QibTimes] DECIMAL(18, 2) NOT NULL,
        [NiiTimes] DECIMAL(18, 2) NOT NULL,
        [RetailTimes] DECIMAL(18, 2) NOT NULL,
        [TotalTimes] DECIMAL(18, 2) NOT NULL,
        [RecordedAt] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_Subscriptions_Ipos FOREIGN KEY ([IpoId]) REFERENCES [dbo].[Ipos]([Id]) ON DELETE CASCADE
    );

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_Subscriptions_IpoId' AND object_id = OBJECT_ID('dbo.Subscriptions'))
        CREATE NONCLUSTERED INDEX IX_Subscriptions_IpoId ON [dbo].[Subscriptions]([IpoId]);
END
GO

-- 4. Table: MarketIndices (Nifty 50, Sensex, Nifty Smallcap)
IF OBJECT_ID(N'[dbo].[MarketIndices]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[MarketIndices] (
        [Symbol] NVARCHAR(32) NOT NULL PRIMARY KEY,
        [Name] NVARCHAR(64) NOT NULL,
        [CurrentValue] DECIMAL(18, 2) NOT NULL,
        [ChangeValue] DECIMAL(18, 2) NOT NULL,
        [ChangePercent] DECIMAL(18, 2) NOT NULL,
        [LastUpdated] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- Seed Baseline Benchmark Indices
MERGE INTO [dbo].[MarketIndices] AS target
USING (VALUES 
    (N'NIFTY 50', N'NIFTY 50', 25350.20, 142.60, 0.57),
    (N'SENSEX', N'BSE SENSEX', 82980.50, 480.10, 0.58),
    (N'NIFTY BANK', N'BANK NIFTY', 52140.80, 210.30, 0.41),
    (N'NIFTY SMALLCAP', N'NIFTY SMALLCAP 250', 18320.15, 95.80, 0.53)
) AS source ([Symbol], [Name], [CurrentValue], [ChangeValue], [ChangePercent])
ON (target.[Symbol] = source.[Symbol])
WHEN MATCHED THEN
    UPDATE SET 
        target.[CurrentValue] = source.[CurrentValue],
        target.[ChangeValue] = source.[ChangeValue],
        target.[ChangePercent] = source.[ChangePercent],
        target.[LastUpdated] = SYSUTCDATETIME()
WHEN NOT MATCHED THEN
    INSERT ([Symbol], [Name], [CurrentValue], [ChangeValue], [ChangePercent], [LastUpdated])
    VALUES (source.[Symbol], source.[Name], source.[CurrentValue], source.[ChangeValue], source.[ChangePercent], SYSUTCDATETIME());
GO
