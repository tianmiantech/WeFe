source ../deploy.cfg

# 检查资源是否存在
DIR_NAME=serving-website
CONTEXT=serving-website
BASE_RUL="\"$BOARD_SERVICE_URL\""
PREFIX_PATH="\"/$CONTEXT/"\"
echo $PREFIX_PATH

if [ $1 == 'update' ];then
  rm -rf $DIR_NAME $DIR_NAME.tar
  wget $DOWNLOAD_BASE_PATH/PACKAGES/$DIR_NAME.tar
fi

tar -xvf $DIR_NAME.tar

sed -i 's@</title>@&<script>window.clientApi = {env: "prod", baseUrl: "", prefixPath: ""};</script>@' $DIR_NAME/$CONTEXT/index.html
sed -i "s@baseUrl.*,@baseUrl: $BASE_RUL,@" $DIR_NAME/$CONTEXT/index.html
sed -i "s@prefixPath.*}@prefixPath: $PREFIX_PATH }@" $DIR_NAME/$CONTEXT/index.html

echo "Success \nPlease config the nginx service."