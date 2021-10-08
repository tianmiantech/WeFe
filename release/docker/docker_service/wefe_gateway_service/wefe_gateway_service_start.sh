
#!/bin/bash

# 导入配置
source ../wefe.cfg

# 修改 gateway 端口
sed -i "/gateway_port/s/-.*:/- $GATEWAY_SERVICE_PORT:/g" ./resources/docker-compose.yml 

# 修改服务启动配置
sed -i "/gateway_logs/s@-.*:@- \"$DATA_PATH/logs/gateway:@g" ./resources/docker-compose.yml
sed -i "/wefe_version/s/gateway_service:.*#/gateway_service:${WEFE_VERSION} #/g" ./resources/docker-compose.yml

# 加载本地离线镜像包
echo "开始加载 gateway 离线镜像"
docker load < resources/wefe_gateway_service_${WEFE_VERSION}\.tar
echo "加载 gateway 离线镜像完成"

docker-compose -p $WEFE_ENV -f resources/docker-compose.yml up -d
