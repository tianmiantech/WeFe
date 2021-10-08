# 指定基础镜像
FROM centos:centos7.5.1804
#语言编码设置
RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8
ENV LC_ALL zh_CN.UTF-8
# 修改时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone
# 指定构建镜像时的工作目录
WORKDIR /opt/docker_jdk
# 复制文件到镜像中
ADD jdk-8u261-linux-x64.tar.gz /opt/docker_jdk/
# 配置环境变量
ENV JAVA_HOME=/opt/docker_jdk/jdk1.8.0_261
ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH=$JAVA_HOME/bin:$PATH
