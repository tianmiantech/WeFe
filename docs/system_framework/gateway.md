# gateway

gateway 是各成员间进行通信的唯一出入口服务。

# 项目特性

基于 SpringBoot + gRPC 架构，支持多种语言的客户端，比如 Java、Python 等；

统一了联邦成员数据的出入口，降低了各模块以及各成员交互的复杂性；

接口使用签名认证、IP白名单、数据防篡改、数据防重放等安全保障。

# 架构图

![image-20210923001512879](../images/GatewaySystemStructure.jpeg)

## 项目的打包与启动
**环境依赖**

本项目的打包与启动环境依赖于

- JDK 1.8
- MySQL 5.7
- Maven

**编译 / 打包**

```bash
mvn clean install -Dmaven.test.skip=true -am -pl gateway
```

**配置文件说明**

gateway 的启动依赖 jar 包同级目录下的配置文件  `application.properties` 。

```bash
# 日志相关配置项
logging.level.root=INFO
logging.file=/data/logs/wefe-gateway/wefe-gateway.log
logging.file.max-history=60
logging.file.max-size=20GB
logging.pattern.console=%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) [%X{requestId}] %clr(${PID:- }){magenta} %clr([%15.15t]){faint} %clr(%-40.40logger{39}[%F:%L]){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] [%X{requestId}] ${PID:- } [%15.15t] %-40.40logger{39}[%F:%L] : %m%n
```

```bash
# gRPC服务端口口
rpc.server.port=50051
# 待转发的元数据消息保存目录
send.transfer.meta.persistent.temp.dir=/data/gateway/send
# 接收到远端网关提过过来的消息保存目录
recv.transfer.meta.persistent.temp.dir=/data/gateway/recv

# 通用配置文件路径（详情请参考相关说明）
config.path=config.properties
```