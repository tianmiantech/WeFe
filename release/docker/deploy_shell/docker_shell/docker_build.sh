#!/bin/bash

export BASE_DIR=$1
export GIT_BRANCH=$2
export WEFE_VERSION=$3
export IMAGE_WORK_DIR=$BASE_DIR/$(date +%Y%m%d)/$GIT_BRANCH/docker_image
export SERVICE_WORK_DIR=$BASE_DIR/$(date +%Y%m%d)/$GIT_BRANCH/docker_service

# 检查基础镜像是否存在
JAVA_BASE_IMAGE=$(sudo docker image ls | grep wefe_java_base | awk '{print $3}' | head -n 1)
if [ ! $JAVA_BASE_IMAGE ]; then
  rm -f $BASE_DIR/base_env/wefe_java_base/Dockerfile
  cp $IMAGE_WORK_DIR/env/jdk_1.8/Dockerfile $BASE_DIR/base_env/wefe_java_base/
  cd $BASE_DIR/base_env/wefe_java_base
  sudo docker build -t wefe_java_base .
  echo 'DONE'
fi

PYTHON_BASE_IMAGE=$(sudo docker image ls | grep wefe_python_base | awk '{print $3}' | head -n 1)
if [ ! $PYTHON_BASE_IMAGE ]; then
  echo 'WEFE_PYTHON_BASE BUILDING'
  rm -f $BASE_DIR/base_env/wefe_python_base/Dockerfile
  cp $IMAGE_WORK_DIR/env/python_3.7/Dockerfile $BASE_DIR/base_env/wefe_python_base/
  cd $BASE_DIR/base_env/wefe_python_base
  sudo docker build -t wefe_python_base .
  echo 'DONE'
fi

# gpu 部署镜像制作
PYTHON_GPU_BASE_IMAGE=$(sudo docker image ls | grep wefe_python_gpu_base | awk '{print $3}' | head -n 1)
if [ ! $PYTHON_GPU_BASE_IMAGE ]; then
  echo 'PYTHON_GPU_BASE_IMAGE BUILDING'
  rm -f $BASE_DIR/base_env/wefe_python_gpu_base/Dockerfile
  cp $IMAGE_WORK_DIR/env/gpu_python_3.7/Dockerfile $BASE_DIR/base_env/wefe_python_gpu_base/
  cd $BASE_DIR/base_env/wefe_python_gpu_base
  sudo docker build -t wefe_python_gpu_base .
  echo 'DONE'
fi

# 频繁操作 Docker 容易造成卡顿，操作前重启一下
sudo systemctl restart docker

# 检查容器运行状态
CONTAINER=$(sudo docker ps -a| awk '{print $2}' | grep wefe | grep $WEFE_VERSION | head -n 1)

echo 'CONTAINERS'
sudo docker ps -a | grep wefe | grep $WEFE_VERSION

if [ $CONTAINER ]; then
  cd $SERVICE_WORK_DIR
  sudo sh wefe_service.sh stop
  sudo sh wefe_service.sh remove
fi

sudo systemctl restart docker

# 删除系统容器
IMAGE=$(sudo docker image ls | grep wefe | grep $WEFE_VERSION | awk '{print $3}' | head -n 1)

echo 'IMAGES'
sudo docker images | grep wefe | grep $WEFE_VERSION


if [ $IMAGE ]; then
  sudo docker rmi $(sudo docker image ls | grep wefe | grep $WEFE_VERSION | awk '{print $3}' | xargs)
fi

sudo systemctl restart docker

# 生成镜像

echo 'WEFE_BOARD_SERVICE BUILDING'
cd $IMAGE_WORK_DIR/app/wefe_board_service
sudo docker build -t wefe_board_service:$WEFE_VERSION .
echo 'DONE'

echo 'WEFE_BOARD_WEBSITE BUILDING'
cd $IMAGE_WORK_DIR/app/wefe_board_website
sudo docker build -t wefe_board_website:$WEFE_VERSION .
echo 'DONE'

echo 'WEFE_GATEWAY_SERVICE BUILDING'
cd $IMAGE_WORK_DIR/app/wefe_gateway_service
sudo docker build -t wefe_gateway_service:$WEFE_VERSION .
echo 'DONE'

echo 'WEFE_PYTHON_SERVICE BUILDING'
cd $IMAGE_WORK_DIR/app/wefe_python_service
sudo docker build -t wefe_python_service:$WEFE_VERSION .
echo 'DONE'

echo 'WEFE_PYTHON_GPU_SERVICE BUILDING'
cd $IMAGE_WORK_DIR/app/wefe_python_gpu_service
sudo docker build -t wefe_python_gpu_service:$WEFE_VERSION .
echo 'DONE'

#echo 'WEFE_FUSION_SERVICE BUILDING'
#cd $IMAGE_WORK_DIR/app/wefe_fusion_service
#sudo docker build -t wefe_fusion_service:$WEFE_VERSION .
#echo 'DONE'
#
#echo 'WEFE_FUSION_WEBSITE BUILDING'
#cd $IMAGE_WORK_DIR/app/wefe_fusion_website
#sudo docker build -t wefe_fusion_website:$WEFE_VERSION .
#echo 'DONE'
#
#echo 'WEFE_MANAGER_SERVICE BUILDING'
#cd $IMAGE_WORK_DIR/app/wefe_manager_service
#sudo docker build -t wefe_manager_service:$WEFE_VERSION .
#echo 'DONE'
#
#echo 'WEFE_MANAGER_WEBSITE BUILDING'
#cd $IMAGE_WORK_DIR/app/wefe_manager_website
#sudo docker build -t wefe_manager_website:$WEFE_VERSION .
#echo 'DONE'
#
#echo 'WEFE_SERVING_SERVICE BUILDING'
#cd $IMAGE_WORK_DIR/app/wefe_serving_service
#sudo docker build -t wefe_serving_service:$WEFE_VERSION .
#echo 'DONE'
#
#echo 'WEFE_SERVING_WEBSITE BUILDING'
#cd $IMAGE_WORK_DIR/app/wefe_serving_website
#sudo docker build -t wefe_serving_website:$WEFE_VERSION .
#echo 'DONE'
#
#echo 'WEFE_BLOCKCHAIN_DATA_SYNC BUILDING'
#cd $IMAGE_WORK_DIR/app/wefe_blockchain_data_sync
#sudo docker build -t wefe_blockchain_data_sync:$WEFE_VERSION .
#echo 'DONE'
#
#echo 'WEFE_UNION_SERVICE BUILDING'
#cd $IMAGE_WORK_DIR/app/wefe_union_service
#sudo docker build -t wefe_union_service:$WEFE_VERSION .
#echo 'DONE'

# 覆盖挂载文件
