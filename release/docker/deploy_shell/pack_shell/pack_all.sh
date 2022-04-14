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

rm -f $WORKSPACE/python_project/config.properties
cp $SHELL_DIR/tool/config.properties $WORKSPACE/python_project
rm -f $WORKSPACE/python_project/requirements.txt
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
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl board/board-service
if [[ ! -f $WORKSPACE/board/board-service/target/wefe-board-service.jar ]]; then
    echo '文件 board-service.jar 不存在，请检查此模块打包流程是否正常'
    exit 1
fi

echo 'FINISHED'
echo 'PACK GATEWAY'
# 生成加密后的 jar 包 gateway/target/gateway_proguard_base.jar
rm -rf $WORKSPACE/gateway/target
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl gateway
if [[ ! -f $WORKSPACE/gateway/target/wefe-gateway.jar ]]; then
    echo '文件 wefe-gateway.jar 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo 'PACK FUSION-SERVICE'
rm -rf $WORKSPACE/fusion/fusion-service/target
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl fusion/fusion-service
if [[ ! -f $WORKSPACE/fusion/fusion-service/target/fusion-service.jar ]]; then
    echo '文件 fusion-service.jar 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo 'PACK MANAGER-SERVICE'
rm -rf $WORKSPACE/manager/manager-service/target
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl manager/manager-service
if [[ ! -f $WORKSPACE/manager/manager-service/target/manager-service.jar ]]; then
    echo '文件 manager-service.jar 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo 'PACK SERVING-SERVICE'
rm -rf $WORKSPACE/serving/serving-service/target
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl serving/serving-service
if [[ ! -f $WORKSPACE/serving/serving-service/target/serving-service.jar ]]; then
    echo '文件 serving-service.jar 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo 'PACK BLOCKCHAIN-DATA-SYNC'
rm -rf $WORKSPACE/union/blockchain-data-sync/target
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl union/blockchain-data-sync
if [[ ! -f $WORKSPACE/union/blockchain-data-sync/target/wefe-blockchain-data-sync.jar ]]; then
    echo '文件 wefe-blockchain-data-sync.jar 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo 'PACK UNION-SERVICE'
rm -rf $WORKSPACE/union/union-service/target
mvn clean install -Dmaven.test.skip=true -Dfile.encoding=utf-8 -am -pl union/union-service
if [[ ! -f $WORKSPACE/union/union-service/target/wefe-union-service.jar ]]; then
    echo '文件 wefe-union-service.jar 不存在，请检查此模块打包流程是否正常'
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
source ~/bash_profile
#nvm use 16.13.0
nvm use 16.13.0
#npm install > /dev/null
npm install
# board/board-website/dist
npm run build -- prod=board-website
sed -i '/<title>/i<script>window.clientApi = {env: "prod", baseUrl: "http://127.0.0.1/board-service", prefixPath: "/board-website/"};</script>' dist/board-website/index.html
if ! $(check_dir $WORKSPACE/board/board-website/dist); then
    echo '目录 $WORKSPACE/board/board-website/dist 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo 'PACK FUSION-WEBSITE'
cd $WORKSPACE/fusion/fusion-website
rm -rf $WORKSPACE/fusion/fusion-website/dist
#npm install > /dev/null
npm install
# fusion/fusion-website/dist
npm run build -- prod=fusion-website
sed -i '/<title>/i<script>window.clientApi = {env: "prod", baseUrl: "http://127.0.0.1/fusion-service", prefixPath: "/fusion-website/"};</script>' dist/fusion-website/index.html
if ! $(check_dir $WORKSPACE/fusion/fusion-website/dist); then
    echo '目录 $WORKSPACE/fusion/fusion-website/dist 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo 'PACK MANAGER-WEBSITE'
cd $WORKSPACE/manager/manager-website
rm -rf $WORKSPACE/manager/manager-website/dist
#npm install > /dev/null
npm install
# manager/manager-website/dist
npm run build -- prod=manager-website
sed -i '/<title>/i<script>window.clientApi = {env: "prod", baseUrl: "http://127.0.0.1/manager-service", prefixPath: "/manager-website/"};</script>' dist/manager-website/index.html
if ! $(check_dir $WORKSPACE/manager/manager-website/dist); then
    echo '目录 $WORKSPACE/manager/manager-website/dist 不存在，请检查此模块打包流程是否正常'
    exit 1
fi
echo 'FINISHED'

echo 'PACK SERVING-WEBSITE'
cd $WORKSPACE/serving/serving-website
rm -rf $WORKSPACE/serving/serving-website/dist
#npm install > /dev/null
npm install
# serving/serving-website/dist
npm run build -- prod=serving-website
sed -i '/<title>/i<script>window.clientApi = {env: "prod", baseUrl: "http://127.0.0.1/serving-service", prefixPath: "/serving-website/"};</script>' dist/serving-website/index.html
if ! $(check_dir $WORKSPACE/serving/serving-website/dist); then
    echo '目录 $WORKSPACE/serving/serving-website/dist 不存在，请检查此模块打包流程是否正常'
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
cp $WORKSPACE/fusion/target/fusion-service.jar $WORKSPACE/deploy_package_resource/fusion-service.jar
cp $WORKSPACE/manager/target/manager-service.jar $WORKSPACE/deploy_package_resource/manager-service.jar
cp $WORKSPACE/serving/target/serving-service.jar $WORKSPACE/deploy_package_resource/serving-service.jar
cp $WORKSPACE/union/blockchain-data-sync/target/wefe-blockchain-data-sync.jar $WORKSPACE/deploy_package_resource/wefe-blockchain-data-sync.jar
cp $WORKSPACE/union/union-service/target/wefe-union-service.jar $WORKSPACE/deploy_package_resource/wefe-union-service.jar

cp -r $WORKSPACE/board/board-website/dist $WORKSPACE/deploy_package_resource/board-website
cp -r $WORKSPACE/fusion/fusion-website/dist $WORKSPACE/deploy_package_resource/fusion-website
cp -r $WORKSPACE/fusion/manager-website/dist $WORKSPACE/deploy_package_resource/manager-website
cp -r $WORKSPACE/serving/serving-website/dist $WORKSPACE/deploy_package_resource/serving-website

cd $WORKSPACE/deploy_package_resource
tar -cf board-website.tar board-website
tar -cf fusion-website.tar fusion-website
tar -cf manager-website.tar manager-website
tar -cf serving-website.tar serving-website

# 数据库文件
cp $WORKSPACE/documents/数据库设计/board/wefe_board.sql $WORKSPACE/deploy_package_resource

echo '###### INTEGRATION FINISHED ######'
