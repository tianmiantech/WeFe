# 安装指南

由于 WeFe 系统服务服务较多，建议使用官方提供的 docker 镜像进行部署。

docker 部署方式暂时仅支持单机，后续会发布分布式部署的 docker 镜像。

    ⚠️ 注意
    安装前建议先查看系统架构相关篇章，了解 WeFe 系统的各构成模块。

## 环境要求

**操作系统：**<br>
推荐 CentOS 7.5+，其他 Linux（Ubuntu、MacOS） 内核的操作系统未做全面测试，但按经验应该都可以。

> 由于部分依赖包在 windows 上获取困难，所以尚不支持 windows 部署。 

<br>

**网络：**<br>
由于联邦学习建模需要与其他成员进行交互，除非两个成员处于同一局域网，否则您需要准备一个外网 IP 供 gateway 服务通过公网与其他成员的 gateway 进行交互。

另外，由于 WeFe 需要访问处于公网的 union 服务，您的服务器需要允许访问外网。

<br>

**服务器：**<br>
WeFe 系统中除了算法所在的 kernel 模块外，其他部分都不需要太多服务器资源，所以以下配置要求仅针对 kernel 所在的服务器。

最低配置：4C8G，仅能运行非常少量样本的数据集，在使用较大数据集进行建模时， kernel 模块可能会无法响应。

推荐配置：8C32G，能支撑数十万样本量，数百特征的数据集进行建模。

如果您希望提升建模效率，除了使用更高的配置，可以查看 [**计算引擎**](/calculation_engine/calculation_engine) 相关章节获取更多信息。


## 安装 docker

如果您的服务器中已有 docker 服务，可以跳过这一小节。

> 较旧的 docker 版本可能不支持 WeFe 系统的部署，请酌情选择是否需要升级您已有的 docker 服务。


<!-- tabs:start -->

#### **CentOS**

**使用官方脚本自动安装：**
```shell
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
```

<br>

**手动安装：**
```shell
# ---------- 安装 docker ----------
yum install -y yum-utilsdevice-mapper-persistent-data lvm2
yum-config-manager --add-repo http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
yum install -y https://mirrors.aliyun.com/docker-ce/linux/centos/7/x86_64/edge/Packages/containerd.io-1.2.13-3.1.el7.x86_64.rpm
yum install docker-ce docker-ce-cli containerd.io
# 检测是否安装成功
docker -v


# ---------- 安装 docker compose ----------
# 可能会出现连接超时的情况，请耐心的尝试几次
wget https://github.com/docker/compose/releases/download/1.27.4/docker-compose-Linux-x86_64
mv docker-compose-Linux-x86_64 /usr/local/bin/docker-compose
# 添加执行权限
chmod +x /usr/local/bin/docker-compose
# 检测是否安装成功
docker-compose -version
```

**配置 docker：**<br>
Docker 的持久化目录需要占用较大的磁盘空间，推荐将其持久化目录修改为较大的挂载盘目录( > 100G)。

修改 Docker 配置文件 /etc/docker/daemon.json，若文件不存在则手动创建此文件。
```json
{
  # 配置 Docker 持久化目录
  "graph": "/data/wefe/docker-compose",
  "live-restore": true
 }

```

#### **Ubuntu**

**使用官方脚本自动安装：**
```shell
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
```

<br>

#### **MacOs**

**手动下载安装：**<br>
https://docs.docker.com/desktop/mac/install/

<br>

**使用 Homebrew 安装：**
```shell
brew install --cask --appdir=/Applications docker
```

<!-- tabs:end -->


## 下载 WeFe 镜像

请前往 [Release Note](release/release) 查看镜像版本列表，并获取下载链接。

## 编辑 wefe.cfg

解压下载的镜像后，在根目录找到 `wefe.cfg` 并对其进行编辑。

**必须修改的配置项：**
```shell
# 本机内网 IP
INTRANET_IP=0.0.0.0
# 本机外网 IP
EXTRANET_IP=0.0.0.0
```

<br>

**可选的配置项：**
```shell
# 若服务器内存 ≥ 32G，可酌情优化为如下配置，否则保持默认配置即可
SPARK_DRIVER_MEMORY=15g
SPARK_DRIVER_MAXRESULTSIZE=2g
SAPRK_NUM_EXECUTORS=6
SPARK_EXECUTOR_MEMORY=2g
SPARK_EXECUTOR_CORES=1
SPARK_NUM_SLICES=32
```

## 启动 WeFe
```shell
# 启动
sh wefe-service.sh start

# 停止
sh wefe-service.sh stop
```

## 访问 WeFe

board-website 是 WeFe 系统的可视化操作界面，访问 `http://IP:PORT/board-website` 即可进入 登录/注册 界面。

第一个在 board 中注册的用户为 WeFe 的超级管理员，超级管理员登录后需要对 WeFe 系统进行初始化，初始化成功后您即登记成为联邦中的一员。

您可以在 board 的系统设置中修改您在联邦中的可见性，相关操作请查看 [**使用手册**](/operation_guide/operation_guide) 相关章节。