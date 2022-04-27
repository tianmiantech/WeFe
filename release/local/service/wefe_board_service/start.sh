# 检查资源是否存在
JAR_NAME=wefe-board-service.jar

if [ $1 == 'update' ];then
  rm -rf $JAR_NAME
  wget $DOWNLOAD_BASE_PATH/PACKAGES/$JAR_NAME
fi

SERVICE=$(ps -ef | grep JAR_NAME | grep -v grep)
COUNT=$(echo $SERVICE | wc -l)
PID=$(echo $SERVICE | awk '{print $2}')

if [ $COUNT != 0 ]; then
  kill -9 $PID
fi

nohup java -jar $JAR_NAME &