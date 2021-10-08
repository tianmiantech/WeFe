# 函数计算

函数计算是一个serverless云服务，提供弹性的扩容机制，并按量计费，非常适用于联邦学习这种需要大量算力，而又并非需要持续运行的场景。

目前函数计算的方案支持阿里云的`函数计算`服务

## 指定计算引擎为 函数计算

在部署的配置文件 `wefe.cfg` 编辑指定配置项 `CALCULATION_ENGINE=FC`，然后根据部署文档修改以下配置项。

>  Tips：函数计算的开通指南参考 [函数计算部署指南](/install/install_fc) 章节

## 相关配置项

> 温馨提示：下面需要配置的参数如：FC_ACCESS_KEY_ID、FC_ACCESS_KEY_SECRET 即为前面创建的API账户的 AccessKey ID 与 AccessKey

```
##### 函数计算的相关配置  #####

# 阿里云账号UID
FC_ACCOUNT_ID="xxx"

# 函数计算所在的区域，建议:cn-shenzhen
FC_REGION="cn-shenzhen"

# 账户ID
FC_ACCOUNT_ID=294***9042

# 提供API访问的access_key_id
FC_ACCESS_KEY_ID="LTA***ND7"

# 提供API访问的access_key_secret
FC_ACCESS_KEY_SECRET="nxL***sfv"

# 函数计算存储类型
FC_STORAGE_TYPE=oss

# oss bucket 名称
FC_OSS_BUCKET_NAME="xxx"
```





