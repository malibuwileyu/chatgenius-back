# Create Maven settings directory if it doesn't exist
$mavenSettingsDir = "J:\.m2"
if (-not (Test-Path $mavenSettingsDir)) {
    New-Item -ItemType Directory -Path $mavenSettingsDir
    Write-Host "Created Maven settings directory at $mavenSettingsDir"
}

# Create settings.xml if it doesn't exist
$settingsXml = Join-Path $mavenSettingsDir "settings.xml"
if (-not (Test-Path $settingsXml)) {
    @"
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <localRepository>J:\.m2\repository</localRepository>
    <interactiveMode>true</interactiveMode>
    <offline>false</offline>
</settings>
"@ | Out-File -FilePath $settingsXml -Encoding UTF8
    Write-Host "Created Maven settings.xml with custom repository location"
}

# Set environment variables
[Environment]::SetEnvironmentVariable("MAVEN_OPTS", "-Dmaven.repo.local=J:\.m2\repository", [System.EnvironmentVariableTarget]::User)
[Environment]::SetEnvironmentVariable("MAVEN_HOME", "J:\.m2", [System.EnvironmentVariableTarget]::User)

Write-Host "Maven environment variables set. Local repository will be at J:\.m2\repository"
Write-Host "Please restart your terminal for changes to take effect"

# Create repository directory if it doesn't exist
$repoDir = "J:\.m2\repository"
if (-not (Test-Path $repoDir)) {
    New-Item -ItemType Directory -Path $repoDir
    Write-Host "Created Maven repository directory at $repoDir"
}

# Verify settings
Write-Host "`nCurrent Maven Settings:"
Write-Host "MAVEN_OPTS: $([Environment]::GetEnvironmentVariable('MAVEN_OPTS', 'User'))"
Write-Host "MAVEN_HOME: $([Environment]::GetEnvironmentVariable('MAVEN_HOME', 'User'))"
Write-Host "Settings file location: $settingsXml"
Write-Host "Repository location: $repoDir" 