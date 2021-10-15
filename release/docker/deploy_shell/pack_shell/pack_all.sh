#!/bin/bash

# 项目打包总脚本

export WORKSPACE=$1
export SHELL_DIR=$2

# mvn 命令用环境变量作为命令参数会报错
# 相对路径变化后需要修修改 MVN 打包中的对应参数
export BOARD_SERVICE_RELATIVE_PATH=board/board-service
export GATEWAY_RELATIVE_PATH=gateway
export BOARD_WEBSITE_RELATIVE_PATH=board/board-website

# 激活打包所需的环境
source /etc/profile 

# Method
function check_dir() 
{ 
    if [[ ! -d $1 ]]; then
        echo '文件夹 '$1' 不存在，请检查此模块打包流程是否正常'
        return 0
    fi
}

function check_file() 
{ 
    if [[ ! -f $1 ]]; then
        echo '文件 '$1' 不存在，请检查此模块打包流程是否正常'
        return 0
    fi
}

echo '###### PYTHON ######'

# go 编译
echo 'GO Build'
cd $WORKSPACE/common/python/calculation/fc/go/src
echo '准备编译go动态链接库'
sh build.sh
echo 'GO Build FINISHED'

# python 项目整合
cd $WORKSPACE
rm -rf $WORKSPACE/python_project
mkdir $WORKSPACE/python_project
find ./common/ -name 'python' |xargs tar czf ./common-python.tgz
tar zxf common-python.tgz -C $WORKSPACE/python_project
cp -r $WORKSPACE/flow $WORKSPACE/python_project
cp -r $WORKSPACE/kernel $WORKSPACE/python_project
#
cp $SHELL_DIR/tool/config.properties $WORKSPACE/python_project
cp $SHELL_DIR/tool/requirements.txt $WORKSPACE/python_project
rm -f $WORKSPACE/python_project/flow/service.sh
cp -f $SHELL_DIR/tool/service.sh $WORKSPACE/python_project/flow/service.sh
# 函数计算环境
cp -r $WORKSPACE/common/python/calculation/fc $WORKSPACE/python_project/common/python/calculation/

echo '###### PYTHON FINISHED ######'
echo '###### JAVA ######'

# Java 项目加密打包
# MAVEN_HOME 环境变量
cd $WORKSPACE
echo 'PACK BOARD-SERVICE'
rm -rf $WORKSPACE/board/board-service/target
# 生成加密后的 jar 包 board/board-service/target/board-service_proguard_base.jar
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl board/board-service > /dev/null
if [[ ! -f $WORKSPACE/board/board-service/target/wefe-board-service.jar ]]; then
    echo '文件 board-service.jar 不存在，请检查此模块打包流程是否正常'
    exit 1
fi

echo 'FINISHED'
echo 'PACK GATEWAY'
# 生成加密后的 jar 包 gateway/target/gateway_proguard_base.jar
rm -rf $WORKSPACE/gateway/target
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl gateway > /dev/null
if [[ ! -f $WORKSPACE/gateway/target/wefe-gateway.jar ]]; then
    echo '文件 wefe-gateway.jar 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo '###### JAVA FINISHED ######'
echo '###### NPM ######'

# 前端项目打包
cd $WORKSPACE/$BOARD_WEBSITE_RELATIVE_PATH
# 防止升级版本造成命令不兼容
echo 'PACK BOARD-WEBSITE'
rm -rf $WORKSPACE/board/board-website/dist
npm i npm@6.9.0 -g > /dev/null
npm install > /dev/null
# board/board-website/dist
npm run build -- prod=board-website > /dev/null
sed -i '/<title>/i<script>window.clientApi = {env: "prod", baseUrl: "http://127.0.0.1/board-service", prefixPath: "/board-website/"};</script>' dist/board-website/index.html
if ! $(check_dir $WORKSPACE/board/board-website/dist); then
    exit 1
fi
echo 'FINISHED'

echo '###### NPM FINISHED ######'
echo '###### INTEGRATION ######'

# 将打包后的资源统一放置在项目根目录的 deploy_package_resource 下
rm -rf $WORKSPACE/deploy_package_resource
mkdir $WORKSPACE/deploy_package_resource

cp -r $WORKSPACE/python_project $WORKSPACE/deploy_package_resource
cp $WORKSPACE/board/board-service/target/wefe-board-service.jar $WORKSPACE/deploy_package_resource/board-service.jar
cp $WORKSPACE/gateway/target/wefe-gateway.jar $WORKSPACE/deploy_package_resource/gateway.jar
cp -r $WORKSPACE/board/board-website/dist $WORKSPACE/deploy_package_resource/html

cd $WORKSPACE/deploy_package_resource
tar -cf html.tar html

# 数据库文件
cp $WORKSPACE/documents/数据库设计/board/wefe_board.sql $WORKSPACE/deploy_package_resource

echo '###### INTEGRATION FINISHED ######'
