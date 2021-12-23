#!/bin/bash



# export Environment Variables
#   PYTHONPATH  Python default search path for module files, add Fedvision, PaddleFL, PaddleDetection
#   PYTHON_EXECUTABLE python executable path, such as python | python3 | venv/bin/python

DIR="$(cd "$(dirname "$0")" >/dev/null 2>&1 && pwd)"
PROJECT_BASE=$(dirname "${DIR}")

unset PYTHONPATH

DEPS_DIR="${PROJECT_BASE}/depends"
if [ -d "${DEPS_DIR}" ]; then
  # development layout
  export PYTHONPATH="${PROJECT_BASE}:${DEPS_DIR}/PaddleDetection:${DEPS_DIR}/PaddleFL/python:${PYTHONPATH}"
else
  export PYTHONPATH="${PROJECT_BASE}:${PYTHONPATH}"
fi

if [ -z "${PYTHON_EXECUTABLE}" ]; then
  export PYTHON_EXECUTABLE=
fi
