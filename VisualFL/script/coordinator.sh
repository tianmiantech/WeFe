#!/bin/bash



DIR="$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)"
PROJECT_BASE=$(dirname "${DIR}")

# shellcheck source=env.sh
. "${PROJECT_BASE}/script/env.sh"
# shellcheck source=service.sh
. "${PROJECT_BASE}/script/service.sh"

usage="Usage: [PYTHON_EXECUTABLE=...] coordinator.sh (start|stop) <port>"
if [ $# -le 1 ]; then
  echo "$usage"
  exit 1
fi

if [ -z "${PYTHON_EXECUTABLE}" ]; then
  echo "python executable not set"
  exit 1
fi

start_coordinator() {
  local re='^[0-9]+$'
  if ! [[ $1 =~ $re ]]; then
    echo "error: port should be number" >&2
    echo "$usage"
    exit 1
  fi
  mkdir -p "$PROJECT_BASE/logs/nohup"
  nohup "${PYTHON_EXECUTABLE}" -m visualfl.client.coordinator --port "${1}" >>"${PROJECT_BASE}/logs/nohup/coordinator" 2>&1 &
}

case "$1" in
start)
  start_service "$2" coordinator start_coordinator "$2"
  exit 0
  ;;
stop)
  stop_service_by_port "$2" coordinator
  exit 0
  ;;
*)
  echo bad command
  echo "$usage"
  exit 1
  ;;
esac
