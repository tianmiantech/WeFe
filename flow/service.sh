#!/bin/bash

export PYTHONPATH=/opt/welab/wefe
export SPARK_HOME=/data/spark-3.0.1-bin-hadoop2.7
# export PYSPARK_PYTHON=/data/spark-2.4.5-bin-hadoop2.7/python
PYTHON_ROOT=/opt/welab/wefe
log_dir=/data/logs/flow_server
venv=/opt/welab/python/venv
flowUrl=http://0.0.0.0:5000


getpid() {
     pid=$(ps -ef|grep python|grep app_launcher.py|grep -v grep|awk '{print $2}')

    if [[ -n ${pid} ]]; then
        return 1
    else
        return 0
    fi
}

mklogsdir() {
    if [[ ! -d $log_dir ]]; then
        mkdir -p $log_dir
    fi
}

status() {
    getpid
    if [[ -n ${pid} ]]; then
        echo "status:
        `ps aux | grep ${pid} | grep -v grep`"
        return 1
    else
        echo "service not running"
        return 0
    fi
}

clean_pycache(){
  # find $PYTHON_ROOT -name '*.pyc' -type f -exec rm {} \;
  find $PYTHON_ROOT -type d | grep __pycache__ | xargs rm -rf
  echo "clean_pycache"
}

start() {
    getpid
    if [[ $? -eq 0 ]]; then

        mklogsdir
        clean_pycache

        source ${venv}/bin/activate

        pip install -r ${PYTHON_ROOT}/requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

        nohup python3 ${PYTHON_ROOT}/flow/service/config/fill_config_shell_service.py >> "${log_dir}/console.log" 2>>"${log_dir}/error.log" &
        nohup python3 ${PYTHON_ROOT}/flow/app_launcher.py >> "${log_dir}/console.log" 2>>"${log_dir}/error.log" &

        sleep 3
        getpid
        if [[ -n ${pid} ]]; then
           echo "service start sucessfully. pid: ${pid}"
        else
           echo "service start failed, please check ${log_dir}/error.log and ${log_dir}/console.log"
        fi
    else
        echo "service already started. pid: ${pid}"
    fi
}

stop() {
    getpid
    if [[ -n ${pid} ]]; then
        echo "clean before stop"
        curl ${flowUrl}/flow/clean

        getpid
        echo "killing:
        `ps aux | grep ${pid} | grep -v grep`"
        kill -9 ${pid}
        # kill -9 $(ps -ef|grep wefe|grep task_executor|grep -v grep|awk '{print $2}')
        if [[ $? -eq 0 ]]; then
            echo "killed"
        else
            echo "kill error"
        fi
    else
        echo "service not running"
    fi
}

case "$1" in
    start)
        start
        status
        ;;

    stop)
        stop
        ;;
    status)
        status
        ;;

    restart)
        stop
        start
        status
        ;;
    *)
        echo "usage: $0 {start|stop|status|restart}"
        exit -1
esac
