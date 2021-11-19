
#!/bin/bash

# 导入配置
source ../wefe.cfg

identity_type=$1
identity_name=$2
cluster_container_ip=$3
cluster_host_ip=$4
worker_ui_port=$5

# ******************
# 定义函数：docker-compose配置初始化
# *****************
spark_cluster_config(){
    echo "当前type:$identity_type"
    case $identity_type in
      master)
        cp -f resources/template/docker-compose-master.yml.template resources/docker-compose.yml
        sed -i "/master_web_ui_port/s/=.* #/=$SPARK_MASTER_UI_PORT #/g" ./resources/docker-compose.yml
        sed -i "/spark_master_ui_port/s/8080:8080/$SPARK_MASTER_UI_PORT:$SPARK_MASTER_UI_PORT/g" ./resources/docker-compose.yml
        sed -i "/master_public_dns/s/SPARK_PUBLIC_DNS=to_replace_ip/SPARK_PUBLIC_DNS=$SPARK_MASTER/g" ./resources/docker-compose.yml
        sed -i "/master_container_ip/s/ipv4_address: to_replace_ip/ipv4_address: $cluster_container_ip/g" ./resources/docker-compose.yml
        ;;
      worker)
        cp -f resources/template/docker-compose-worker.yml.template resources/docker-compose.yml
        sed -i "/worker_name/s/wefe_python_service_worker/wefe_python_service_$identity_name/g" ./resources/docker-compose.yml
        sed -i "/worker_public_dns/s/SPARK_PUBLIC_DNS=to_replace_ip/SPARK_PUBLIC_DNS=$cluster_host_ip/g" ./resources/docker-compose.yml
        sed -i "/worker_web_ui_port/s/=.* #/=$worker_ui_port #/g" ./resources/docker-compose.yml
        sed -i "/worker_container_ip/s/ipv4_address: to_replace_ip/ipv4_address: $cluster_container_ip/g" ./resources/docker-compose.yml

        # worker ui port
        sed -i "/spark_worker_ui_port/s/8081:8081/$worker_ui_port:$worker_ui_port/g" ./resources/docker-compose.yml

        # replace host
        container_subnet_split=(${CONTAINER_SUBNET//./ })
        container_subnet_split[${#container_subnet_split[@]}-1]=2
        master_container_ip=$(IFS=.; echo "${container_subnet_split[*]}")
        echo "master_container_ip:$master_container_ip"
        sed -i "/worker_extra_hosts/s/master:to_replace_container_ip/master:$master_container_ip/g" ./resources/docker-compose.yml
        ;;
      *)
        echo '非集群模式'
        ;;
      esac
}

# 集群环境的docker-compose配置
spark_cluster_config $identity_type

# 填充环境变量
sed -i "/FLOW_PORT/s/=.*/=$PYTHON_SERVICE_PORT/g" ./resources/variables.env
sed -i "/NGINX_PORT/s/=.*/=$NGINX_PORT/g" ./resources/variables.env
sed -i "/GATEWAY_PORT/s/=.*/=$GATEWAY_SERVICE_PORT/g" ./resources/variables.env
sed -i "/INTRANET_IP/s/=.*/=$INTRANET_IP/g" ./resources/variables.env

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