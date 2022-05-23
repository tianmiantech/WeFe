#使用说明
如果引用方springboot项目请不要扫描storage模块的下面的bean
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