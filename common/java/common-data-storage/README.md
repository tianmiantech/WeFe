#使用说明
> 引用方不是springboot工程的,请在程序启动设置config.path
    1,在启动类初始化storage模块,StorageManager.getInstance().init();
    2,项目启动时候设置数据库配置文件地址config.path,例如:java -jar -Dwefe.common.config.path=G://wefe/config.properties