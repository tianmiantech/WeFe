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


INPUT_ACTION=$1

# ******************
# 函数：拷贝单个python service到远程机器
# *****************
_cp_python_service(){
    # 参数
    to_host=$1
    to_path=$2

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
    ssh root@$to_host "docker stop $(docker ps -a | grep $WEFE_ENV | grep python | awk '{print $1}' | xargs)"
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

    echo "准备启动远程容器$to_host, 目录:$to_path, 身份:$identity, identity_name:$identity_name"
    ssh root@$to_host "cd $to_path && cd wefe_python_service && sh wefe_python_service_start.sh $identity $identity_name"
    sleep 1
}

# ******************
# 函数：拷贝python service到所有的远程机器
# *****************
cp_python_service_all(){

    # 创建远程master目录
    master_path=$SPARK_CLUSTER_DATA_PATH/master
    ssh root@$SPARK_MASTER "mkdir -p $master_path"

    # 拷贝python service 到master
    _cp_python_service $SPARK_MASTER $master_path

    # 拷贝到每个slave
    slaves=(`echo $SPARK_SLAVES | tr ',' ' '` )
    index=0
    for slave_ip in ${slaves[@]}
    do
       echo $slave_ip
       slave_path=$SPARK_CLUSTER_DATA_PATH"/slave_"$index

       # 远程创建slave目录
       ssh root@$SPARK_MASTER "mkdir -p $slave_path"
       echo $slave_path

       # 远程拷贝
       _cp_python_service $slave_ip $slave_path

       index=$[index + 1]
    done

}

# ******************
# 函数：启动集群python service
# *****************
start_cluster_python_service_all(){

    # 拷贝镜像到集群机器
    cp_python_service_all

    # master目录
    master_path=$SPARK_CLUSTER_DATA_PATH/master

    # start master
    _start_python_service $SPARK_MASTER $master_path master master
    sleep 3

    # 拷贝到每个slave
    slaves=(`echo $SPARK_SLAVES | tr ',' ' '` )
    index=0
    for slave_ip in ${slaves[@]}
    do
       echo $slave_ip
       slave_path=$SPARK_CLUSTER_DATA_PATH"/slave_"$index
       slave_name="slave_"$index

       # 远程启动slave
       _start_python_service $slave_ip $slave_path slave $slave_name

       index=$[index + 1]
    done

}


# ******************
# 函数：停止集群python service
# *****************
stop_cluster_python_service_all(){

    # master目录
    master_path=$SPARK_CLUSTER_DATA_PATH/master

    # start master
    _stop_python_service $SPARK_MASTER $master_path
    sleep 3

    # 拷贝到每个slave
    slaves=(`echo $SPARK_SLAVES | tr ',' ' '` )
    index=0
    for slave_ip in ${slaves[@]}
    do
       echo $slave_ip
       slave_path=$SPARK_CLUSTER_DATA_PATH"/slave_"$index

       # 远程启动slave
       _stop_python_service $slave_ip $slave_path

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
