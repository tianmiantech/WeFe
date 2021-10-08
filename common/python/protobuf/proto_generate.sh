#!/usr/bin/env bash


BASEDIR=$(dirname "$0")
cd "$BASEDIR" || exit

PROTO_DIR="proto"
TARGER_DIR="pyproto"

generate() {
  python3 -m grpc_tools.protoc -I./$PROTO_DIR --python_out=./$TARGER_DIR --grpc_python_out=./$TARGER_DIR "$1"
}

generate_all() {
  for proto in "$PROTO_DIR"/*.proto; do
    echo "protoc: $proto"
    generate "$proto"
  done
}

if [ $# -gt 0 ]; then
  generate "$1"
else
  generate_all
fi
