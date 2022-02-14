Wefe Spark 集群版相关配置与部署方案。

master 和 worker 都是 wefe 的基于 docker 启动的 WeFe PythonProject，各集群节点都需要有相关的运行环境以及项目代码。

# 免密登陆

部署 WeFe 集群版本需要配置主从机器间可进行 Root 免密登陆。

**公私密钥**

若 A 需要 SSH 免密登陆 B，则 A 需要生成公钥和私钥，而 B 需要有 A 的公钥。

```shell
# 机器 A
sudo su root
# 默认两次回车生成公钥和私钥
ssh-keygen -t rsa -P “”
cat ~/.ssh/id_rsa.pub # 复制 A 的公钥

# 机器 B
vi ~/.ssh/authorized_keys # 粘贴 A 的公钥
```

**sshd 配置**

SSH 服务默认是不允许 Root 角色的免密登陆的，需要修改其默认配置文件 /etc/ssh/sshd_config。

```shell
# 修改机器 B 的 SSH 服务配置
PermitRootLogin yes
```

免密登陆测试

```shell
# A 登陆 B
ssh root@[B IP]
```



# 网络设置

Docker 容器中的各节点需要跨主机通信，这里使用 Docker Overlay 网络。

要实现 Overlay 网络，需要一个发现服务，这里使用 Consul 进行服务发现。

## Consul 部署配置

Consul 需要部署在 master 机器上。

**master**

下载 Consul 镜像并启动，默认启动在 8500 端口。

```shell
docker pull consul:1.9.10
docker run -d -p 8500:8500 -h consul --name consul consul
```

访问地址 `[master ip]:8500`，若服务启动成功，则可正常访问 Consul WebUI 页面。

修改 master 机器上的 Docker 配置文件 /etc/docker/daemon.json，添加以下配置：

```json
{
  "live-restore": true,
  "cluster-store": "consul://[master ip]:8500",
  "cluster-advertise": "[master ip]:2375"
 }
```

修改完 Docker 的 daemon.json 配置文件后重启 docker 服务。

```shell
systemctl restart docker
```

**worker**

修改 worker 机器上的 Docker 配置文件 /etc/docker/daemon.json，添加以下配置：

```json
{
  "live-restore": true,
  "cluster-store": "consul://[master ip]:8500",
  "cluster-advertise": "[worker ip]:2375"
 }
```

修改完 Docker 的 daemon.json 配置文件后重启 docker 服务。

```shell
systemctl restart docker
```

## Overlay 网络模式

在 master 机器上创建 Docker Overlay 网络。

需要注意的是，此处指定的网络 subnet 需要与 wefe.cfg 中配置的 CONTAINER_SUBNET 一致（默认是一致的）。

```shell
docker network create -d overlay --subnet 10.101.0.0/24  spark_overlay
```

查看网络是否创建成功。

```shell
docker network ls

# wefe_overlay    overlay   global
```

在 worker 服务器查看在 master 创建的 Global Overlay 类型的网络 wefe_overlay 是否被成功同步。

```shell
docker network ls

# wefe_overlay    overlay   global
```

若在 worker 的服务器上能看到创建的 Global Overlay 类型的网络 wefe_overlay，则网络同步成功，网络配置成功。

# 配置与部署

部署 Spark 集群版，需要按需修改 Spark 相关配置，默认配置如下。

```
# 配置文件位于项目根目录下的 wefe.cfg 文件中

# SPARK 启动模式 LOCAL / STANDALONE，其中 STANDALONE 为 spark 的集群模式
SPARK_MODE=LOCAL
# 若采用集群模式，需要配置 MASTER 及 WORKER 参数，且保证各机器间已配置 SSH 免密登录以及 Overlay 网络
# 目前只支持单个 master，若干个 worker 的模式
SPARK_MASTER=192.168.1.2
# 填写机器的内网 IP，逗号分隔
SPARK_WORKERS=192.168.1.2,192.168.1.3
SPARK_SUBMIT_PORT=7077
SPARK_MASTER_UI_PORT=8082
# 相应的 worker 的 worker_ui_port 也需要多个配置
SPARK_WORKER_UI_PORTS=8081,8081
SPARK_CLUSTER_DATA_PATH=/data/wefe/wefe_spark_cluster/
# 集群 SPARK 容器的网段 IP（虚拟网段，不建议修改）
CONTAINER_SUBNET=10.101.0.0
```





