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

