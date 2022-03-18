#!/bin/bash

export INPUT_ACTION=$1
export INPUT_SERVICE=$2
export INPUT_DEPLOY=$3

source ./wefe.cfg

if [ $SPARK_MODE = "STANDALONE" ];then
  source ./spark_cluster.sh
fi

export PWD=$(pwd)

edit_wefe_config(){

    # ************
    # 加速方案相关配置
    # ************
    sed -i "/wefe.job.acceleration/s/=.*/=$ACCELERATION/g" ./config.properties

    # ************
    # 中间件相关配置
    # ************

    # clickhouse
    sed -i "/clickhouse.url/s/:\/\/.*/:\/\/$INTRANET_IP:$CLICKHOUSE_PORT/g" ./config.properties
    sed -i "/clickhouse.host/s/=.*/=$INTRANET_IP/g" ./config.properties
    sed -i "/clickhouse.tcp.port/s/=.*/=$CLICKHOUSE_TCP_PORT/g" ./config.properties
    sed -i "/clickhouse.username/s/=.*/=$CLICKHOUSE_USERNAME/g" ./config.properties
    sed -i "/clickhouse.password/s/=.*/=$CLICKHOUSE_PASSWORD/g" ./config.properties

    # mysql
    sed -i "/mysql.url/s/:\/\/.*?/:\/\/$INTRANET_IP:$MYSQL_PORT\/$MYSQL_DATABASE?/g" ./config.properties
    sed -i "/mysql.host/s/=.*/=$INTRANET_IP/g" ./config.properties
    sed -i "/mysql.port/s/=.*/=$MYSQL_PORT/g" ./config.properties
    sed -i "/mysql.database/s/=.*/=$MYSQL_DATABASE/g" ./config.properties
    sed -i "/mysql.username/s/=.*/=$MYSQL_USERNAME/g" ./config.properties
    sed -i "/mysql.password/s/=.*/=$MYSQL_PASSWORD/g" ./config.properties

    # ************
    # 计算引擎相关配置
    # ************

    # 计算引擎选择
#    sed -i "/wefe.job.backend/s/=.*/=$CALCULATION_ENGINE/g" ./config.properties

    # spark
    sed -i "/driver.memory/s/=.*/=$SPARK_DRIVER_MEMORY/g" ./config.properties
    sed -i "/driver.maxResultSize/s/=.*/=$SPARK_DRIVER_MAXRESULTSIZE/g" ./config.properties
    sed -i "/num.executors/s/=.*/=$SPARK_NUM_EXECUTORS/g" ./config.properties
    sed -i "/executor.memory/s/=.*/=$SPARK_EXECUTOR_MEMORY/g" ./config.properties
    sed -i "/executor.cores/s/=.*/=$SPARK_EXECUTOR_CORES/g" ./config.properties
    sed -i "/num.slices/s/=.*/=$SPARK_NUM_SLICES/g" ./config.properties

    # 函数计算
    if [[ $FC_STORAGE_TYPE == 'oss' ]]; then
        # 修改 oss 的临时授权
        sed -i "/fc.cloud_store.temp_auth_internal_end_point/s@//.*@//oss-$FC_REGION-internal.aliyuncs.com@g" ./config.properties
        sed -i "/fc.cloud_store.temp_auth_end_point/s@//.*@//oss-$FC_REGION.aliyuncs.com@g" ./config.properties
        sed -i "/fc.cloud_store.temp_auth_role_arn/s@acs:ram::.*:role/wefe-fc-ossread@acs:ram::$FC_ACCOUNT_ID:role/wefe-fc-ossread@g" ./config.properties
        # 修改 oss 配置
        sed -i "/fc.oss.bucket_name/s/=.*/=$FC_OSS_BUCKET_NAME/g" ./config.properties
        sed -i "/fc.oss.internal_endpoint/s@//.*@//oss-$FC_REGION-internal.aliyuncs.com@g" ./config.properties
        sed -i "/fc.oss.endpoint/s@//.*@//oss-$FC_REGION.aliyuncs.com@g" ./config.properties
    else
        echo "函数存储类型不支持该类型：$FC_STORAGE_TYPE"
    fi
    sed -i "/fc.storage.type/s/=.*/=$FC_STORAGE_TYPE/g" ./config.properties
    sed -i "/fc.qualifier/s/=.*/=$FC_QUALIFIER/g" ./config.properties
    sed -i "/fc.region/s/=.*/=$FC_REGION/g" ./config.properties
    sed -i "/fc.account_id/s/=.*/=$FC_ACCOUNT_ID/g" ./config.properties
    sed -i "/fc.access_key_id/s/=.*/=$FC_ACCESS_KEY_ID/g" ./config.properties
    sed -i "/fc.access_key_secret/s/=.*/=$FC_ACCESS_KEY_SECRET/g" ./config.properties
    sed -i "/fc.vpc_id/s/=.*/=$FC_VPC_ID/g" ./config.properties
    sed -i "/fc.v_switch_ids/s/=.*/=$FC_V_SWITCH_IDS/g" ./config.properties
    sed -i "/fc.security_group_id/s/=.*/=$FC_SECURITY_GROUP_ID/g" ./config.properties
    sed -i "/fc.account_type/s/=.*/=$FC_ACCOUNT_TYPE/g" ./config.properties
    sed -i "s|fc.end_point=https://.*.cn-shenzhen.fc.aliyuncs.com|fc.end_point=https://$FC_ACCOUNT_ID.cn-shenzhen.fc.aliyuncs.com|g" ./config.properties

}

send_wefe_config(){
    cp -f ./config.properties wefe_board_service/resources/mount/
    cp -f ./config.properties wefe_gateway_service/resources/mount/
    cp -f ./config.properties wefe_python_service/resources/mount/
}

init(){
    # 执行前初始化配置文件
    edit_wefe_config
    # 分发配置
    send_wefe_config
}

_run_python_service(){
    if [ ${ACCELERATION,,} = "gpu" ];then
      cd $PWD/wefe_python_service
      sh wefe_python_service_start.sh gpu
    else
      if [ $SPARK_MODE = "STANDALONE" ]; then
        # 集群方式启动
        start_cluster
      else
        if [ $CALCULATION_ENGINE = "FC" ]; then
          # 函数计算启动
          cd $PWD/wefe_python_service
          sh wefe_python_service_start.sh fc
        else
          # 单机启动
          cd $PWD/wefe_python_service
          sh wefe_python_service_start.sh
        fi
      fi
    fi
}

_stop_cluster_python_service(){
    if [ $SPARK_MODE = "STANDALONE" ]; then
      stop_cluster
    fi
}

_remove_cluster_python_service(){
    if [ $SPARK_MODE = "STANDALONE" ]; then
      remove_cluster
    fi
}

start(){
    init
    case $INPUT_SERVICE in
        board)
            cd $PWD/wefe_board_service
            sh wefe_board_service_start.sh
            cd ../wefe_board_website
            sh wefe_board_website_start.sh
            ;;
        gateway)
            cd $PWD/wefe_gateway_service
            sh wefe_gateway_service_start.sh
            ;;
        python)
            _run_python_service
            ;;
        middleware)
            cd $PWD/wefe_middleware_service
            sh wefe_middleware_service_start.sh
            ;;
        '')
            cd $PWD
            sh wefe_service.sh start middleware
            sh wefe_service.sh start board
            sh wefe_service.sh start gateway
            sh wefe_service.sh start python
            ;;
        *)
            echo "Please Input a Legal Service"
            echo "eg. {board|gateway|python|middleware}"
            exit -1
    esac
}

stop(){
    # init
    case $INPUT_SERVICE in
        board | gateway | python | middleware)
            CONTAINER=$(docker ps -a | grep $WEFE_ENV | grep $INPUT_SERVICE | awk '{print $1}' | xargs)
            docker stop $CONTAINER
            if [ $INPUT_SERVICE = "python" ]; then
              _stop_cluster_python_service
            fi
            ;;
        '')
            CONTAINER=$(docker ps -a | grep $WEFE_ENV | grep wefe | awk '{print $1}' | xargs)
            docker stop $CONTAINER
            _stop_cluster_python_service
            ;;
        *)
            echo "Please Input a Legal Service"
            echo "eg. {board|gateway|python|middleware}"
            exit -1
    esac
}

remove(){
    # init
    case $INPUT_SERVICE in
        board | gateway | python | middleware)
            CONTAINER=$(docker ps -a | grep $WEFE_ENV | grep $INPUT_SERVICE | awk '{print $1}' | xargs)
            docker rm $CONTAINER
            if [ $INPUT_SERVICE = "python" ]; then
              _remove_cluster_python_service
            fi
            ;;
        '')
            CONTAINER=$(docker ps -a | grep $WEFE_ENV | grep wefe | awk '{print $1}' | xargs)
            docker rm $CONTAINER
            _remove_cluster_python_service
            ;;
        *)
            echo "Please Input a Legal Service"
            echo "eg. {board|gateway|python|middleware}"
            exit -1
    esac
}

restart(){
    case $INPUT_SERVICE in
        board | gateway | python | middleware)
            CONTAINER=$(docker ps -a | grep $WEFE_ENV |grep $INPUT_SERVICE | awk '{print $1}' | xargs)
            docker restart $CONTAINER
            ;;
        '')
            CONTAINER=$(docker ps -a | grep $WEFE_ENV | grep wefe | awk '{print $1}' | xargs)
            docker restart $CONTAINER
            ;;
        *)
            echo "Please Input a Legal Service"
            echo "eg. {board | gateway | python | middleware}"
            exit -1
    esac
}

help(){
    echo "Support Action: start | stop | restart"
    echo "Support Service: board | gateway | python | middleware"
    echo "sh service.sh [Action] [Service]"
}

case $INPUT_ACTION in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    remove)
        remove
        ;;
    help)
        help
        ;;
    *)
        echo "Please Input a Legal Action"
        echo "eg. {start | stop | restart | help}"
        exit -1
esac                
