依赖的基础环境

- wefe_python_base:[TAG]

- wefe_java_base:[TAG]

运行 Jenkins 打包容器时，需要检查依赖的基础环境是否存在

若基础环境不存在，则在 docker_image/app 目录下生成

```
# 根据当前目录 Dockerfile 生成新的镜像
docker build -t [NAME]:[TAG] .
```

DataBase 使用官方提供的镜像，无须自定义 DockerFile

- clickhouse 镜像版本: yandex/clickhouse-server:20.6.5.8

- mysql 镜像版本: mysql:5.7.24

