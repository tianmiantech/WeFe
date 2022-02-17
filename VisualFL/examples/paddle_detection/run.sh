#!/bin/bash



# export Environment Variables
#   PYTHONPATH  Python default search path for module files, add Fedvision, PaddleFL, PaddleDetection
#   PYTHON_EXECUTABLE python executable path, such as python | python3 | venv/bin/python

DIR="$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)"

usage="Usage: run.sh <endpoint to submitter service>"

if [ $# -le 0 ]; then
  echo "$usage"
  exit 1
fi

python -m visualfl.client.submitter submit --config "${DIR}/config.yaml" --endpoint "$1"
