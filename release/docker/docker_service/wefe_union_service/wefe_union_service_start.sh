#!/bin/bash

# 导入配置
source ../wefe.cfg

# 修改服务启动配置
sed -i "/service_logs/s@-.*:@- \"$DATA_PATH/logs/service:@g" ./resources/docker-compose.yml
sed -i "/wefe_version/s/service:.*#/service:$WEFE_VERSION #/g" ./resources/docker-compose.yml

# 修改镜像文件配置
sed -i "/service_port/s/-.*:/- $UNION_SERVICE_PORT:/g" ./resources/docker-compose.yml

# 加载本地离线镜像包
echo "开始加载 union-service 离线镜像"
docker load < resources/wefe_union_service_${WEFE_VERSION}\.tar
echo "加载 union-service 离线镜像完成"

docker-compose -p $WEFE_ENV -f resources/docker-compose.yml up -d
