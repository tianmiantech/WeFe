
#!/bin/bash

# 导入配置
source ../wefe.cfg

identity_type=$1
identity_name=$2

# ******************
# 定义函数：docker-compose配置初始化
# *****************
spark_cluster_config(){
    # $1：identity_type
    echo "当前type:$identity_type"
    case $identity_type in
      master)
        cp -f resources/conf/docker-compose-master.yml.template docker-compose.yml
        ;;
      slave)
        cp -f resources/conf/docker-compose-slave.yml.template docker-compose.yml
        ;;
      esac
}

spark_cluster_config $identity_type

# 修改服务启动配置
sed -i "/wefe_version/s/python_service:.*#/python_service:$WEFE_VERSION #/g" ./resources/docker-compose.yml
sed -i "/flow_logs/s@-.*:@- $DATA_PATH/logs/flow:@g" ./resources/docker-compose.yml

# 修改 flow 端口
sed -i "/flow_port/s/-.*:/- $PYTHON_SERVICE_PORT:/g" ./resources/docker-compose.yml

# TODO:修改 spark 相关配置
sed -i "/spark_master/s/master:7077/$SPARK_MASTER:7077/g" ./resources/docker-compose.yml
sed -i "/slave_name/s/wefe_python_service_slave/wefe_python_service_$identity_name/g" ./resources/docker-compose.yml

# 加载本地离线镜像包
echo "开始加载 flow 离线镜像"
docker load < resources/wefe_python_service_$WEFE_VERSION\.tar
echo "加载 flow 离线镜像完成"

# 启动 flow 镜像
docker-compose -p $WEFE_ENV -f resources/docker-compose.yml up -d