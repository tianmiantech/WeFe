#!/usr/bin/env bash

## -x: Debug mode
## -e: exit the script if any statement returns a non-true return value
[ x"${DEBUG}" == x"true" ] && set -ex || set -e

## application 值为 Jenkins 编译时传入，对应 Jenkins 的 JOB_BASE_NAME，即 APP_NAME
application=$1

## --- 该分割线以上代码不用动 ---

## 切换到具体的子项目顶层目录
workdir=$(pwd)

cd "$workdir"

mvn clean install -Dmaven.test.skip=true -am -pl board/board-service
echo "打包完毕"

## 生成 JSON 配置文件，此文件作用告知运维怎么拿到实际要部署的代码、配置文件（以目录形式存放）
## JSON 中的 key 值，事先和运维约定好
cat > /tmp/"$application" <<-EOF
{
    "targetPath": "${workdir}/board/board-service/target"
}
EOF

exit 0
