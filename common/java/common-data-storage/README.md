# Storage

storage 模块负责实现三个场景的数据存取功能：

1. 持久化存储（persistent storage）：以 clickhouse、lmdb、mysql 等方式实现，用于持久化储存数据集。
2. 函数计算存储（fc storage）：为了提高函数计算的 IO 效率，在使用函数计算执行任务时会将数据集从持久化存储中取出到OSS进行计算，最后将结果再落回到持久化存储。
3. 中间件存储（middleware storage）：中间件存储是对持久化存储的补充，提供了缓存、队列等功能。


持久化储存使用示例：

```java
// 初始化 storage 实例
PersistentStorage.init(new ClickhouseConfig("127.0.0.1",8123,"user","pasdword"));
// 使用新的配置重新初始化 storage
PersistentStorage.init(new ClickhouseConfig("127.0.0.1",8123,"user","pasdword"));

// 使用 storage
PersistentStorage.getInstance().put("wefe","test",new DataItemModel("a","123"));
```


FC储存使用示例：

```java
// 初始化 storage 实例
FcStorage.init(new AliyunOssConfig(...));
// 使用新的配置重新初始化 storage
FcStorage.init(new AliyunOssConfig(...));

// 使用 storage
FcStorage.getInstance().putAll("");
```
