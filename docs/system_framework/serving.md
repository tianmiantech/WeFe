serving-service 是一个模型联合在线服务平台，提供了联邦学习联合预测功能。

serving-service 本身是一个独立的服务，其预测所需的模型数据依赖于 WeFe-Member 的模型训练结果的同步。

# 功能实现

单条、批量的预测功能；

基于 RSA 密钥验签保证暴露 API 的安全；

联合建模的多方成员数据不出库，保证数据隐私安全性；

实时记录模型的调用日志、统计调用情况；

结合(serving-website此处是link)通过可视化的页面，多维度的图表监控与观测模型的使用情况；

一键上下线模型，便于控制模型的调用安全性；

提供java-sdk包，简化调用，减少部署维护成本；

支持一键同步模型，基于wefe其他服务训练的模型一键同步

支持在线调试模型。

# 项目的构建与部署

serving-service基于spring-boot框架，采用 maven 进行包管理

## 初始化数据库

执行SQl脚本：serving-init.sql

## 项目打包方法

```
mvn clean install -Dmaven.test.skip=true -am -pl serving/serving-service
```

# SDK 使用方法

导入 sdk

```maven
 <dependencies>
    <dependency>
         <groupId>com.welab.wefe</groupId>
          <artifactId>serving-sdk-java</artifactId>
          <version>1.0.0</version>
      </dependency>
</dependencies>
```

sdk jar 包下载

[下载地址]()