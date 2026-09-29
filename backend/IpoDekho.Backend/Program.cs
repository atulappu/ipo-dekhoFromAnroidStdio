using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the DI container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
    {
        Title = "IPODekho Real-Time API",
        Version = "v1",
        Description = "ASP.NET Core 8 Web API for Indian IPO Tracker, Live GMP feeds, and Push Notifications"
    });
});

// Configure SQL Server Database Context
builder.Services.AddDbContext<IpoDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("IpoDbConnection")));

// Register Scraper, Notification and Background Services
builder.Services.AddHttpClient();
builder.Services.AddScoped<IFirebaseNotificationService, FirebaseNotificationService>();
builder.Services.AddScoped<IIpoScraperService, IpoScraperService>();
builder.Services.AddHostedService<IpoDataIngestionWorker>();

// Configure CORS for Android Mobile App & Web
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment() || true) // Enabled for easy testing
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "IPODekho API v1");
        c.RoutePrefix = string.Empty; // Swagger UI as root
    });
}

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

app.Run();
