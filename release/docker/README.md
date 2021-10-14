

# 部署操作

WeFe Member 模块提供了基于 Docker 容器的快速启动方式，需要依赖于 Docker 环境。

Docker 运行操作需要 root 用户权限。

部署包下载:
> `OSS 下载地址`: `https://welab-wefe-release.oss-cn-shenzhen.aliyuncs.com/welab_wefe_v.2.2.tar`


## 主机环境

推荐配置：CPU ＞ 8、内存 > 32G

系统要求：CentOS 7.5

防火墙关闭或开启对应的容器开放端口（具体端口在 `wefe.cfg` 文件中进行查看与修改）。

```
systemctl stop firewalld.service
# 重启 docker
systemctl restart docker
```

关闭 `setLinux`。

```
vim /etc/selinux/config
# 将其中 SELINUX 设置为 disable
```

## Docker 环境安装

```
### 本机 Docker 卸载
yum remove docker \
     docker-client \
     docker-client-latest \
     docker-common \
     docker-latest \
     docker-latest-logrotate \
     docker-logrotate \
     docker-engine
rpm -qa |grep docker
yum list installed | grep docker

yum remove -y containerd.io.x86_64 \
docker-ce.x86_64 docker-ce-cli.x86_64

### Docker 安装
# 复制后注意检查粘贴后的代码是否正确
yum install -y \
yum-utilsdevice-mapper-persistent-data lvm2

yum-config-manager \
--add-repo http://mirrors.aliyun.com\
/docker-ce/linux/centos/docker-ce.repo

yum install -y https://mirrors.aliyun.com/\
docker-ce/linux/centos/7/x86_64/edge/\
Packages/containerd.io-1.2.13-3.1.el7.x86_64.rpm

yum install docker-ce docker-ce-cli containerd.io
```

## Docker-Compose 环境安装

```
# 下载对应版本的官方包，可能会出现连接超时的情况，请耐心的尝试几次
wget https://github.com/docker/compose/releases/download/1.27.4/docker-compose-Linux-x86_64
mv docker-compose-Linux-x86_64 /usr/local/bin/docker-compose
# 添加执行权限
chmod +x /usr/local/bin/docker-compose
# 检测是否安装正确
docker-compose -version
```

## Docker 配置

Docker 的持久化目录需要占用较大的磁盘空间，推荐将其持久化目录修改为较大的挂载盘目录( > 100G)。

修改 Docker 配置文件 /etc/docker/daemon.json，若文件不存在则手动创建此文件。

```
{
  # 配置 Docker 持久化目录
  "graph": "/data/wefe/docker-compose",
  "live-restore": true
 }
 ```

## 项目配置

解压下载的系统部署包。

编辑 wefe.cfg 文件进行配置的修改。其中必须修改对应服务部署的服务器 IP，若都部署在同一台服务器则服务的 IP 都为同一个 IP 地址。

常规配置：无默认值，需要根据服务器的实际情况进行修改；

```
# 本机内网 IP
INTRANET_IP=127.0.0.1
# 本机外网 IP
EXTRANET_IP=127.0.0.1
```

可选配置：有默认值，不影响系统使用，可不修改；

优化配置：有默认值。可根据服务器的情况对系统进行调优。

```
# 若服务器内存 ≥ 32，可优化为如下配置，否则保持默认配置即可
SPARK_DRIVER_MEMORY=15g
SPARK_DRIVER_MAXRESULTSIZE=2g
SPARK_NUM_EXECUTORS=6
SPARK_EXECUTOR_MEMORY=2g
SPARK_EXECUTOR_CORES=1
SPARK_NUM_SLICES=32
```

### 函数计算

系统支持阿里云的函数计算；函数计算是事件驱动的全托管计算服务，具体部署方式参考[函数计算部署文档](./README_FC.md)

## 项目启动

运行脚本启动项目。

```
sh wefe-service.sh start
```

> 注意，配置中的端口不可与本机以占用的端口发生冲突，若发生端口冲突导致启动失败，则需要停止移除所有容器，重新启动服务。

运行脚本停止项目。

```
sh wefe-service.sh stop
```

## 访问项目

访问 `http://[主机 IP]/board-website` 进入项目登陆界面。

第一个注册的用户为此联邦成员的超级管理员。

在全局设置 –> 系统设置 –> 填写各服务访问地址

```
# Board-Service 内网地址
http://[ip:port]/board-service

# Gateway 内网地址
[ip:port]

# Flow 内网地址
http://[ip:port]
```

检查首页的消息面板，各服务的服务状态是否正常，消息面板是否有异常信息。

右上角的帮助文档可帮助您了解基本的项目操作。

## 网络要求
 
WeFe 联邦学习平台的成员需要与 Union 交互，获取联邦的相关信息；

WeFe 联邦学习平台的成员需要与建模合作方交互，获取建模的相关信息；

故部署 Gateway 所在的服务器需要有外网的访问权限；若无外网访问权限，则至少需要可以访问以下 IP:Port

- 47.155.159.203:8080   # WeFe Union 的交互地址；
- 建模合作方的 Gateway IP:Port。

配置的 Gateway Uri 需要可以被外网访问，若部署的服务器不允许直接对外网开放，可使用 Nginx 进行代理转发，详细配置见下文。

**使用 Nginx 转发，则 Gateway Uri 需要修改为 Nginx 监听的地址。**

## Nginx grpc 配置

若服务器中的服务不可直接暴露在外网中，可以通过 Nginx 进行转发。

```
### 参考配置

user  root;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  65;
    underscores_in_headers on; # 必须
    server {
        listen 80 http2;
        server_name localhost

        grpc_connect_timeout 600s;
        grpc_read_timeout 600s;
        grpc_send_timeout 600s;

        location / {
            grpc_pass grpc://localhost:50051;
        }
    }
}
```