blockchain-data-sync 对 union 的区块链相关业务进行了优化处理，实现了区块链数据到业务库的实时同步，以方便做一些复杂查询跟统计的业务操作。

# 环境依赖

blockchain-data-sync 使用 java 开发，基于 spring-boot 框架，使用 maven 进行包管理；

blockchain-data-sync 依赖 mongodb 数据库，所以在启动前需要先准备好 mongodb 服务；

blockchain-data-sync 区块链节点，所以必须要部署区块链节点。

# 编译打包

项目基于 maven 进行打包（部署机器上需要有 maven 环境）。

```bash
# 在 [Root Dir] 目录下执行打包命令
mvn clean install -Dmaven.test.skip=true -am -pl union/blockchain-data-sync
```

**修改配置文件**

blockchain-data-sync 的启动依赖 resource/application.yml 配置文件

修改 mongodb 相关配置项

```yml
spring:
  datasource:
    mongodb:
      uri: mongodb://user:pwd@127.0.0.1:37016/wefe_union
      databaseName: wefe_union
```

修改文件区块链连接相关配置项。

```yml
sdk:
  # 区块链节点 ip
  ip: 127.0.0.1
  # 区块链端口
  channelPort: 20200
  # sdk 证书所在目录
  # 根证书 ca.crt | sdk 证书 sdk.crt | sdk 秘钥 sdk.key
  certPath: xx/xx/cert  # cert path of relative or absolute
```


修改文件合约相关配置项。

```yml
contract:
  # 需要同步的群组 id，多个以逗号分隔
  data-sync-group-id: 1
  # 合约目录
  # 启动服务前必须把需要同步数据合约的 abi 文件放在当前目录下的 abi 目录里面，bin 文件放在当前目录下的 bin 目录下面
  solidity-path: union/blockchain-data-sync/solidity
```

修改文件报警企业微信机器人地址，数据同步异常会发报警信息到企业微信。

```bash

wechat:
  bot-url: https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=[Your Key]

```

**启动项目**

```bash
java -jar blockchain-data-sync.jar
```
