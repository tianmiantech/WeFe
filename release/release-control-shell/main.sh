echo "******************** WeFe 升级程序 ********************"
# 配置文件所在目录
config_path=$1
echo config.properties:$config_path

# 获取 json形式的入参
main_input=$2
echo "main input:$main_input"

# 脚本所在的目录
echo "current dir:$(pwd)"
echo "******************************************************"


echo "正在初始化 python 运行环境"
pip install -r requirements.txt
echo "python 运行环境初始化成功！"
echo "******************************************************"


echo 正在检查升级程序运行环境
python env_checker.py "$config_path"
check_result=$?
echo "检查结果：$check_result"

if [ $check_result != 0 ];
then
  echo "已退出升级程序"
  exit 1
fi

echo "升级程序运行环境检查成功！"
echo "******************************************************"
echo ""
echo "*********************** 开始升级 **********************"
python -c "import launcher; Launcher.run('$main_input')"