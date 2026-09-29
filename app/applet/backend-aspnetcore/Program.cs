using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.Services;

var builder = WebApplication.CreateBuilder(args);

// 1. Database Connection (MS SQL Server)
var connectionString = builder.Configuration.GetConnectionString("DefaultConnection")
    ?? "Server=localhost,1433;Database=IpoDekhoDb;User Id=sa;Password=YourStrong@Password123;TrustServerCertificate=True;";

builder.Services.AddDbContext<AppDbContext>(options =>
{
    options.UseSqlServer(connectionString, sqlOptions =>
    {
        sqlOptions.EnableRetryOnFailure(
            maxRetryCount: 5,
            maxRetryDelay: TimeSpan.FromSeconds(15),
            errorNumbersToAdd: null);
    });
});

// 2. HTTP Client for Scraper
builder.Services.AddHttpClient("IpoCrawler", client =>
{
    client.DefaultRequestHeaders.Add("User-Agent",
        builder.Configuration.GetValue<string>("ScraperSettings:UserAgent")
        ?? "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
    client.Timeout = TimeSpan.FromSeconds(30);
});

// 3. Register Background Scraper Worker
builder.Services.AddHostedService<IpoSyncBackgroundWorker>();

// 4. Controllers & JSON Options
builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        options.JsonSerializerOptions.DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull;
        options.JsonSerializerOptions.PropertyNamingPolicy = System.Text.Json.JsonNamingPolicy.SnakeCaseLower;
    });

// 5. CORS for Android App & Web Testing
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// 6. Swagger API Documentation
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
    {
        Title = "IPODekho REST API",
        Version = "v1",
        Description = "ASP.NET Core 8 Web API backend for the IPODekho Android App. Powered by MS SQL Server."
    });
});

var app = builder.Build();

// Auto-migrate & seed database schema on startup
using (var scope = app.Services.CreateScope())
{
    try
    {
        var dbContext = scope.ServiceProvider.GetRequiredService<AppDbContext>();
        // Automatically creates database and tables if running against fresh SQL instance
        dbContext.Database.EnsureCreated();
    }
    catch (Exception ex)
    {
        app.Logger.LogWarning(ex, "Could not ensure database created at startup. Verify SQL Server connection string.");
    }
}

// HTTP Pipeline
if (app.Environment.IsDevelopment() || true) // enable Swagger in dev and test
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "IPODekho API v1");
        c.RoutePrefix = string.Empty; // Serve Swagger at root: http://localhost:5000/
    });
}

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

app.Run();
