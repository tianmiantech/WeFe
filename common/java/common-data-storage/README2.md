# Storage

storage 模块负责实现三个场景的数据存取功能：

1. 持久化存储（persistent storage）：默认以 clickhouse 实现，用于持久化储存数据集。
2. 函数计算存储（fc storage）：为了提高函数计算的 IO 效率，在使用函数计算执行任务时会将数据集从持久化存储中取出到OSS进行计算，最后将结果再落回到持久化存储。
3. 中间件存储（middleware storage）：中间件存储是对持久化存储的补充，提供了缓存、队列等功能。

如果引用方是springboot项目请不要扫描storage模块的下面的bean

## 1. 添加依赖

```bash
<dependency>
    <groupId>com.welab.wefe</groupId>
    <artifactId>common-data-storage</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 2. 初始化跟更新配置 (刷新配置后不需要重启服务)

```bash
#初始化
StorageConfig storageConfig = new StorageConfig(x...);
StorageManager.getInstance().init(storageConfig);

#更新jdbc配置
StorageManager.getInstance().refreshJdbcConfig(new JdbcConfig(x...));
#更新lmdb配置
StorageManager.getInstance().refreshLmdbConfig(new LmdbConfig(x...));
#更新函数计算存储配置
StorageManager.getInstance().refreshFcStorageConfig(new FcStorageConfig(x...));



```

## 3. 使用

```bash
#获取StorageService
protected StorageService storageService = StorageManager.getInstance().getRepo(StorageService.class);
#调用方法
storageService.xx();
```