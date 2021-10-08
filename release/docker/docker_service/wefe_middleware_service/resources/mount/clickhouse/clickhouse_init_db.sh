#!/bin/bash
set -e

clickhouse client -n <<-EOSQL
    CREATE DATABASE IF NOT EXISTS wefe_data;
    CREATE DATABASE IF NOT EXISTS wefe_process;
EOSQL
