#!/usr/bin/env bash
# Tira um print do emulador/celular conectado e salva em capturas/<nome>.png
# Uso: bash ferramentas/captura.sh <nome> [claro|escuro]
set -euo pipefail
# Git Bash: não converter caminhos do Android (/sdcard) em caminhos do Windows.
export MSYS_NO_PATHCONV=1
ADB=/d/android-dev/sdk/platform-tools/adb.exe
DIR="$(cd "$(dirname "$0")/.." && pwd)/capturas"
mkdir -p "$DIR"
case "${2:-}" in
  claro) "$ADB" shell cmd uimode night no >/dev/null; sleep 2 ;;
  escuro) "$ADB" shell cmd uimode night yes >/dev/null; sleep 2 ;;
esac
"$ADB" exec-out screencap -p > "$DIR/$1.png"
echo "$DIR/$1.png"
