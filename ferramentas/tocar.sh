#!/usr/bin/env bash
# Toca no primeiro elemento da tela cujo texto (ou descrição) é exatamente <texto>.
# Uso: bash ferramentas/tocar.sh "Pessoas"
set -euo pipefail
# Git Bash: não converter caminhos do Android (/sdcard) em caminhos do Windows.
export MSYS_NO_PATHCONV=1
ADB=/d/android-dev/sdk/platform-tools/adb.exe
"$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null
XML=$("$ADB" exec-out cat /sdcard/ui.xml)
BOUNDS=$(echo "$XML" | tr '>' '\n' | grep -E "(text|content-desc)=\"$1\"" | head -1 | sed -E 's/.*bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]".*/\1 \2 \3 \4/')
if [ -z "$BOUNDS" ]; then echo "não achei: $1" >&2; exit 1; fi
read -r X1 Y1 X2 Y2 <<<"$BOUNDS"
"$ADB" shell input tap $(( (X1 + X2) / 2 )) $(( (Y1 + Y2) / 2 ))
