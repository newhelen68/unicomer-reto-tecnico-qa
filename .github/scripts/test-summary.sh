#!/usr/bin/env bash
# Genera un resumen Markdown a partir de testng-results.xml.
set -euo pipefail

title="$1"
results="target/surefire-reports/testng-results.xml"

echo "### $title"
if [[ ! -f "$results" ]]; then
  echo "Sin resultados de TestNG: la ejecución no llegó a correr las pruebas."
  exit 0
fi

header=$(grep -o '<testng-results[^>]*>' "$results")
attr() { echo "$header" | grep -o "$1=\"[0-9]*\"" | grep -o '[0-9]*'; }

# El total de TestNG incluye las pruebas excluidas por grupo; se cuentan solo las ejecutadas.
passed=$(attr passed)
failed=$(attr failed)
skipped=$(attr skipped)

echo "| Ejecutadas | Aprobadas | Fallidas | Omitidas |"
echo "|---|---|---|---|"
echo "| $((passed + failed + skipped)) | $passed | $failed | $skipped |"
