#!/bin/sh

ID=`ps -ef | grep paddle_serving_server | grep -v "grep" | awk '{print $2}'`
for id in $ID
do
    kill -9 $id
done
