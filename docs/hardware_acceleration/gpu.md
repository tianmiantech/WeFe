# GPU 部署

## 部署说明
<font color="red">部署GPU前，请确认宿主机为 GPU 服务器</font>

### 一、环境安装
由于本项目采用 `Docker` 方式部署，所以，以下指引都是基于 Docker 部署方案进行罗列：

#### 1. CUDA 环境安装
> 虽然 WeFe 是采用 Docker 安装部署，但部署 GPU 时，仍需在宿主机安装 CUDA 环境，以下介绍具体步骤：

##### 1.1. 前往 `NVIDIA` [官网](https://developer.nvidia.com/cuda-downloads?target_os=Linux&target_arch=x86_64) 选择相应的环境
- 例如，选择基于 Linux 操作系统，x86_64, CentOS 7 的 `CUDA` 环境

<img src="_media/hardware_acceleration/select_system.png" style="max-height:700px;" />

- 随后按照相应的安装命令进行安装

<img src="_media/hardware_acceleration/install_shell.png" style="max-height:700px;" />

```shell
wget https://developer.download.nvidia.com/compute/cuda/11.4.2/local_installers/cuda-repo-rhel7-11-4-local-11.4.2_470.57.02-1.x86_64.rpm
sudo rpm -i cuda-repo-rhel7-11-4-local-11.4.2_470.57.02-1.x86_64.rpm
sudo yum clean all
sudo yum -y install nvidia-driver-latest-dkms cuda
sudo yum -y install cuda-drivers
```

> 至此安装完毕

#### 2. 配置修改

##### 2.1. Docker 配置修改
- 前提先安装 `Docker`，如未安装，请参考 [部署文档中的 Docker 安装部分](../install/install.md)

要启动 GPU Docker 容器，先要修改 Docker 配置文件 /etc/docker/daemon.json，替换成如下内容：
```json
{
  "graph": "/data/wefe/docker-compose",
  "live-restore": true,
  "runtimes": {
        "nvidia": {
            "path": "nvidia-container-runtime",
            "runtimeArgs": [],
            "live-restore": true
        }
  }
}
```

##### 2.2. wefe.cfg 配置修改
安装完 CUDA 环境后，仅需修改 wefe.cfg 配置 `ACCELERATION="GPU"` 即可启动 `wefe_service.sh` 脚本
