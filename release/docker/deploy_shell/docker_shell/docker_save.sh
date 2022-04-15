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

echo "SAVING GATEWAY SERVICE"
cd $SERVICE_WORK_DIR/wefe_gateway_service/resources
rm -f wefe_gateway_service_$WEFE_VERSION.tar
sudo docker save -o wefe_gateway_service_$WEFE_VERSION.tar wefe_gateway_service:$WEFE_VERSION
echo "GATEWAY SERVICE SAVED"

echo "packing welab_wefe tar "
cd $SERVICE_WORK_DIR
# 排除 gpu 版本 Python 包，另外打包
sudo tar -cf welab_wefe_$WEFE_VERSION.tar *
echo "packed welab_wefe tar!"

echo "SAVING FUSION SERVICE"
cd $SERVICE_WORK_DIR/wefe_fusion_service/resources
rm -f wefe_fusion_service_$WEFE_VERSION.tar
sudo docker save -o wefe_fusion_service_$WEFE_VERSION.tar wefe_fusion_service:$WEFE_VERSION
echo "FUSION SERVICE SAVED"

echo "SAVING FUSION WEBSITE"
cd $SERVICE_WORK_DIR/wefe_fusion_website/resources
rm -f wefe_fusion_website_$WEFE_VERSION.tar
sudo docker save -o wefe_fusion_website_$WEFE_VERSION.tar wefe_fusion_website:$WEFE_VERSION
echo "FUSION WEBSITE SAVED"

echo "SAVING MANAGER SERVICE"
cd $SERVICE_WORK_DIR/wefe_manager_service/resources
rm -f wefe_manager_service_$WEFE_VERSION.tar
sudo docker save -o wefe_manager_service_$WEFE_VERSION.tar wefe_manager_service:$WEFE_VERSION
echo "MANAGER SERVICE SAVED"

echo "SAVING MANAGER WEBSITE"
cd $SERVICE_WORK_DIR/wefe_manager_website/resources
rm -f wefe_manager_website_$WEFE_VERSION.tar
sudo docker save -o wefe_manager_website_$WEFE_VERSION.tar wefe_manager_website:$WEFE_VERSION
echo "MANAGER WEBSITE SAVED"

echo "SAVING SERVING SERVICE"
cd $SERVICE_WORK_DIR/wefe_serving_service/resources
rm -f wefe_serving_service_$WEFE_VERSION.tar
sudo docker save -o wefe_serving_service_$WEFE_VERSION.tar wefe_serving_service:$WEFE_VERSION
echo "SERVING SERVICE SAVED"

echo "SAVING SERVING WEBSITE"
cd $SERVICE_WORK_DIR/wefe_serving_website/resources
rm -f wefe_serving_website_$WEFE_VERSION.tar
sudo docker save -o wefe_serving_website_$WEFE_VERSION.tar wefe_serving_website:$WEFE_VERSION
echo "SERVING WEBSITE SAVED"

echo "SAVING BLOCKCHAIN_DATA_SYNC"
cd $SERVICE_WORK_DIR/wefe_blockchain_data_sync/resources
rm -f wefe_blockchain_data_sync_$WEFE_VERSION.tar
sudo docker save -o wefe_blockchain_data_sync_$WEFE_VERSION.tar wefe_blockchain_data_sync:$WEFE_VERSION
echo "BLOCKCHAIN_DATA_SYNC SAVED"

echo "SAVING UNION SERVICE"
cd $SERVICE_WORK_DIR/wefe_union_service/resources
rm -f wefe_union_service_$WEFE_VERSION.tar
sudo docker save -o wefe_union_service_$WEFE_VERSION.tar wefe_union_service:$WEFE_VERSION
echo "UNION SERVICE SAVED"

# 单独打包 gpu 镜像
echo "SAVING GPU PYTHON SERVICE"
cd $SERVICE_WORK_DIR/wefe_python_service/resources
rm  -f wefe_python_gpu_service_$WEFE_VERSION.tar
sudo docker save -o wefe_python_gpu_service_$WEFE_VERSION.tar wefe_python_gpu_service:$WEFE_VERSION
echo "GPU PYTHON SERVICE SAVED"

echo "COPY GPU PYTHON SERVICE"
cp wefe_python_gpu_service_$WEFE_VERSION.tar /data/jenkins_docker_deploy/
cd $SERVICE_WORK_DIR
cp welab_wefe_${WEFE_VERSION}.tar /data/jenkins_docker_deploy/
echo "COPY GPU PYTHON SERVICE FINISH"

echo 'The Final Package Build Success'
