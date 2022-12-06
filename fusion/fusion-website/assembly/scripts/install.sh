echo ">> 1.切换node, npm 版本"
## 切换 node 版本
[ -e $HOME/.nvm/nvm.sh ] && source $HOME/.nvm/nvm.sh
version=v16.14.0 ; nvm use $version || { nvm install $version ; nvm use $version ; }
echo ">> 2.开始部署: 环境【"$REGION"】, 分支【"$CI_COMMIT_REF_NAME"】"
echo ">> 清理缓存"

npm cache clean -f

echo ">> 3.清理完毕"

echo ">> 安装依赖"
npm install --registry=http://nx-dev.welab-inc.com:8081/repository/welabnpmGroup/ --legacy-peer-deps --verbose

echo ">> 安装依赖完成"

echo ">> 编译"
npm run build -- --HOST_ENV $CI_DEPLOY_ENV
echo ">> 编译完成"
