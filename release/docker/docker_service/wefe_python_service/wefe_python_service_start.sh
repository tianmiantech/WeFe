
#!/bin/bash

# 导入配置
source ../wefe.cfg

# ******************
# 定义函数：docker-compose配置初始化
# *****************

spark_cluster_config(){
    # $1：identity_type
    echo "当前type:$1"
    case $1 in
      master)
        cp -f resources/conf/docker-compose-master.yml.template docker-compose.yml
        ;;
      slave)
        cp -f resources/conf/docker-compose-slave.yml.template docker-compose.yml
        ;;
      esac
}

spark_cluster_config $1

# 修改服务启动配置
sed -i "/wefe_version/s/python_service:.*#/python_service:$WEFE_VERSION #/g" ./resources/docker-compose.yml
sed -i "/flow_logs/s@-.*:@- $DATA_PATH/logs/flow:@g" ./resources/docker-compose.yml

# 修改 flow 端口
sed -i "/flow_port/s/-.*:/- $PYTHON_SERVICE_PORT:/g" ./resources/docker-compose.yml

# 加载本地离线镜像包
echo "开始加载 flow 离线镜像"
docker load < resources/wefe_python_service_$WEFE_VERSION\.tar
echo "加载 flow 离线镜像完成"

# 启动 flow 镜像
docker-compose -p $WEFE_ENV -f resources/docker-compose.yml up -d