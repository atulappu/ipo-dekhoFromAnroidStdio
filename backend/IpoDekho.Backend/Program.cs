using Microsoft.EntityFrameworkCore;
using IpoDekho.Backend.Data;
using IpoDekho.Backend.Services;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configure SQL Server Database Context
builder.Services.AddDbContext<IpoDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("IpoDbConnection")));

// Register 5-Minute Automated Ingestion Background Worker
builder.Services.AddHttpClient();
builder.Services.AddHostedService<IpoDataIngestionWorker>();

// Configure CORS for Android App & Web
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

// Configure HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAll");
app.UseAuthorization();
app.MapControllers();

app.Run();
