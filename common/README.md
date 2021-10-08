# common.java

Java 服务的公共模块。

**common-data-storage**

持久化数据及算法中间数据的存储实现，目前支持：
- ClickHouse；
- LMDB；
- 云存储 OTS、OSS。

**common-data-mongodb**

区块链数据同步到业务库 (mongodb) 的存储实现。
# common.python

Python 服务的公共模块，包含常用的工具层、数据的存储交互层以及分布式计算引擎接入层等。

## 模块结构

**calculation**

提供计算相关的实现，目前支持：
- Spark 分布式计算引擎；
- 函数计算；
- GPU 硬件加速（实验版本）。

**common**

定义系统常量，主要包括统一异常的定义，常量定义，枚举定义等。

**db**

实现了业务数据库的具体操作方法，目前支持的 DB 类型：
- MySQL
- SQLite

**dto**

数据传输对象

**p_federation**

成员间数据交互的实现，提供单机版及集群版的方案

**p_session**

存储及计算功能的会话实现

**protobuf**

协议文件，包括数据传输交互的协议及部分存储结构的协议

**storage**

持久化数据及算法中间数据的存储实现，目前支持：
- ClickHouse；
- LMDB；
- 云存储 OTS、OSS。

**utils**

常用的工具类的方法，包括缓存、序列化、配置、日志等