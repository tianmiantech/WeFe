#ntos，版本为7，build镜像时会自动下载
FROM gpu_python37_base:latest

#语言编码设置
RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8
ENV LC_ALL zh_CN.UTF-8

# 修改时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

# 项目位置
ENV CODE_DIR=/opt/welab/wefe

# 在镜像创建目录
RUN mkdir -p $CODE_DIR

# 将项目模块放入 docker 镜像
COPY ./python_project $CODE_DIR/

