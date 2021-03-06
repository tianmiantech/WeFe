# GPU 部署

## 前言
> WeFe 目前还支持 GPU 的部署方案，主要目的就是提高联邦建模算法的运算效率，现阶段实现了模幂、模乘的加速，且目前处于试验阶段，仅供参考!

- 皮尔逊算法组件测试：4~6 倍的提升，数据量越大，提升效果越明显。
- 纵向逻辑回归算法：1倍左右的提升。

## 部署说明
<font color="red">部署GPU前，请确认宿主机为 GPU 服务器</font>

### 一、环境安装
由于本项目采用 `Docker` 方式部署，所以，以下指引都是基于 Docker 部署方案进行罗列：

#### 1. CUDA 环境安装
> 虽然 WeFe 是采用 Docker 安装部署，但部署 GPU 时，仍需在宿主机安装 CUDA 环境，具体移步 [WeFe手册]()

#### 2. 配置修改
> 安装完 CUDA 环境后，仅需修改 wefe.cfg 配置 `ACCELERATION="GPU"` 即可启动 `wefe_service.sh` 脚本
