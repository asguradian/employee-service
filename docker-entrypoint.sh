#!/bin/sh
set -eu

if [ -n "${LOG_FILE:-}" ]; then
  mkdir -p "$(dirname "$LOG_FILE")"
fi

exec java -jar /app/employee-service.jar "$@"
