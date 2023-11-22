#!/bin/bash



DIR="$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)"
PROJECT_BASE=$(dirname "${DIR}")


# shellcheck source=env.sh
. "${PROJECT_BASE}/script/env.sh"
# shellcheck source=service.sh
. "${PROJECT_BASE}/script/service.sh"

usage="Usage: cluster_worker.sh (start|stop) <unique worker name> [<local ip> <port for serve start> <port for serve end> <max tasks> <cluster manager address> <data base dir>]"
if [ $# -le 1 ]; then
  echo "$usage"
  exit 1
fi

if [ -z "${PYTHON_EXECUTABLE}" ]; then
  echo "python executable not set"
  exit 1
fi

start_cluster_worker() {
  local pid
  pid=$(
    ps aux | grep "visualfl.client.cluster_worker" | grep "name ${1}" | grep -v grep | awk '{print $2}'
  )
  if [[ -z ${pid} ]]; then
    mkdir -p "$PROJECT_BASE/logs/nohup"
    nohup "${PYTHON_EXECUTABLE}" -m visualfl.client.cluster_worker --name "$1" --worker-ip "$2" --port-start "$3" --port-end "$4" --max-tasks "$5" --manager-address "$6" --data-base-dir "$7" >>"${PROJECT_BASE}/logs/nohup/worker" 2>&1 &
    for ((i = 1; i <= 20; i++)); do
      sleep 0.1
      pid=$(
        ps aux | grep "visualfl.client.cluster_worker" | grep "name ${1}" | grep -v grep | awk '{print $2}'
      )
      if [[ -n ${pid} ]]; then
        echo "cluster worker service start successfully. pid: ${pid}"
        exit 0
      fi
    done
    echo "cluster worker service start failed"
    exit 1
  else
    echo "cluster worker service named <$1> already started. pid: $pid"
    exit 1
  fi
}

stop_cluster_worker() {
  local pid
  pid=$(
    ps aux | grep "visualfl.client.cluster_worker" | grep "name ${1}" | grep -v grep | awk '{print $2}'
  )
  if [[ -n ${pid} ]]; then
    echo "killing: $(ps aux | grep "${pid}" | grep -v grep)"
    for ((i = 1; i <= 20; i++)); do
      sleep 0.1
      kill "${pid}"
      pid=$(
        ps aux | grep "visualfl.client.cluster_worker" | grep "name ${1}" | grep -v grep | awk '{print $2}'
      )
      if [[ -z ${pid} ]]; then
        echo "killed by SIGTERM"
        exit 0
      fi
    done
    if [[ $(kill -9 "${pid}") -eq 0 ]]; then
      echo "killed by SIGKILL"
      exit 0
    else
      echo "Kill error"
      exit 1
    fi
  else
    echo "cluster worker named <${1}> service not running"
    exit 1
  fi
}

case "$1" in
start)
  start_cluster_worker "$2" "$3" "$4" "$5" "$6" "$7" "$8"
  exit 0
  ;;
stop)
  stop_cluster_worker "$2"
  exit 0
  ;;
*)
  echo bad command
  echo "$usage"
  exit 1
  ;;
esac
