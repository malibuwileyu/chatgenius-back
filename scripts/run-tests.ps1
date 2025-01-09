# Load environment variables from .env file
$envFile = Join-Path $PSScriptRoot "../.env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^(SUPABASE_URL|SUPABASE_KEY)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            # Remove surrounding quotes if they exist
            $value = $value -replace '^["'']|["'']$'
            [Environment]::SetEnvironmentVariable($key, $value)
            Write-Host "Loaded $key"
        }
    }
    
    # Verify required variables
    if (-not [Environment]::GetEnvironmentVariable("SUPABASE_URL") -or 
        -not [Environment]::GetEnvironmentVariable("SUPABASE_KEY")) {
        Write-Host "Error: SUPABASE_URL and SUPABASE_KEY must be set in .env file" -ForegroundColor Red
        exit 1
    }
    
    Write-Host "Successfully loaded Supabase configuration" -ForegroundColor Green
} else {
    Write-Host "Error: .env file not found at $envFile" -ForegroundColor Red
    exit 1
}

# Verify Maven settings
$mavenRepo = "J:\.m2\repository"
$mavenSettings = "J:\.m2\settings.xml"

if (-not (Test-Path $mavenRepo) -or -not (Test-Path $mavenSettings)) {
    Write-Host "Maven not configured for J: drive. Running setup script..." -ForegroundColor Yellow
    & "$PSScriptRoot\setup-maven.ps1"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to configure Maven. Please run setup-maven.ps1 manually." -ForegroundColor Red
        exit 1
    }
}

# Set Spring active profile to test
[Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", "test")

# Run tests with Maven using custom settings
Write-Host "Running integration tests..."
$mavenCmd = "mvn -s J:\.m2\settings.xml test -Dtest=*IntegrationTest -DfailIfNoTests=false"
Invoke-Expression $mavenCmd

# Check if tests passed
if ($LASTEXITCODE -eq 0) {
    Write-Host "All tests passed successfully!" -ForegroundColor Green
} else {
    Write-Host "Tests failed with exit code $LASTEXITCODE" -ForegroundColor Red
}

# Clean up environment variables
[Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", $null)
[Environment]::SetEnvironmentVariable("SUPABASE_URL", $null)
[Environment]::SetEnvironmentVariable("SUPABASE_KEY", $null) 