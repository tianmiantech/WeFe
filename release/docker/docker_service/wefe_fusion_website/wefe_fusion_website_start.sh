#!/bin/bash

# 导入配置
source ../wefe.cfg

# Nginx 配置文件修改
sed -i "/server_name/s/server_name.*;/server_name  $EXTRANET_IP;/g" ./resources/mount/default.conf
sed -i "/proxy_pass/s@proxy_pass.*;@proxy_pass http://$EXTRANET_IP:$FUSION_SERVICE_PORT/fusion-service/;@g" ./resources/mount/default.conf

# 修改静态文件
sed -i "s@baseUrl.*/fusion-service@baseUrl: \"http://$EXTRANET_IP:$FUSION_WEBSITE_PORT/fusion-service@g" ./resources/mount/fusion-website/fusion-website/index.html
sed -i "/wefe_version/s/website:.*#/website:$WEFE_VERSION #/g" ./resources/docker-compose.yml
sed -i "/website_port/s/-.*:/- $FUSION_WEBSITE_PORT:/g" ./resources/docker-compose.yml

echo "开始加载 fusion-website 离线镜像"
docker load < resources/wefe_fusion_website_${WEFE_VERSION}\.tar
echo "加载 fusion-website 离线镜像完成"

docker-compose -p $WEFE_ENV -f resources/docker-compose.yml up -d
