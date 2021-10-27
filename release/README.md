release 是 WeFe 系统的发行服务，提供了一整套基于 Docker 的打包配置，是一套结构化的打包流程，并支持基于 Jenkins 实现持续的软件版本发布。

# 简介

打包的总入口为项目的根目录下的 `jenkins_docker_deploy.sh` 脚本。

目录结构：

```shell
.
├── docker
│   ├── deploy_shell
│   ├── docker_image
│   └── docker_service
├── docker_manager
└── release-control-shell
```

**docker**

与 Docker 打包相关的脚本配置文件。

**docker_manager （Coming Soon）**

WeFe 发行管理的接口服务。

**release-control-shell （Coming Soon）**

WeFe 发行升级的相关脚本配置。

# docker

提供 WeFe 的打包、Docker 镜像打包以及 WeFe 一键部署启动等服务。

基于 Docker 镜像部署包启动 WeFe 项目请参考文档 [docker/README.md](./docker/README.md)

**deploy_shell**

打包的脚本目录。

打包的大致流程为：项目各模块的打包 → 资源目录整理 → Docker 镜像打包 → 部署目录整理。

**docker_image**

Docker 镜像打包的目录，包含各模块的  `docker-compose` 文件以及打包所需要的资源文件；其中 `env` 目录放置的是基础环境的镜像打包配置和资源，`app` 目录放置的是 WeFe 模块的打包配置与资源。

其中，WeFe 模块的镜像打包依赖于基础环境镜像。

**docker_service**

WeFe 基于 Docker 镜像进行部署的配置管理目录，包含 WeFe 的个性化配置以及各模块的运行管理等功能。其中，`wefe.cfg` 为项目的总配置文件，`wefe_service.sh` 为项目模块的管理脚本。

