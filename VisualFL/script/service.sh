#!/bin/bash



# start_service <port> <name> <cmd> <cmd args...>
start_service() {
  local pid
  pid=$(lsof -i:"$1" | grep 'LISTEN' | awk '{print $2}' | uniq)
  if [[ -z ${pid} ]]; then
    $3 "${@:4}"
    for ((i = 1; i <= 20; i++)); do
      sleep 0.1
      pid=$(lsof -i:"$1" | grep 'LISTEN' | awk '{print $2}' | uniq)
      if [[ -n ${pid} ]]; then
        echo "$2 service start successfully. pid: ${pid}"
        exit 0
      fi
    done
    echo "$2 service start failed"
    exit 1
  else
    echo "$2 service already started at port $1. pid: $pid"
    exit 1
  fi
}

# stop_service <port> <name>
stop_service_by_port() {
  local pid
  pid=$(lsof -i:"$1" | grep 'LISTEN' | awk '{print $2}' | uniq)
  if [[ -n ${pid} ]]; then
    echo "killing: $(ps aux | grep "${pid}" | grep -v grep)"
    for ((i = 1; i <= 20; i++)); do
      sleep 0.1
      kill "$pid"
      pid=$(lsof -i:"$1" | grep 'LISTEN' | awk '{print $2}' | uniq)
      if [[ -z ${pid} ]]; then
        echo "killed by SIGTERM"
        exit 0
      fi
    done
    echo $pid
    kill -9 "$pid"
    echo "killed by SIGKILL"
    exit 0
  else
    echo "$2 service not running"
    exit 1
  fi
}
