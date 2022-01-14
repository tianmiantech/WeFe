# Copyright 2021 The WeFe Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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


echo 正在使用 python 脚本执行升级操作
python -c "import main; Main.run('$config_path','$main_input')"

check_result=$?
echo "升级结果：$check_result"

if [ $check_result != 0 ];
then
  echo "已退出升级程序"
  exit 1
fi

echo "升级成功！"
echo "******************************************************"
echo ""
echo "*********************** 开始升级 **********************"
