using System.Text.Json.Serialization;

namespace IpoDekho.Backend.Models.Dtos;

public class IpoDto
{
    [JsonPropertyName("id")]
    public string Id { get; set; } = string.Empty;

    [JsonPropertyName("name")]
    public string Name { get; set; } = string.Empty;

    [JsonPropertyName("symbol")]
    public string Symbol { get; set; } = string.Empty;

    [JsonPropertyName("category")]
    public string Category { get; set; } = "MAINBOARD";

    [JsonPropertyName("status")]
    public string Status { get; set; } = "UPCOMING";

    [JsonPropertyName("price_band_min")]
    public double? PriceBandMin { get; set; }

    [JsonPropertyName("price_band_max")]
    public double? PriceBandMax { get; set; }

    [JsonPropertyName("lot_size")]
    public int? LotSize { get; set; }

    [JsonPropertyName("min_investment")]
    public double? MinInvestment { get; set; }

    [JsonPropertyName("issue_size_cr")]
    public double? IssueSizeCr { get; set; }

    [JsonPropertyName("fresh_issue_cr")]
    public double? FreshIssueCr { get; set; }

    [JsonPropertyName("ofs_cr")]
    public double? OfsCr { get; set; }

    [JsonPropertyName("open_date")]
    public string? OpenDate { get; set; }

    [JsonPropertyName("close_date")]
    public string? CloseDate { get; set; }

    [JsonPropertyName("allotment_date")]
    public string? AllotmentDate { get; set; }

    [JsonPropertyName("listing_date")]
    public string? ListingDate { get; set; }

    [JsonPropertyName("current_gmp")]
    public double? CurrentGmp { get; set; }

    [JsonPropertyName("estimated_listing_price")]
    public double? EstimatedListingPrice { get; set; }

    [JsonPropertyName("estimated_gain_percent")]
    public double? EstimatedGainPercent { get; set; }

    [JsonPropertyName("last_gmp_updated")]
    public string? LastGmpUpdated { get; set; }

    [JsonPropertyName("current_subscription_times")]
    public double? CurrentSubscriptionTimes { get; set; }

    [JsonPropertyName("qib_times")]
    public double? QibTimes { get; set; }

    [JsonPropertyName("nii_times")]
    public double? NiiTimes { get; set; }

    [JsonPropertyName("retail_times")]
    public double? RetailTimes { get; set; }

    [JsonPropertyName("listing_price")]
    public double? ListingPrice { get; set; }

    [JsonPropertyName("listing_gain_percent")]
    public double? ListingGainPercent { get; set; }

    [JsonPropertyName("current_market_price")]
    public double? CurrentMarketPrice { get; set; }

    [JsonPropertyName("current_return_percent")]
    public double? CurrentReturnPercent { get; set; }

    [JsonPropertyName("description")]
    public string? Description { get; set; }

    [JsonPropertyName("sector")]
    public string? Sector { get; set; }

    [JsonPropertyName("listing_exchanges")]
    public string? ListingExchanges { get; set; }

    [JsonPropertyName("face_value")]
    public double? FaceValue { get; set; }

    [JsonPropertyName("lead_managers")]
    public string? LeadManagers { get; set; }

    [JsonPropertyName("registrar")]
    public string? Registrar { get; set; }

    [JsonPropertyName("promoter_holding_pre")]
    public double? PromoterHoldingPre { get; set; }

    [JsonPropertyName("promoter_holding_post")]
    public double? PromoterHoldingPost { get; set; }

    [JsonPropertyName("subscription_details")]
    public SubscriptionDetailsDto? SubscriptionDetails { get; set; }

    [JsonPropertyName("gmp_history")]
    public List<GmpHistoryItemDto>? GmpHistory { get; set; }
}

public class GmpHistoryItemDto
{
    [JsonPropertyName("date")]
    public string Date { get; set; } = string.Empty;

    [JsonPropertyName("gmp_amount")]
    public double GmpAmount { get; set; }

    [JsonPropertyName("estimated_listing_price")]
    public double EstimatedListingPrice { get; set; }

    [JsonPropertyName("estimated_gain_percent")]
    public double EstimatedGainPercent { get; set; }

    [JsonPropertyName("rating")]
    public string Rating { get; set; } = "Neutral";
}

public class SubscriptionDetailsDto
{
    [JsonPropertyName("ipo_id")]
    public string IpoId { get; set; } = string.Empty;

    [JsonPropertyName("qib_times")]
    public double QibTimes { get; set; }

    [JsonPropertyName("nii_times")]
    public double NiiTimes { get; set; }

    [JsonPropertyName("retail_times")]
    public double RetailTimes { get; set; }

    [JsonPropertyName("total_times")]
    public double TotalTimes { get; set; }

    [JsonPropertyName("day_breakdown")]
    public List<SubscriptionDayDto> DayBreakdown { get; set; } = new();
}

public class SubscriptionDayDto
{
    [JsonPropertyName("day_label")]
    public string DayLabel { get; set; } = string.Empty;

    [JsonPropertyName("date")]
    public string Date { get; set; } = string.Empty;

    [JsonPropertyName("qib_times")]
    public double QibTimes { get; set; }

    [JsonPropertyName("nii_times")]
    public double NiiTimes { get; set; }

    [JsonPropertyName("retail_times")]
    public double RetailTimes { get; set; }

    [JsonPropertyName("total_times")]
    public double TotalTimes { get; set; }
}

public class MarketIndexDto
{
    [JsonPropertyName("symbol")]
    public string Symbol { get; set; } = string.Empty;

    [JsonPropertyName("name")]
    public string Name { get; set; } = string.Empty;

    [JsonPropertyName("current_value")]
    public double CurrentValue { get; set; }

    [JsonPropertyName("change_value")]
    public double ChangeValue { get; set; }

    [JsonPropertyName("change_percent")]
    public double ChangePercent { get; set; }
}
