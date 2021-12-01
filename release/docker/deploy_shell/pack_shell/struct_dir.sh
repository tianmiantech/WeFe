#!/bin/bash

# 构造部署的目录

# 创建目录一个目录用于创建 Docker 容器和镜像
export WORKSPACE=$1
export BASE_DIR=$2
export GIT_BRANCH=$3
export WORK_DIR=$BASE_DIR/$(date +%Y%m%d)/$GIT_BRANCH
export IMAGE_WORK_DIR=$WORK_DIR/docker_image
export SERVICE_WORK_DIR=$WORK_DIR/docker_service

# 更新挂载文件的目录
export MYSQL_FILE_LASTED=$WORKSPACE/deploy_package_resource/wefe_board.sql
export MYSQL_MOUNT_FILE=$SERVICE_WORK_DIR/wefe_middleware_service/resources/mount/sql/wefe_board.sql

export INDEX_FILE_LASTED=$WORKSPACE/deploy_package_resource/html/board-website/index.html
export INDEX_MOUNT_FILE=$SERVICE_WORK_DIR/wefe_board_website/resources/mount/index.html


# 创建工作目录
rm -rf $WORK_DIR
mkdir -p $WORK_DIR

# 将项目中的部署目录结构复制到工作目录中
# 拷贝 docker 工作目录代码
cp -r $WORKSPACE/release/docker/docker_image $WORK_DIR
cp -r $WORKSPACE/release/docker/docker_service $WORK_DIR
cp -r $WORKSPACE/deploy_package_resource $WORK_DIR

echo '所有资源打包在 $WORKSPACE/deploy_package_resource 目录下'

echo 'UPDATE MOUNT FILE'
# 更新挂载的文件

# SQL 文件
cp -f $MYSQL_FILE_LASTED $MYSQL_MOUNT_FILE
# index.html 文件
cp -f $INDEX_FILE_LASTED $INDEX_MOUNT_FILE

echo 'UPDATE MOUNT FILE FINISHED'

# 分发文件到相应的 docker_image 目录下
echo 'SEND RESOURCES TO DOCKER IMAGE'
cp -f $WORKSPACE/deploy_package_resource/html.tar $IMAGE_WORK_DIR/app/wefe_board_website
cp -f $WORKSPACE/deploy_package_resource/board-service.jar $IMAGE_WORK_DIR/app/wefe_board_service
cp -f $WORKSPACE/deploy_package_resource/gateway.jar $IMAGE_WORK_DIR/app/wefe_gateway_service
cp -rf $WORKSPACE/deploy_package_resource/python_project $IMAGE_WORK_DIR/app/wefe_python_service
cp -rf $WORKSPACE/deploy_package_resource/python_project $IMAGE_WORK_DIR/app/wefe_python_gpu_service
echo 'SEND RESOURCES TO DOCKER IMAGE FINISHED'

# 分发文件到相应的 docker_service 的 resources 目录下，挂载支持动态更新
echo 'SEND RESOURCES TO MOUNT DIR'
cp -rf $WORKSPACE/deploy_package_resource/html $SERVICE_WORK_DIR/wefe_board_website/resources/mount/
cp -f $WORKSPACE/deploy_package_resource/board-service.jar $SERVICE_WORK_DIR/wefe_board_service/resources/mount/board-service.jar
cp -f $WORKSPACE/deploy_package_resource/gateway.jar $SERVICE_WORK_DIR/wefe_gateway_service/resources/mount/gateway.jar
cp -rf $WORKSPACE/deploy_package_resource/python_project $SERVICE_WORK_DIR/wefe_python_service/resources/mount/
cp -rf $WORKSPACE/deploy_package_resource/python_project $SERVICE_WORK_DIR/wefe_python_gpu_service/resources/mount/
echo 'SEND RESOURCES TO MOUNT DIR FINISHED'