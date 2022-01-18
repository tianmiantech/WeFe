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


echo "******************************************************"
echo "******************** WeFe 升级程序 ********************"
echo "******************************************************"
# 配置文件所在目录
config_path=$1
echo "config.properties:$config_path"

# 获取 json形式的入参
action_info=$(echo $2 | tr -d "\n")
echo "action       info:$action_info"

# 脚本所在的目录
workspace=$(pwd)
echo "current       dir:$workspace"
echo "******************************************************"


echo "正在初始化 python 运行环境"
pip install -r requirements.txt
echo "python 运行环境初始化成功！"
echo "******************************************************"


echo 正在使用 python 脚本执行升级操作
python -c "import main; main.run('$workspace', '$action_info')"

python_result=$?
echo "升级结果：$python_result"

if [ $python_result != 0 ];
then
  echo "已退出升级程序"
  exit 1
fi

echo "升级成功！"
echo "******************************************************"
echo ""
echo "*********************** 开始升级 **********************"
