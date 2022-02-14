union 模块是联邦信息登记服务，使用区块链作为底层存储，是各成员在发起建模前互相了解的媒介。

# union-service

union-service 是联邦学习的 union 的后台服务；

union-service 使用去中心化的数据存储方式，存储联邦成员的公开信息，比如成员信息、公开的数据集信息；

union-service 提供 http 接口供各成员修改自身公开信息以及获取联邦中其他成员的公开信息。

## 项目特点

使用 Springboot + Mongodb + Fisco bcos(金链盟) 架构方式；

去中心化、防篡改、可追溯性。

## 环境要求

在使用本组件前，请确认系统环境已安装相关依赖软件，清单如下：

| 依赖软件 | 说明 |备注|
| --- | --- | --- |
| Java | JDK[1.8] ||
| Mongodb | >= Mongodb[4.0] | |
| Fisco bcos | Wefe 内部提供的二进制安装包 | |

### *注意点*

由于目前board模块的忘记密码要发短信验证码功能，而发短信功能暂时集合在union（board调用union的发短信接口）且默认使用阿里云通道，
因此如果想正常启用忘记密码发短信功能，则要修改union的application.properties配置：

```ini
# 阿里云ID
aliyun.access.key.id=xxxxx
# 阿里云Secret
aliyun.access.key.secret=xxxx
# 短信签名
sms.aliyun.sign.name=xxx
# 忘记密码的短信模板编码
sms.aliyun.account.forget.password.verification.code.template.code=SMS_227737637
```
*PS*: 关于如何开通阿里云短信服务产品请参数阿里云官网：https://www.aliyun.com/product/sms

