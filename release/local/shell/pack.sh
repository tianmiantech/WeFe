#!/bin/bash

source ./env
source ./func

rebuild_cd_dir $PACK_DIR

pack_java $JAVA_PACK_STR

JAR_PATH_STR=`get_jar_path $JAVA_PACK_STR`
echo $JAR_PATH_STR

copy_jar $JAR_PATH_STR

source ~/.bash_profile
nvm use 16.13.0
pack_webs $WEB_PACK_STR

pack_python

upload_packages
