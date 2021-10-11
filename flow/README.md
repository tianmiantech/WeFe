flow 在系统中提供任务调度、任务状态监控以及 API 接口服务等功能；

flow 的启动依赖于 kernel 以及 common.python；

flow 的任务消费依赖于 board 写入任务，共用业务数据库。

# 项目启动

基于 Python37、Java (JDK 1.8)、Spark (spark-3.0.1-bin-hadoop2.7) 环境。

参考 board-service 模块修改数据库配置。

**交互启动**

```
# 激活虚拟环境
source [Virtual Env Dir]/bin/activate

# 声明环境变量
export PYTHONPATH=[Root Dir]
export SPARK_HOME=[Spark Dir]
export JAVA_HOME=[JDK Dir]
export PATH=$SPARK_HOME/bin:$JAVA_HOME/bin:$PATH

# 下载环境依赖
pip install -r ${PYTHON_ROOT}/requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

# 启动项目
cd [Root Dir]/flow
nohup python3 app_launcher.py >> "[Log Dir]/console.log" 2>>"[Log Dir]/error.log" &
```

**脚本启动**

```
# 声明 JAVA_HOME
export JAVA_HOME=[JDK Dir]
export PATH=$JAVA_HOME/bin:$PATH
```

修改 [Root Dir]/flow 目录下的 service.sh 启动脚本。

```
export PYTHONPATH=[Root Dir]
export SPARK_HOME=[Spark Dir]
PYTHON_ROOT=[Root Dir]
log_dir=[Log Dir]
venv=[Virtual Env Dir]
```

# 项目结构

```
.
├── alert_service
├── cycle_actions
│   ├── flow_action_queue
│   └── guard
├── service
├── utils
└── web
```

**alert_service**

提供各类提醒服务的调用。

**cycle_actions**

定义长轮询服务，所有长轮询服务都应该写在此模块下。

目前已经实现了的长轮询服务：
- 监听消费队列，消费任务；
- 监听任务运行状态，任务状态管理。

**service**

业务服务的具体实现。

**utils**

常用的工具方法。

**web**

基于 Flask 的自定义 Web 架构，实现了目录与访问地址上下文映射关系，提供基础的 Web 服务。





