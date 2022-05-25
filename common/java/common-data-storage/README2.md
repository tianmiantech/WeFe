# Storage

storage 模块负责实现三个场景的数据存取功能：

1. 持久化存储（persistent storage）：默认以 clickhouse 实现，用于持久化储存数据集。
2. 函数计算存储（fc storage）：为了提高函数计算的 IO 效率，在使用函数计算执行任务时会将数据集从持久化存储中取出到OSS进行计算，最后将结果再落回到持久化存储。
3. 中间件存储（middleware storage）：中间件存储是对持久化存储的补充，提供了缓存、队列等功能。


## 1. 添加依赖

```bash
<dependency>
    <groupId>com.welab.wefe</groupId>
    <artifactId>common-data-storage</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 2. 使用

```bash
#持久化存储(ck,mysql)初始化
PersistentStorage.init(new ClickhouseConfig("ip",端口,"user","password"));
PersistentStorage storage = PersistentStorage.getInstance();
storage.put("wefe","test",new DataItemModel("123","456"));
List<DataItemModel> list = storage.collect("wefe","test");
```