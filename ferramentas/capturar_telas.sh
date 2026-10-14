#!/usr/bin/env bash
# Instala o APK de debug no emulador, carrega dados de exemplo e tira prints
# de todas as telas no tema pedido. Uso: bash ferramentas/capturar_telas.sh <prefixo> [claro|escuro]
set -euo pipefail
# Git Bash: não converter caminhos do Android (/sdcard) em caminhos do Windows.
export MSYS_NO_PATHCONV=1
cd "$(dirname "$0")/.."
ADB=/d/android-dev/sdk/platform-tools/adb.exe
P=${1:-p}
TEMA=${2:-claro}
foco() { until "$ADB" shell dumpsys window | grep -q "mCurrentFocus.*$1"; do sleep 1; done; sleep 2; }
tocar() { bash ferramentas/tocar.sh "$1"; sleep 2; }
foto() { bash ferramentas/captura.sh "$1" | tail -1; }

"$ADB" install -r app/build/outputs/apk/debug/app-debug.apk | tail -1
if [ "$TEMA" = escuro ]; then "$ADB" shell cmd uimode night yes; else "$ADB" shell cmd uimode night no; fi
"$ADB" shell am start -W -n com.miojo.cerebelo/.debug.SeedActivity >/dev/null
foco MainActivity
foto "${P}_nova_nota_$TEMA"
"$ADB" shell input keyevent KEYCODE_BACK; sleep 1; "$ADB" shell input keyevent KEYCODE_BACK; sleep 2
foto "${P}_notas_$TEMA"
tocar "Tempo";    foto "${P}_tempo_$TEMA"
tocar "Pessoas";  foto "${P}_pessoas_$TEMA"
tocar "Ana";      foto "${P}_pessoa_$TEMA"
tocar "Afazeres"; foto "${P}_afazeres_$TEMA"
