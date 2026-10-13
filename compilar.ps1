# Compila o app e gera o APK de debug.
# Uso (no PowerShell, dentro da pasta do projeto):   .\compilar.ps1
# Para instalar direto no celular conectado via USB: .\compilar.ps1 -Instalar
param([switch]$Instalar)

# Ferramentas baixadas em D:\android-dev (troque se instalar em outro lugar).
$env:JAVA_HOME = 'D:\android-dev\jdk17'
$env:GRADLE_USER_HOME = 'D:\android-dev\gradle-home'

$tarefa = if ($Instalar) { 'installDebug' } else { 'assembleDebug' }
& "$PSScriptRoot\gradlew.bat" $tarefa
if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "APK pronto em: $PSScriptRoot\app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Green
}
