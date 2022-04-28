source ../deploy.cfg

# 检查资源是否存在
DIR_NAME=manager-website

if [ $1 == 'update' ];then
  rm -rf $DIR_NAME $DIR_NAME.tar
  wget $DOWNLOAD_BASE_PATH/PACKAGES/$DIR_NAME.tar
fi

tar -xvf $DIR_NAME.tar

cd DIR_NAME/DIR_NAME/index.html



