source ./wefe.cfg
export PWD=$(pwd)

# 前置处理
if [ $SPARK_MODE = "STANDALONE" ]
then
  echo '当前为集群部署'
else
  echo '非集群模式，退出'
  exit 0
fi

# 检查依赖环境

# Consul 服务
CONSUL_SERVICE=$(docker ps | grep consul)
if [ ! $CONSUL_SERVICE ]]; then
  echo '未检测到 consul 服务，请参照集群部署文档部署 consul 服务'
fi

# 网络检测
OVERLAY_NETWORK=$(docker network ls | grep overlay)
if [[ ! $OVERLAY_NETWORK ]]; then
  echo '未检测到 Overlay 网络，请参照集群部署文档创建 Overlay 网络'
fi


INPUT_ACTION=$1

# ******************
# 函数：拷贝单个python service到远程机器
# *****************
_cp_python_service(){
    # 参数
    to_host=$1
    to_path=$2

    if [ ${#to_path} -gt 5 ]; then
      echo "删除远程目录${to_host}:${to_path}并重新创建"
      ssh root@$to_host "rm -rf $to_path"
      ssh root@$to_host "mkdir -p $to_path"
    fi

    echo "准备拷贝数据到$to_host的$to_path目录，请耐心等待..."
    scp wefe.cfg root@$to_host:$to_path
    scp config.properties root@$to_host:$to_path
    scp -r wefe_python_service root@$to_host:$to_path/ >/dev/null 2>&1
}

# ******************
# 函数：停止单个python service
# *****************
_stop_python_service(){
    # 参数
    to_host=$1
    to_path=$2

    echo "准备关闭远程容器$to_host, 目录:$to_path"
    # ssh root@$to_host "cd $to_path && cd wefe_python_service && sh wefe_python_service_stop.sh"
    to_stop=$(ssh root@$to_host "docker ps -a | grep $WEFE_ENV | grep python " | awk '{print $1}' | xargs)
    ssh root@$to_host "docker stop $to_stop"
    sleep 1
}


_remove_python_service(){
    # 参数
    to_host=$1
    to_path=$2

    echo "准备关闭远程容器$to_host, 目录:$to_path"
    # ssh root@$to_host "cd $to_path && cd wefe_python_service && sh wefe_python_service_stop.sh"
    to_remove=$(ssh root@$to_host "docker ps -a | grep $WEFE_ENV | grep python " | awk '{print $1}' | xargs)
    ssh root@$to_host "docker rm $to_remove"
    sleep 1
}

# ******************
# 函数：启动单个python service
# *****************
_start_python_service(){
    # 参数:
    to_host=$1
    to_path=$2
    identity=$3
    identity_name=$4
    container_ip=$5
    worker_ui_port=$6

    echo "准备启动远程容器$to_host, 目录:$to_path, 身份:$identity, identity_name:$identity_name"
    ssh root@$to_host "cd $to_path && cd wefe_python_service && sh wefe_python_service_start.sh $identity $identity_name $container_ip $to_host $worker_ui_port"
    sleep 1
}

# ******************
# 函数：根据container_index生成容器ip
# *****************
_generate_container_ip(){
    container_index=$1

    container_subnet_split=(${CONTAINER_SUBNET//./ })
    container_subnet_split[${#container_subnet_split[@]}-1]=$container_index
    container_ip=$(IFS=.; echo "${container_subnet_split[*]}")
    echo $container_ip
}

# ******************
# 函数：拷贝 python service 到所有的远程机器
# *****************
cp_python_service_all(){

    # 拷贝数据到远程 master 目录
    REMOTE_MASTER_PATH=$SPARK_CLUSTER_DATA_PATH/master
    # ssh root@$SPARK_MASTER "mkdir -p $master_path"

    # 拷贝python service 到master
    _cp_python_service $SPARK_MASTER $REMOTE_MASTER_PATH

    # 拷贝到每个 worker
    workers=(`echo $SPARK_WORKERS | tr ',' ' '` )
    index=0
    for worker in ${workers[@]}
    do
       # echo $worker
       worker_path=$SPARK_CLUSTER_DATA_PATH"/worker_"$index

       # 远程创建 worker 目录
       # ssh root@$worker "mkdir -p $worker_path"
       # echo $worker_path

       # 远程拷贝
       _cp_python_service $worker $worker_path

       index=$[index + 1]
    done

}

# ******************
# 函数：启动集群 python service
# *****************
start_cluster(){

    # 拷贝镜像到集群机器
    cp_python_service_all

    # container_index，默认设置从2开始
    container_index=2
    master_container_ip=$(_generate_container_ip $container_index)

    # master目录
    REMOTE_MASTER_PATH=$SPARK_CLUSTER_DATA_PATH/master

    # start master
    _start_python_service $SPARK_MASTER $REMOTE_MASTER_PATH master master $master_container_ip no_worder_ui_port
    sleep 3

    # 拷贝到每个 worker
    workers=(`echo $SPARK_WORKERS | tr ',' ' '` )
    worker_ui_ports=(`echo $SPARK_WORKER_UI_PORTS | tr ',' ' '` )
    index=0
    for worker in ${workers[@]}
    do
       echo $worker
       worker_path=$SPARK_CLUSTER_DATA_PATH"/worker_"$index
       worker_name="worker_"$index
       container_index=$[container_index + 1]
       worker_container_ip=$(_generate_container_ip $container_index)
       worker_ui_port=${worker_ui_ports[$index]}

       # 远程启动 worker
       _start_python_service $worker $worker_path worker $worker_name $worker_container_ip $worker_ui_port

       index=$[index + 1]
    done

}


# ******************
# 函数：停止集群python service
# *****************
stop_cluster(){

    # master目录
    master_path=$SPARK_CLUSTER_DATA_PATH/master

    # start master
    _stop_python_service $SPARK_MASTER $master_path
    sleep 3

    # 拷贝到每个 worker
    workers=(`echo $SPARK_WORKERS | tr ',' ' '` )
    index=0
    for worker in ${workers[@]}
    do
       echo $worker
       worker_path=$SPARK_CLUSTER_DATA_PATH"/worker_"$index

       # 远程启动 worker
       _stop_python_service $worker $worker_path

       index=$[index + 1]
    done

}

remove_cluster(){

    # master目录
    master_path=$SPARK_CLUSTER_DATA_PATH/master

    # start master
    _remove_python_service $SPARK_MASTER $master_path
    sleep 3

    # 拷贝到每个 worker
    workers=(`echo $SPARK_WORKERS | tr ',' ' '` )
    index=0
    for worker in ${workers[@]}
    do
       echo $worker
       worker_path=$SPARK_CLUSTER_DATA_PATH"/worker_"$index

       # 远程启动 worker
       _remove_python_service $worker $worker_path

       index=$[index + 1]
    done

}


#case $INPUT_ACTION in
#    start)
#        start_python_service_all
#        ;;
#    stop)
#        stop_python_service_all
#        ;;
#    *)
#        echo "Please Input a Legal Action"
#        echo "eg. {start | stop | restart | help}"
#        exit -1
#esac
