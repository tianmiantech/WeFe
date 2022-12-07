#!/usr/bin/env bash

## -x: Debug mode
## -e: exit the script if any statement returns a non-true return value
[ x"${DEBUG}" == x"true" ] && set -ex || set -e

## application 值为 Jenkins 编译时传入，对应 Jenkins 的 JOB_BASE_NAME，即 APP_NAME
application=$1

## --- 该分割线以上代码不用动 ---

## 切换到具体的子项目顶层目录
workdir=$(pwd) ; projectdir=$workdir ; tmpdir=/tmp/python-project.$$ ; mkdir -p $tmpdir

# python 项目整合
#projectdir=$(dirname $(dirname $(dirname "$PWD")))
cd $projectdir ; #rm -rf python-project ; mkdir python-project

# go 编译
export GOPROXY="https://goproxy.cn,direct"

echo "GO Build [ GOPROXY: ${GOPROXY%,*} ]"  ; cd common/python/calculation/fc/go/src
echo '准备编译go动态链接库'                 ; sh build.sh
echo 'GO Build FINISHED'

# 切换目录
cd $projectdir ; echo '拷贝代码文件...' ; set -x

#find ./common/ -name 'python' | xargs tar -czf ./common-python.tgz
#tar -zxf common-python.tgz -C $tmpdir/
find ./common/ -name 'python' | xargs tar -cf - | tar -xf - -C $tmpdir/

cp -r $projectdir/flow $tmpdir/
cp -r $projectdir/kernel $tmpdir/

rm -f $tmpdir/config.properties
rm -f $tmpdir/requirements.txt

cp $projectdir/release/docker/deploy_shell/tool/requirements.txt $tmpdir/
rm -f $tmpdir/flow/service.sh ; cp $workdir/flow/assembly/scripts/service.sh $tmpdir/flow/

# 函数计算环境
cp -r $projectdir/common/python/calculation/fc $tmpdir/common/python/calculation/

## rsync codeDir
targetPath=$projectdir/flow/target ; rsync -az --delete $tmpdir/ $targetPath/

## 生成 JSON 配置文件，此文件作用告知运维怎么拿到实际要部署的代码、配置文件（以目录形式存放）
## JSON 中的 key 值，事先和运维约定好
cat > /tmp/${SERVICE_NAME:=$application} <<-EOF
{
    "targetPath": "$targetPath"
}
EOF

exit 0
