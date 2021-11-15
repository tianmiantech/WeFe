#!/bin/bash

export BASE_DIR=$1
export GIT_BRANCH=$2
export WEFE_VERSION=$3
export IMAGE_WORK_DIR=$BASE_DIR/$(date +%Y%m%d)/$GIT_BRANCH/docker_image
export SERVICE_WORK_DIR=$BASE_DIR/$(date +%Y%m%d)/$GIT_BRANCH/docker_service


echo "SAVING BOARD SERVICE"
cd $SERVICE_WORK_DIR/wefe_board_service/resources
rm -f wefe_board_service_$WEFE_VERSION.tar
sudo docker save -o wefe_board_service_$WEFE_VERSION.tar wefe_board_service:$WEFE_VERSION
echo "Board SERVICE SAVED"

echo "SAVING BOARD WEBSITE"
cd $SERVICE_WORK_DIR/wefe_board_website/resources
rm -f wefe_board_website_$WEFE_VERSION.tar
sudo docker save -o wefe_board_website_$WEFE_VERSION.tar wefe_board_website:$WEFE_VERSION
echo "BOARD WEBSITE SAVED"

echo "SAVING PYTHON SERVICE"
cd $SERVICE_WORK_DIR/wefe_python_service/resources
rm  -f wefe_python_service_$WEFE_VERSION.tar
sudo docker save -o wefe_python_service_$WEFE_VERSION.tar wefe_python_service:$WEFE_VERSION
echo "PYTHON SERVICE SAVED"

echo "SAVING GPU PYTHON SERVICE"
cd $SERVICE_WORK_DIR/wefe_python_gpu_service/resources
rm  -f wefe_python_gpu_service_$WEFE_VERSION.tar
sudo docker save -o wefe_python_gpu_service_$WEFE_VERSION.tar wefe_python_gpu_service:$WEFE_VERSION
echo "GPU PYTHON SERVICE SAVED"

echo "SAVING GATEWAY SERVICE"
cd $SERVICE_WORK_DIR/wefe_gateway_service/resources
rm -f wefe_gateway_service_$WEFE_VERSION.tar
sudo docker save -o wefe_gateway_service_$WEFE_VERSION.tar wefe_gateway_service:$WEFE_VERSION
echo "GATEWAY SERVICE SAVED"

cd $SERVICE_WORK_DIR

# 排除 gpu 版本 Python 包，另外打包
sudo tar -cf welab_wefe_${WEFE_VERSION}.tar * --exclude=wefe_python_gpu_service
cd wefe_python_gpu_service
sudo tar -cf wefe_python_gpu_service_$WEFE_VERSION.tar

# 单独打包 gpu 镜像
cp wefe_python_gpu_service_$WEFE_VERSION.tar /data/jenkins_docker_deploy/
cd $SERVICE_WORK_DIR
cp welab_wefe_${WEFE_VERSION}.tar /data/jenkins_docker_deploy/

echo 'The Final Package Build Success'
