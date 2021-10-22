source ./wefe.cfg
export PWD=$(pwd)

# 前置处理
if [ $SPARK_MODE = "STANDALONE" ]
then
  echo '集群'
else
  echo '非集群模式，退出'
  exit 0
fi

# 同步镜像，采用ssh命令远程执行
# mkdir -p "$SPARK_CLUSTER_DATA_PATH/master"



# 同步配置
# 启动master
# 启动slave