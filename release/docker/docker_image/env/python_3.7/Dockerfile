#基础镜像为centos，版本为7，build镜像时会自动下载
FROM centos:centos7.5.1804

#语言编码设置
RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8
ENV LC_ALL zh_CN.UTF-8

# 修改时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone \
    && yum -y install gcc gcc-c++ make openssl-devel gmp-devel mpfr-devel libmpc-devel libaio numactl \
    autoconf automake libtool libffi-devel snappy snappy-devel zlib zlib-devel bzip2 bzip2-devel lz4-devel \
    libasan lsof sysstat telnet psmisc python3-devel wget less vim

# 本地环境路径
ARG LOCAL_ENV_BASE=.

# 镜像环境路径
ARG ENV_BASE=/data/environment
ARG MINICONDA_DIR=$ENV_BASE/miniconda3
ARG PYTHON_DIR=$ENV_BASE/miniconda3/envs/wefe-python37
ARG SPARK_DIR=$ENV_BASE/spark-3.0.1-bin-hadoop2.7
ARG JDK_DIR=$ENV_BASE/jdk1.8.0_241
ARG OSSUTIL_DIR=$ENV_BASE/ossutil
ARG FUNCRAFT_DIR=/usr/local/bin

# 在镜像创建目录
RUN mkdir -p $MINICONDA_DIR \
    && mkdir -p $SPARK_DIR \
    && mkdir -p $JDK_DIR \
    && mkdir -p $OSSUTIL_DIR

# 复制 miniconda 的 python 环境
COPY $LOCAL_ENV_BASE/miniconda3 $MINICONDA_DIR/

# 复制 spark 环境
COPY $LOCAL_ENV_BASE/spark-3.0.1-bin-hadoop2.7 $SPARK_DIR/

# 复制 jdk 环境
COPY $LOCAL_ENV_BASE/jdk1.8.0_241 $JDK_DIR/

# 复制 ossutil
COPY $LOCAL_ENV_BASE/ossutil $OSSUTIL_DIR

# 复制 funcraft
COPY $LOCAL_ENV_BASE/funcraft/fun $FUNCRAFT_DIR

# 配置环境变量
ENV JAVA_HOME $JDK_DIR
ENV PATH $JAVA_HOME/bin:$PATH

ENV SPARK_HOME $SPARK_DIR
ENV PATH $SPARK_HOME/bin:$PATH

ENV PYTHON_HOME $PYTHON_DIR
ENV PATH $PYTHON_HOME/bin:$PATH