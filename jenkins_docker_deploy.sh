#!/bin/bash

# 入参检查

source ./release/docker/docker_service/wefe.cfg

if [ -z $1 ]; then
    echo "请输入 Jenkins 的工作目录空间"
    exit 0
fi

export WEFE_VERSION=$WEFE_VERSION
export WORKSPACE=$1
export GIT_BRANCH=$2
export DEPLOY_MODE=$3
# 服务器上生成存放部署目录的
export BASE_DIR=/data/jenkins_docker_deploy
export SHELL_DIR=$WORKSPACE'/release/docker/deploy_shell'
# oss 上传工具地址
export OSS_TOOL=/data/aliyun_oss/oss
# FC 环境部署的工作空间
export FC_WORKSPACE=/data/jenkins_workspace/workspace/wefe_fc_nas

# docker_dir -> docker_image 镜像打包目录 ; docker_service 容器启动目录
# shell_dir -> pack_shell 打包脚本目录 ; struct_shell 构造脚本目录 ; tool 相关工具目录

if [[ ! -d $SHELL_DIR ]]; then
  echo '脚本目录: '$SHELL_DIR
  echo '部署脚本文件夹不存在，请检查部署分支是否正确'
  exit 1
fi

# 更新 FC 环境，依赖于 wefe_fc_nas 项目部署
cp -r $FC_WORKSPACE/common/python/calculation/fc/function/wefe-fc/.fun $WORKSPACE/common/python/calculation/fc/function/wefe-fc

echo ' ___       __   _______   ________ _______      '
echo '|\  \     |\  \|\  ___ \ |\  _____\\  ___ \     '
echo '\ \  \    \ \  \ \   __/|\ \  \__/\ \   __/|    '
echo ' \ \  \  __\ \  \ \  \_|/_\ \   __\\ \  \_|/__  '
echo '  \ \  \|\__\_\  \ \  \_|\ \ \  \_| \ \  \_|\ \ '
echo '   \ \____________\ \_______\ \__\   \ \_______\'
echo '    \|____________|\|_______|\|__|    \|_______|'
echo ''

echo ''
echo '====== PACK ALL ======'
# 项目打包
cd $SHELL_DIR/pack_shell
sh pack_all.sh $WORKSPACE $SHELL_DIR
if [[ $? == 1 ]];then
  echo '项目打包异常，暂停脚本'
  exit 1;
fi
echo '====== DONE ======'
echo ''

echo '====== STRUCT DIR ======'
# 目录构建
cd $SHELL_DIR/pack_shell
sh struct_dir.sh $WORKSPACE $BASE_DIR $GIT_BRANCH
if [[ $? == 1 ]];then
  echo '目录构建异常，暂停脚本'
  exit 1;
fi
echo '====== DONE ======'
echo ''

if [[ $DEPLOY_MODE == 'pack' ]]; then
  echo '====== ORIGIN DEPLOY MODE ======'
  cd $WORKSPACE
  tar -cvf deploy_package_resource.tar deploy_package_resource > /dev/null
  ./$OSS_TOOL cp ./deploy_package_resource.tar oss://welab-wefe-release/
  echo '====== DONE ======'
  echo '====== DOWNLOAD LINK https://welab-wefe-release.oss-cn-shenzhen.aliyuncs.com/deploy_package_resource.tar ======'
  exit 1
fi

echo '====== DOCKER BUILD ======'
# 生成 Docker 镜像
cd $SHELL_DIR/docker_shell
sh docker_build.sh $BASE_DIR $GIT_BRANCH $WEFE_VERSION
if [[ $? == 1 ]];then
  echo '镜像打包异常，暂停脚本'
  exit 1;
fi
echo '====== DONE ======'
echo ''

echo '====== DOCKER SAVE ======'
# 保存 Docker 镜像，并放置在对应项目的 Resource 目录下
cd $SHELL_DIR/docker_shell
sh docker_save.sh $BASE_DIR $GIT_BRANCH $WEFE_VERSION
if [[ $? == 1 ]];then
  echo '镜像保存异常，暂停脚本'
  exit 1;
fi
echo '====== DONE ======'