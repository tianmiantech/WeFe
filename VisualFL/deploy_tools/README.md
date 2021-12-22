
#### 1. 本地执行：python visualfl_deploy/_build.py
#### 2. 打包deploy_tool：python setup.py sdist，上传到服务器
#### 3. 创建一个python虚拟环境（推荐），并激活：source <virtualenv>/bin/activate
#### 4. 安装deploy_tool：pip install visualfl_deploy-1.0.tar.gz
#### 5. 生成模版：wefe_visualfl_deploy template standalone
#### 6. 部署服务：wefe_visualfl_deploy deploy deploy --config standalone_template.yaml
#### 7. 启动服务：wefe_visualfl_deploy services all start standalone_template.yaml

#### 运行示例
cd /path/to/visualfl
export PYTHONPATH=$PYTHONPATH:/path/to/visualfl/VisualFL
sh VisualFL/examples/paddle_clas/run.sh 127.0.0.1:10002





