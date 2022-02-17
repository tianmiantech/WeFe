#!/usr/bin/env bash

## -x: Debug mode
## -e: exit the script if any statement returns a non-true return value
[ x"${DEBUG}" == x"true" ] && set -ex || set -e

## application 值为 Jenkins 编译时传入，对应 Jenkins 的 JOB_BASE_NAME，即 APP_NAME
application=$1

## --- 该分割线以上代码不用动 ---

## 检索当前要编译的项目，再去调具体子项目的编译脚本
case "$application" in
    wefe-board-service)
        /bin/bash ./board/board-service/assembly/scripts/install.sh $application
        ;;
    wefe-board-website)
        /bin/bash -x ./board/board-website/assembly/scripts/install.sh $application
        ;;
    wefe-union-service)
        /bin/bash ./union/union-service/assembly/scripts/install.sh $application
        ;;
    wefe-union-website)
        /bin/bash ./union/union-website/assembly/scripts/install.sh $application
        ;;
    wefe-gateway)
        /bin/bash ./gateway/assembly/scripts/install.sh $application
         ;;
    wefe-serving-service)
        /bin/bash ./serving/serving-service/assembly/scripts/install.sh $application
        ;;
    wefe-serving-website)
        /bin/bash ./serving/serving-website/assembly/scripts/install.sh $application
        ;;
    wefe-blockchain-data-sync)
        /bin/bash ./blockchain/wefe-blockchain-data-sync/assembly/scripts/install.sh $application
        ;;
    wefe-data-fusion-service)
        /bin/bash ./fusion/fusion-service/assembly/scripts/install.sh $application
        ;;
    wefe-data-fusion-website)
        /bin/bash ./fusion/fusion-website/assembly/scripts/install.sh $application
        ;;
    wefe-manager-service)
        /bin/bash ./manager/manager-service/assembly/scripts/install.sh $application
        ;;
    wefe-manager-website)
        /bin/bash ./manager/manager-website/assembly/scripts/install.sh $application
        ;;
esac

exit 0
