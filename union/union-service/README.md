union-service 是联邦学习的 union 的后台服务；

union-service 使用去中心化的数据存储方式，存储联邦成员的公开信息，比如成员信息、公开的数据集信息；

union-service 提供 http 接口供各成员修改自身公开信息以及获取联邦中其他成员的公开信息。

# 项目特点

使用 Springboot + Mongodb + Fisco bcos(金链盟) 架构方式；

去中心化、防篡改、可追溯性。

# 环境要求

在使用本组件前，请确认系统环境已安装相关依赖软件，清单如下：

| 依赖软件 | 说明 |备注|
| --- | --- | --- |
| Java | JDK[1.8] ||
| Mongodb | >= Mongodb[4.0] | |
| Fisco bcos | [Fisco bcos 搭建请参考](https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/manual/build_chain.html)  | |


# 编译打包
项目基于 maven 进行打包（部署机器上需要有 maven 环境）。

```bash
# 在 [Root Dir] 目录下执行打包命令
mvn clean install -Dmaven.test.skip=true -am -pl union/union-service
```

# 修改配置文件

新建config.toml 文件
```bash
[cryptoMaterial]
# 证书目录 需要根证书ca.crt,sdk证书sdk.crt,sdk秘钥sdk.key
certPath = "xx/xx/cert"                           # The certification path

[network]

peers=["0.0.0.0:20200"]    # The peer list to connect
```


union-service 的启动依赖 resource/application.properties 配置文件

修改 mongodb 相关配置项

```bash
spring.datasource.mongodb.uri=mongodb://user:pwd@0.0.0.0:37017/wefe_union
spring.datasource.mongodb.databaseName=wefe_union
```


修改 区块链 相关配置项

```bash
block.chain.toml.file.path=xx/xx/config.toml
```




# 启动项目

```bash
java -jar wefe-union-service.jar
```
