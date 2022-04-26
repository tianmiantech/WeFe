#!/bin/bash

source ./env
source ./func

rebuild_cd_dir $WORK_DIR/package
init_dir $INIT_DIR_STR

cd $WORK_DIR
java_pack $JAVA_PACK_STR

cd $WORK_DIR
JAR_PATH_STR=`get_jar_path $JAVA_PACK_STR`
echo $JAR_PATH_STR

cd $WORK_DIR
copy_jar $JAR_PATH_STR

cd $WORK_DIR
source ~/.bash_profile
nvm use 16.13.0
pack_webs $WEB_PACK_STR

cd $WORK_DIR
pack_python
