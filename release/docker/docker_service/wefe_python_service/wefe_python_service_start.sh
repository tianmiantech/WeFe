
#!/bin/bash

# 导入配置
source ../wefe.cfg

identity_type=$1
identity_name=$2
cluster_container_ip=$3
cluster_host_ip=$4
slave_ui_port=$5

# ******************
# 定义函数：docker-compose配置初始化
# *****************
spark_cluster_config(){
    echo "当前type:$identity_type"
    case $identity_type in
      master)
        cp -f resources/template/docker-compose-master.yml.template resources/docker-compose.yml
        sed -i "/spark_submit_port/s/7077:7077/$SPARK_SUBMIT_PORT:7077/g" ./resources/docker-compose.yml
        sed -i "/spark_master_ui_port/s/8080:8080/$SPARK_MASTER_UI_PORT:8080/g" ./resources/docker-compose.yml
        sed -i "/master_public_dns/s/SPARK_PUBLIC_DNS=to_replace_ip/SPARK_PUBLIC_DNS=$SPARK_MASTER/g" ./resources/docker-compose.yml
        sed -i "/master_container_ip/s/ipv4_address: to_replace_ip/ipv4_address: $cluster_container_ip/g" ./resources/docker-compose.yml
        ;;
      slave)
        cp -f resources/template/docker-compose-slave.yml.template resources/docker-compose.yml
        sed -i "/slave_name/s/wefe_python_service_slave/wefe_python_service_$identity_name/g" ./resources/docker-compose.yml
        sed -i "/slave_public_dns/s/SPARK_PUBLIC_DNS=to_replace_ip/SPARK_PUBLIC_DNS=$cluster_host_ip/g" ./resources/docker-compose.yml
        sed -i "/slave_container_ip/s/ipv4_address: to_replace_ip/ipv4_address: $cluster_container_ip/g" ./resources/docker-compose.yml

        # worker ui port
        sed -i "/spark_slave_ui_port/s/8081:8081/$slave_ui_port:8081/g" ./resources/docker-compose.yml

        # replace host
        container_subnet_split=(${CONTAINER_SUBNET//./ })
        container_subnet_split[${#container_subnet_split[@]}-1]=2
        master_container_ip=$(IFS=.; echo "${container_subnet_split[*]}")
        echo "master_container_ip:$master_container_ip"
        sed -i "/slave_extra_hosts/s/master:to_replace_container_ip/master:$master_container_ip/g" ./resources/docker-compose.yml
        ;;
      gpu)
        echo 'GPU 加速模式'
        cp -f resources/template/docker-compose-gpu.yml.template resources/docker-compose.yml
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
if [ ${ACCELERATION,,} == 'gpu' ];then
  echo "开始加载 gpu python 离线镜像"
  docker load < resources/wefe_python_gpu_service_$WEFE_VERSION\.tar
  echo "加载 gpu python 离线镜像完成"
else
  echo "开始加载 python 离线镜像"
  docker load < resources/wefe_python_service_$WEFE_VERSION\.tar
  echo "加载 python 离线镜像完成"
fi

# 启动 flow 镜像
docker-compose -p $WEFE_ENV -f resources/docker-compose.yml up -d