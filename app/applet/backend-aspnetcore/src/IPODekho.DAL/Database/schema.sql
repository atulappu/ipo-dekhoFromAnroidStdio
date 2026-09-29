-- ====================================================================
-- IPODekho Enterprise MS SQL Server Database Schema (Database-First)
-- Database: IPODekho
-- ====================================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'IPODekho')
BEGIN
    CREATE DATABASE [IPODekho];
END
GO

USE [IPODekho];
GO

-- 1. Roles Table
IF OBJECT_ID(N'[dbo].[Roles]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Roles] (
        [RoleId] INT IDENTITY(1,1) PRIMARY KEY,
        [RoleName] NVARCHAR(50) NOT NULL UNIQUE,
        [Description] NVARCHAR(250) NULL,
        [CreatedDate] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
END
GO

-- 2. Users Table
IF OBJECT_ID(N'[dbo].[Users]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Users] (
        [UserId] INT IDENTITY(1,1) PRIMARY KEY,
        [Username] NVARCHAR(100) NOT NULL UNIQUE,
        [Email] NVARCHAR(150) NOT NULL UNIQUE,
        [PasswordHash] NVARCHAR(256) NOT NULL,
        [FullName] NVARCHAR(150) NOT NULL,
        [PhoneNumber] NVARCHAR(20) NULL,
        [IsActive] BIT NOT NULL DEFAULT 1,
        [CreatedDate] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [ModifiedDate] DATETIME2 NULL
    );
END
GO

-- 3. UserRoles Mapping Table
IF OBJECT_ID(N'[dbo].[UserRoles]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[UserRoles] (
        [UserRoleId] INT IDENTITY(1,1) PRIMARY KEY,
        [UserId] INT NOT NULL,
        [RoleId] INT NOT NULL,
        [AssignedDate] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_UserRoles_Users FOREIGN KEY ([UserId]) REFERENCES [dbo].[Users]([UserId]) ON DELETE CASCADE,
        CONSTRAINT FK_UserRoles_Roles FOREIGN KEY ([RoleId]) REFERENCES [dbo].[Roles]([RoleId]) ON DELETE CASCADE,
        CONSTRAINT UQ_UserRoles UNIQUE ([UserId], [RoleId])
    );
END
GO

-- 4. Companies Table
IF OBJECT_ID(N'[dbo].[Companies]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Companies] (
        [CompanyId] INT IDENTITY(1,1) PRIMARY KEY,
        [CompanyName] NVARCHAR(250) NOT NULL,
        [Industry] NVARCHAR(150) NULL,
        [Website] NVARCHAR(250) NULL,
        [IncorporationYear] INT NULL,
        [ManagingDirector] NVARCHAR(150) NULL,
        [RegisteredAddress] NVARCHAR(MAX) NULL
    );
END
GO

-- 5. Registrars Table
IF OBJECT_ID(N'[dbo].[Registrars]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Registrars] (
        [RegistrarId] INT IDENTITY(1,1) PRIMARY KEY,
        [RegistrarName] NVARCHAR(200) NOT NULL,
        [PortalUrl] NVARCHAR(250) NULL,
        [Phone] NVARCHAR(50) NULL,
        [Email] NVARCHAR(100) NULL
    );
END
GO

-- 6. LeadManagers Table
IF OBJECT_ID(N'[dbo].[LeadManagers]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[LeadManagers] (
        [LeadManagerId] INT IDENTITY(1,1) PRIMARY KEY,
        [ManagerName] NVARCHAR(200) NOT NULL,
        [Website] NVARCHAR(250) NULL
    );
END
GO

-- 7. IPO Categories Table
IF OBJECT_ID(N'[dbo].[IPOCategories]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[IPOCategories] (
        [CategoryId] INT IDENTITY(1,1) PRIMARY KEY,
        [CategoryCode] NVARCHAR(50) NOT NULL UNIQUE, -- MAINBOARD | SME
        [CategoryName] NVARCHAR(100) NOT NULL
    );
END
GO

-- 8. Master IPOs Table
IF OBJECT_ID(N'[dbo].[IPOs]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[IPOs] (
        [IPOId] INT IDENTITY(1,1) PRIMARY KEY,
        [CompanyId] INT NULL,
        [IPOName] NVARCHAR(250) NOT NULL,
        [Symbol] NVARCHAR(50) NOT NULL UNIQUE,
        [MarketType] NVARCHAR(20) NOT NULL DEFAULT 'MAINBOARD', -- MAINBOARD | SME
        [Status] NVARCHAR(20) NOT NULL DEFAULT 'UPCOMING',     -- OPEN | UPCOMING | CLOSED | LISTED
        [PriceBandMin] DECIMAL(18, 2) NULL,
        [PriceBandMax] DECIMAL(18, 2) NULL,
        [LotSize] INT NULL,
        [IssueSize] DECIMAL(18, 2) NULL, -- in Crores
        [FreshIssue] DECIMAL(18, 2) NULL,
        [OfferForSale] DECIMAL(18, 2) NULL,
        [FaceValue] DECIMAL(18, 2) NULL,
        [OpenDate] DATE NULL,
        [CloseDate] DATE NULL,
        [AllotmentDate] DATE NULL,
        [ListingDate] DATE NULL,
        [ListingPrice] DECIMAL(18, 2) NULL,
        [CurrentMarketPrice] DECIMAL(18, 2) NULL,
        [RegistrarId] INT NULL,
        [ListingExchanges] NVARCHAR(50) NULL DEFAULT 'NSE, BSE',
        [Description] NVARCHAR(MAX) NULL,
        [IsActive] BIT NOT NULL DEFAULT 1,
        [CreatedDate] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [ModifiedDate] DATETIME2 NULL,
        CONSTRAINT FK_IPOs_Companies FOREIGN KEY ([CompanyId]) REFERENCES [dbo].[Companies]([CompanyId]) ON DELETE SET NULL,
        CONSTRAINT FK_IPOs_Registrars FOREIGN KEY ([RegistrarId]) REFERENCES [dbo].[Registrars]([RegistrarId]) ON DELETE SET NULL
    );

    CREATE NONCLUSTERED INDEX IX_IPOs_MarketType ON [dbo].[IPOs]([MarketType]);
    CREATE NONCLUSTERED INDEX IX_IPOs_Status ON [dbo].[IPOs]([Status]);
    CREATE NONCLUSTERED INDEX IX_IPOs_CloseDate ON [dbo].[IPOs]([CloseDate]);
END
GO

-- 9. GMP Details & History
IF OBJECT_ID(N'[dbo].[GMPDetails]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[GMPDetails] (
        [GMPId] INT IDENTITY(1,1) PRIMARY KEY,
        [IPOId] INT NOT NULL UNIQUE,
        [CurrentGMP] DECIMAL(18, 2) NOT NULL DEFAULT 0,
        [EstimatedListingPrice] DECIMAL(18, 2) NULL,
        [EstimatedGainPercent] DECIMAL(18, 2) NULL,
        [Rating] NVARCHAR(50) NULL DEFAULT 'Neutral',
        [KostakRate] DECIMAL(18, 2) NULL,
        [SubjectToSauda] DECIMAL(18, 2) NULL,
        [LastUpdated] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_GMPDetails_IPOs FOREIGN KEY ([IPOId]) REFERENCES [dbo].[IPOs]([IPOId]) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID(N'[dbo].[GMPHistory]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[GMPHistory] (
        [HistoryId] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [IPOId] INT NOT NULL,
        [EntryDate] NVARCHAR(30) NOT NULL,
        [GMPAmount] DECIMAL(18, 2) NOT NULL,
        [EstimatedPrice] DECIMAL(18, 2) NULL,
        [GainPercent] DECIMAL(18, 2) NULL,
        [RecordedAt] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_GMPHistory_IPOs FOREIGN KEY ([IPOId]) REFERENCES [dbo].[IPOs]([IPOId]) ON DELETE CASCADE
    );

    CREATE NONCLUSTERED INDEX IX_GMPHistory_IPOId ON [dbo].[GMPHistory]([IPOId]);
END
GO

-- 10. Subscription Details & Breakdown
IF OBJECT_ID(N'[dbo].[SubscriptionDetails]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[SubscriptionDetails] (
        [SubscriptionId] INT IDENTITY(1,1) PRIMARY KEY,
        [IPOId] INT NOT NULL UNIQUE,
        [QIBTimes] DECIMAL(18, 2) NOT NULL DEFAULT 0,
        [NIITimes] DECIMAL(18, 2) NOT NULL DEFAULT 0,
        [RetailTimes] DECIMAL(18, 2) NOT NULL DEFAULT 0,
        [TotalTimes] DECIMAL(18, 2) NOT NULL DEFAULT 0,
        [LastUpdated] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_SubscriptionDetails_IPOs FOREIGN KEY ([IPOId]) REFERENCES [dbo].[IPOs]([IPOId]) ON DELETE CASCADE
    );
END
GO

IF OBJECT_ID(N'[dbo].[SubscriptionBreakdown]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[SubscriptionBreakdown] (
        [BreakdownId] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [IPOId] INT NOT NULL,
        [DayLabel] NVARCHAR(50) NOT NULL, -- Day 1, Day 2, Day 3
        [BiddingDate] DATE NOT NULL,
        [QIBTimes] DECIMAL(18, 2) NOT NULL,
        [NIITimes] DECIMAL(18, 2) NOT NULL,
        [RetailTimes] DECIMAL(18, 2) NOT NULL,
        [TotalTimes] DECIMAL(18, 2) NOT NULL,
        CONSTRAINT FK_SubscriptionBreakdown_IPOs FOREIGN KEY ([IPOId]) REFERENCES [dbo].[IPOs]([IPOId]) ON DELETE CASCADE
    );
END
GO

-- 11. Financial Details
IF OBJECT_ID(N'[dbo].[FinancialDetails]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[FinancialDetails] (
        [FinancialId] INT IDENTITY(1,1) PRIMARY KEY,
        [IPOId] INT NOT NULL,
        [FinancialYear] NVARCHAR(20) NOT NULL, -- FY22, FY23, FY24
        [RevenueCr] DECIMAL(18, 2) NULL,
        [ExpenseCr] DECIMAL(18, 2) NULL,
        [ProfitAfterTaxCr] DECIMAL(18, 2) NULL,
        [NetWorthCr] DECIMAL(18, 2) NULL,
        [TotalAssetsCr] DECIMAL(18, 2) NULL,
        CONSTRAINT FK_FinancialDetails_IPOs FOREIGN KEY ([IPOId]) REFERENCES [dbo].[IPOs]([IPOId]) ON DELETE CASCADE
    );
END
GO

-- 12. User Watchlists
IF OBJECT_ID(N'[dbo].[Watchlists]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[Watchlists] (
        [WatchlistId] INT IDENTITY(1,1) PRIMARY KEY,
        [UserId] INT NOT NULL,
        [IPOId] INT NOT NULL,
        [AddedDate] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT FK_Watchlists_Users FOREIGN KEY ([UserId]) REFERENCES [dbo].[Users]([UserId]) ON DELETE CASCADE,
        CONSTRAINT FK_Watchlists_IPOs FOREIGN KEY ([IPOId]) REFERENCES [dbo].[IPOs]([IPOId]) ON DELETE CASCADE,
        CONSTRAINT UQ_User_IPO UNIQUE ([UserId], [IPOId])
    );
END
GO

-- 13. Audit Logs (Admin Audit Trail)
IF OBJECT_ID(N'[dbo].[AuditLogs]', N'U') IS NULL
BEGIN
    CREATE TABLE [dbo].[AuditLogs] (
        [LogId] BIGINT IDENTITY(1,1) PRIMARY KEY,
        [UserId] INT NULL,
        [Action] NVARCHAR(100) NOT NULL,
        [Entity] NVARCHAR(100) NOT NULL,
        [EntityId] NVARCHAR(100) NULL,
        [OldValue] NVARCHAR(MAX) NULL,
        [NewValue] NVARCHAR(MAX) NULL,
        [Timestamp] DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [IpAddress] NVARCHAR(50) NULL
    );
END
GO

-- 14. Seed Roles and Default Admin/User
IF NOT EXISTS (SELECT 1 FROM [dbo].[Roles] WHERE [RoleName] = 'ADMIN')
    INSERT INTO [dbo].[Roles] ([RoleName], [Description]) VALUES ('ADMIN', 'Administrator with full management permissions');

IF NOT EXISTS (SELECT 1 FROM [dbo].[Roles] WHERE [RoleName] = 'USER')
    INSERT INTO [dbo].[Roles] ([RoleName], [Description]) VALUES ('USER', 'Standard registered investor user');

-- Seed Categories
IF NOT EXISTS (SELECT 1 FROM [dbo].[IPOCategories] WHERE [CategoryCode] = 'MAINBOARD')
    INSERT INTO [dbo].[IPOCategories] ([CategoryCode], [CategoryName]) VALUES ('MAINBOARD', 'NSE / BSE Mainboard IPOs');

IF NOT EXISTS (SELECT 1 FROM [dbo].[IPOCategories] WHERE [CategoryCode] = 'SME')
    INSERT INTO [dbo].[IPOCategories] ([CategoryCode], [CategoryName]) VALUES ('SME', 'NSE Emerge / BSE SME IPOs');

-- Seed Default Admin User (Password: Admin@12345)
IF NOT EXISTS (SELECT 1 FROM [dbo].[Users] WHERE [Username] = 'admin')
BEGIN
    INSERT INTO [dbo].[Users] ([Username], [Email], [PasswordHash], [FullName], [IsActive])
    VALUES ('admin', 'admin@ipodekho.com', 'AQAAAAIAAYagAAAAENK5w6y2Xf0Y9p+8r4m+VjG+3hN8k0K9L6q3X1y4Z7w=', 'System Administrator', 1);
    
    DECLARE @AdminUserId INT = SCOPE_IDENTITY();
    DECLARE @AdminRoleId INT = (SELECT [RoleId] FROM [dbo].[Roles] WHERE [RoleName] = 'ADMIN');
    
    INSERT INTO [dbo].[UserRoles] ([UserId], [RoleId]) VALUES (@AdminUserId, @AdminRoleId);
END
GO
