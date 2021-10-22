
#!/bin/bash

# 导入配置
source ../wefe.cfg

# 修改服务启动配置
sed -i "/wefe_version/s/python_service:.*#/python_service:$WEFE_VERSION #/g" ./resources/docker-compose.yml
sed -i "/flow_logs/s@-.*:@- $DATA_PATH/logs/flow:@g" ./resources/docker-compose.yml

# 修改 flow 端口
sed -i "/flow_port/s/-.*:/- $PYTHON_SERVICE_PORT:/g" ./resources/docker-compose.yml

# 分发镜像、资源配置到集群的相关机器
# ...

# 加载本地离线镜像包
echo "开始加载 flow-master 离线镜像"
# docker load < resources/wefe_python_service_$WEFE_VERSION\.tar
echo "加载 flow-master 离线镜像完成"

# 启动 flow 镜像
# docker-compose -p $WEFE_ENV -f resources/docker-compose.yml up -d
