param(
  [int]$Threads = 20,
  [int]$Loops = 20,
  [int]$Ramp = 10
)

$ErrorActionPreference = 'Stop'

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$root = (Resolve-Path (Join-Path $scriptDir '..\..\')).Path
$systemDir = Join-Path $root 'System'
$jmeterBin = (Get-Command jmeter.bat -ErrorAction Stop).Source
$jmxPath = Join-Path $scriptDir 'programming-platform-core-api.jmx'
$jarPath = Join-Path $systemDir 'target\programming-system-1.0.0.jar'
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$runDir = Join-Path $systemDir 'loadtest-run-output'
$reportDir = Join-Path $runDir "report-$timestamp"
$resultFile = Join-Path $runDir "results-$timestamp.jtl"
$summaryCsv = Join-Path $runDir "summary-$timestamp.csv"
$summaryMd = Join-Path $runDir "summary-$timestamp.md"
$backendLog = Join-Path $runDir "backend-$timestamp.log"
$backendErr = Join-Path $runDir "backend-$timestamp.err.log"
$jmeterLog = Join-Path $runDir "jmeter-$timestamp.log"

if (-not (Test-Path $runDir)) {
  throw "Load test output directory not found: $runDir"
}

function Get-PercentileValue {
  param(
    [double[]]$Values,
    [double]$Percentile
  )

  if (-not $Values -or $Values.Count -eq 0) {
    return 0
  }

  $sorted = $Values | Sort-Object
  $index = [Math]::Ceiling(($Percentile / 100) * $sorted.Count) - 1
  if ($index -lt 0) { $index = 0 }
  if ($index -ge $sorted.Count) { $index = $sorted.Count - 1 }
  return [Math]::Round([double]$sorted[$index], 2)
}

function Wait-BackendReady {
  param([string]$BaseUrl)

  $deadline = (Get-Date).AddSeconds(60)
  while ((Get-Date) -lt $deadline) {
    try {
      $response = Invoke-WebRequest -Uri "$BaseUrl/api/problem/languages" -UseBasicParsing -TimeoutSec 5
      if ($response.StatusCode -ge 200) {
        return
      }
    } catch {
      if ($_.Exception.Response) {
        return
      }
    }
    Start-Sleep -Seconds 2
  }

  throw "Backend did not become ready within 60 seconds."
}

function New-LoadtestUser {
  param([string]$BaseUrl)

  $username = "loadtest_$timestamp"
  $password = 'Loadtest123'
  $payload = @{
    username = $username
    password = $password
  } | ConvertTo-Json

  $registerResponse = Invoke-RestMethod -Uri "$BaseUrl/api/user/register" -Method Post -ContentType 'application/json' -Body $payload
  if ($registerResponse.code -ne 200) {
    throw "Register failed: $($registerResponse.msg)"
  }

  return @{
    Username = $username
    Password = $password
  }
}

function Build-Summary {
  param(
    [string]$JtlPath,
    [string]$CsvPath,
    [string]$MarkdownPath,
    [string]$BaseUrl,
    [string]$Username
  )

  $rows = Import-Csv -Path $JtlPath
  if (-not $rows -or $rows.Count -eq 0) {
    throw 'No JMeter result rows were produced.'
  }

  $summary = foreach ($group in ($rows | Group-Object label | Sort-Object Name)) {
    $samples = $group.Group
    $elapsedValues = @($samples | ForEach-Object { [double]$_.elapsed })
    $successCount = @($samples | Where-Object { $_.success -eq 'true' }).Count
    $errorCount = $samples.Count - $successCount
    $firstTimestamp = [double](($samples | Measure-Object -Property timeStamp -Minimum).Minimum)
    $lastTimestamp = [double](($samples | ForEach-Object { [double]$_.timeStamp + [double]$_.elapsed } | Measure-Object -Maximum).Maximum)
    $durationSeconds = [Math]::Max((($lastTimestamp - $firstTimestamp) / 1000), 1)

    [PSCustomObject]@{
      Label = $group.Name
      Samples = $samples.Count
      AvgMs = [Math]::Round(($elapsedValues | Measure-Object -Average).Average, 2)
      P90Ms = Get-PercentileValue -Values $elapsedValues -Percentile 90
      P95Ms = Get-PercentileValue -Values $elapsedValues -Percentile 95
      MaxMs = [Math]::Round(($elapsedValues | Measure-Object -Maximum).Maximum, 2)
      ErrorRatePct = [Math]::Round(($errorCount * 100.0 / [Math]::Max($samples.Count, 1)), 2)
      ThroughputRps = [Math]::Round(($samples.Count / $durationSeconds), 2)
      SuccessCount = $successCount
      ErrorCount = $errorCount
    }
  }

  $summary | Export-Csv -Path $CsvPath -NoTypeInformation -Encoding UTF8

  $overallElapsed = @($rows | ForEach-Object { [double]$_.elapsed })
  $overallErrors = @($rows | Where-Object { $_.success -ne 'true' }).Count
  $overall = [PSCustomObject]@{
    Samples = $rows.Count
    AvgMs = [Math]::Round(($overallElapsed | Measure-Object -Average).Average, 2)
    P95Ms = Get-PercentileValue -Values $overallElapsed -Percentile 95
    MaxMs = [Math]::Round(($overallElapsed | Measure-Object -Maximum).Maximum, 2)
    ErrorRatePct = [Math]::Round(($overallErrors * 100.0 / [Math]::Max($rows.Count, 1)), 2)
  }

  $md = @()
  $md += "# Core API Load Test Summary"
  $md += ""
  $md += "- Base URL: $BaseUrl"
  $md += "- Test user: $Username"
  $md += "- Threads: $Threads"
  $md += "- Loops per thread: $Loops"
  $md += "- Ramp-up seconds: $Ramp"
  $md += "- Total samples: $($overall.Samples)"
  $md += "- Overall avg latency: $($overall.AvgMs) ms"
  $md += "- Overall p95 latency: $($overall.P95Ms) ms"
  $md += "- Overall max latency: $($overall.MaxMs) ms"
  $md += "- Overall error rate: $($overall.ErrorRatePct)%"
  $md += ""
  $md += "| Label | Samples | Avg(ms) | P90(ms) | P95(ms) | Max(ms) | Error% | Throughput(req/s) |"
  $md += "| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |"
  foreach ($item in $summary) {
    $md += "| $($item.Label) | $($item.Samples) | $($item.AvgMs) | $($item.P90Ms) | $($item.P95Ms) | $($item.MaxMs) | $($item.ErrorRatePct) | $($item.ThroughputRps) |"
  }

  Set-Content -Path $MarkdownPath -Value ($md -join [Environment]::NewLine) -Encoding UTF8
}

$job = $null

try {
  $job = Start-Job -ScriptBlock {
    param($TargetJar, $Stdout, $Stderr)

    $env:DB_HOST = '192.168.59.133'
    $env:DB_PORT = '3306'
    $env:DB_NAME = 'programming_system'
    $env:DB_USER = 'root'
    $env:DB_PASSWORD = '123'
    $env:REDIS_HOST = '192.168.59.133'
    $env:REDIS_PORT = '6379'
    $env:REDIS_PASSWORD = ''
    $env:SERVER_PORT = '18080'
    $env:PROGRAMMING_DOCKER_HOST = 'tcp://192.168.59.133:2375'

    & java -jar $TargetJar *>> $Stdout 2>> $Stderr
  } -ArgumentList $jarPath, $backendLog, $backendErr

  $baseUrl = 'http://127.0.0.1:18080'
  Wait-BackendReady -BaseUrl $baseUrl
  $credential = New-LoadtestUser -BaseUrl $baseUrl

  if (Test-Path $reportDir) {
    Remove-Item -Path $reportDir -Recurse -Force
  }

  $jmeterCommand = @(
    "`"$jmeterBin`"",
    '-n',
    "-t `"$jmxPath`"",
    "-l `"$resultFile`"",
    "-j `"$jmeterLog`"",
    '-e',
    "-o `"$reportDir`"",
    '"-Jhost=127.0.0.1"',
    '"-Jport=18080"',
    "`"-Jthreads=$Threads`"",
    "`"-Jloops=$Loops`"",
    "`"-Jramp=$Ramp`"",
    "`"-Jusername=$($credential.Username)`"",
    "`"-Jpassword=$($credential.Password)`""
  ) -join ' '

  cmd /c $jmeterCommand

  if ($LASTEXITCODE -ne 0) {
    throw "JMeter finished with exit code $LASTEXITCODE"
  }

  if (-not (Test-Path $resultFile)) {
    throw "JMeter did not produce a result file: $resultFile"
  }

  Build-Summary -JtlPath $resultFile -CsvPath $summaryCsv -MarkdownPath $summaryMd -BaseUrl $baseUrl -Username $credential.Username

  Write-Host "Load test finished."
  Write-Host "Summary: $summaryMd"
  Write-Host "CSV: $summaryCsv"
  Write-Host "Report: $reportDir"
} finally {
  if ($job) {
    Stop-Job -Job $job -ErrorAction SilentlyContinue | Out-Null
    Remove-Job -Job $job -Force -ErrorAction SilentlyContinue
  }
}
