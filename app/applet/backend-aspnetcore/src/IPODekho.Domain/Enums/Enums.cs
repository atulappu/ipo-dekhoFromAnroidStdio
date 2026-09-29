namespace IPODekho.Domain.Enums;

public enum MarketType
{
    MAINBOARD,
    SME
}

public enum IpoStatus
{
    OPEN,
    UPCOMING,
    CLOSED,
    LISTED
}

public enum UserRoleType
{
    ADMIN,
    USER
}

public enum IpoSortBy
{
    Default,
    GmpDesc,
    CloseDate,
    NameAsc,
    NameDesc
}
