param([switch]$ServiceOnly)

$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path $PSScriptRoot -Parent
$evidenceDirectory = Join-Path $repositoryRoot 'docs/laboratorio/unitarias/evidencia'
$logName = if ($ServiceOnly) { 'service-test.log' } else { 'mvn-test.log' }
$testLog = Join-Path $evidenceDirectory $logName
New-Item -ItemType Directory -Force -Path $evidenceDirectory | Out-Null

Push-Location $repositoryRoot
try {
    if ($env:JAVA_HOME) { $env:Path = "$env:JAVA_HOME/bin;$env:Path" }
    $relativeLog = "docs/laboratorio/unitarias/evidencia/$logName"
    $mavenArguments = @('-B', '-ntp', 'test', '-l', $relativeLog)
    if ($ServiceOnly) { $mavenArguments += '-Dtest=PlayerServiceTest' }

    Write-Host 'IDGS101 - Gorilla Escape Backend - Pruebas unitarias'
    [xml]$projectPom = Get-Content pom.xml -Raw
    Write-Host ('JUnit Jupiter ' + $projectPom.project.properties.'junit-jupiter.version' + ' | Mockito | AssertJ')
    Write-Host "Repositorio: $repositoryRoot"
    Write-Host ('Ejecucion: .\mvnw.cmd ' + ($mavenArguments -join ' '))
    & ./mvnw.cmd @mavenArguments
    $mavenExitCode = $LASTEXITCODE

    # Resumen literal del log; el archivo completo conserva tambien los avisos.
    Get-Content -LiteralPath $testLog |
        Select-String '^\[INFO\] (Running |Tests run:|Results:|BUILD SUCCESS|BUILD FAILURE|Total time:|Finished at:)' |
        ForEach-Object { Write-Host $_.Line }
    Write-Host "Codigo de salida Maven: $mavenExitCode"
    if ($mavenExitCode -ne 0) { throw "Maven fallo. Consulta $testLog" }

    $reports = if ($ServiceOnly) {
        Get-ChildItem target/surefire-reports -Filter '*PlayerServiceTest.txt'
    } else {
        Get-ChildItem target/surefire-reports -Filter '*.txt'
    }
    $reports | Copy-Item -Destination $evidenceDirectory -Force
} finally {
    Pop-Location
}
