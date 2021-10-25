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
# 函数：拷贝python service到远程机器
# *****************
cp_python_service(){
    # $1：ip
    # $2: to path
    echo "准备拷贝数据到$1的$2目录"
    scp wefe.cfg root@$1:$2
    scp config.properties root@$1:$2
    scp -r wefe_python_service root@$1:$2/
}

# ******************
# 函数：停止python service
# *****************
stop_python_service(){
    # 参数:
    # $1: ip
    # S2: path
    echo "准备关闭远程容器$1, 目录:$2"
    ssh root@$1 "cd $2 && cd wefe_python_service && sh wefe_python_service_stop.sh"
    sleep 1
}

# ******************
# 函数：启动python service
# *****************
start_python_service(){
    # 参数:
    # $1: ip
    # S2: path
    # s3: identity: master/slave
    # s4: identity_name:
    echo "准备启动远程容器$1, 目前:$2, 身份:$3"
    ssh root@$1 "cd $2 && cd wefe_python_service && sh wefe_python_service_start.sh $3 $4"
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
    cp_python_service $SPARK_MASTER $master_path

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
       cp_python_service $slave_ip $slave_path

       index=$[index + 1]
    done

}

# ******************
# 函数：启动python service
# *****************
start_python_service_all(){

    # 拷贝镜像到集群机器
    cp_python_service_all

    # master目录
    master_path=$SPARK_CLUSTER_DATA_PATH/master

    # start master
    start_python_service $SPARK_MASTER $master_path master master
    sleep 3

    # 拷贝到每个slave
    slaves=(`echo $SPARK_SLAVES | tr ',' ' '` )
    index=0
    for slave_ip in ${slaves[@]}
    do
       echo $slave_ip
       slave_path=$SPARK_CLUSTER_DATA_PATH"/slave_"$index
       slave_name="/slave_"$index

       # 远程启动slave
       start_python_service $slave_ip $slave_path slave $slave_name

       index=$[index + 1]
    done

}


# ******************
# 函数：停止python service
# *****************
stop_python_service_all(){

    # master目录
    master_path=$SPARK_CLUSTER_DATA_PATH/master

    # start master
    stop_python_service $SPARK_MASTER $master_path
    sleep 3

    # 拷贝到每个slave
    slaves=(`echo $SPARK_SLAVES | tr ',' ' '` )
    index=0
    for slave_ip in ${slaves[@]}
    do
       echo $slave_ip
       slave_path=$SPARK_CLUSTER_DATA_PATH"/slave_"$index

       # 远程启动slave
       stop_python_service $slave_ip $slave_path

       index=$[index + 1]
    done

}


case $INPUT_ACTION in
    start)
        start_python_service_all
        ;;
    stop)
        stop_python_service_all
        ;;
    *)
        echo "Please Input a Legal Action"
        echo "eg. {start | stop | restart | help}"
        exit -1
esac

#
## ****************************
## 同步镜像，采用ssh命令远程执行
## ***************************
#
## 创建远程master目录
#master_path=$SPARK_CLUSTER_DATA_PATH/master
#ssh root@$SPARK_MASTER "mkdir -p $master_path"
## 拷贝python service 到master
#cp_python_service $SPARK_MASTER $master_path
#
## 启动master
#start_python_service $SPARK_MASTER $master_path master
#
#sleep 3
#
## 拷贝到每个slave
#slaves=(`echo $SPARK_SLAVES | tr ',' ' '` )
#index=0
#for slave_ip in ${slaves[@]}
#do
#   echo $slave_ip
#   slave_path=$SPARK_CLUSTER_DATA_PATH"/slave_"$index
#
#   # 远程创建slave目录
#   ssh root@$SPARK_MASTER "mkdir -p $slave_path"
#   echo $slave_path
#
#   # 远程拷贝
#   cp_python_service $slave_ip $slave_path
#
#   # 远程启动slave
#   start_python_service $slave_ip $slave_path slave
#
#   index=$[index + 1]
#done

# 同步配置
# 启动master
# 启动slave