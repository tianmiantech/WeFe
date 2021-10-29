# 函数计算部署

- [函数计算部署](#函数计算部署)
  - [一、服务开通](#一服务开通)
    - [1. FC 函数计算](#1-fc-函数计算)
    - [2. OSS（对象存储）](#2-oss对象存储)
    - [3. NAS（文件存储）](#3-nas文件存储)
    - [4. LOG（日志服务）可选，用于函数计算执行日志的关联](#4-log日志服务可选用于函数计算执行日志的关联)
  - [二、相关配置（按需配置）](#二相关配置按需配置)
    - [1. OSS 服务配置](#1-oss-服务配置)
      - [1.1、创建 bucket](#11创建-bucket)
      - [1.2、OSS 服务相关RAM权限配置](#12oss-服务相关ram权限配置)
    - [2. LOG 服务配置](#2-log-服务配置)
      - [2.1、创建 Project](#21创建-project)
      - [2.2、创建 Logstore](#22创建-logstore)
    - [3. FC 函数计算服务配置](#3-fc-函数计算服务配置)
      - [3.1、函数部署相关权限配置](#31函数部署相关权限配置)
    - [4、项目参数配置](#4项目参数配置)

## 一、服务开通

> 本次服务开通注意事项：

函数计算：<font color='red'> 必选 </font> \
文件存储 (NAS): <font color='red'> 必选 </font> \
云存储 OSS: <font color='red'> 必选 </font> \
日志：<font color='grey'> 可选 </font>

### 1. FC 函数计算

> **函数计算服务** [点击](https://www.aliyun.com/product/fc) , 去往管理控制台

### 2. OSS（对象存储）

> 开通阿里云 **对象存储** 服务 [点击开通](https://www.aliyun.com/product/oss)

### 3. NAS（文件存储）

> 开通阿里云 **文件存储** 服务 [点击开通](https://www.aliyun.com/product/nas)

### 4. LOG（日志服务）可选，用于函数计算执行日志的关联

> 开通阿里云 **文件存储** 服务 [点击开通](https://www.aliyun.com/product/log)

## 二、相关配置（按需配置）

### 1. OSS 服务配置

#### 1.1、创建 bucket

![](../../images/fc/create_oss_bucket.png)

![](../../images/fc/create_oss_bucket_1.png)

注：请留意 bucket 名称，后续配置需用到

#### 1.2、OSS 服务相关RAM权限配置

- 前往RAM控制台，在权限策略管理中创建权限策略，如图：

![](../../images/fc/create_ossRead_policy.png)

> 策略名称：oss-readOnly

脚本配置的 json 如下：

```json
{
  "Version": "1",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "oss:Get*",
      "Resource": "acs:oss:*:*:{bucket_name}/wefe_transfer/*"
    },
    {
      "Effect": "Allow",
      "Action": "oss:ListObjects",
      "Resource": "acs:oss:*:*:{bucket_name}",
      "Condition": {
        "StringLike": {
          "oss:Prefix": "wefe_transfer/*"
        }
      }
    }
  ]
}
```
```
配置中 Resource 格式为 "acs:oss:{region}:{bucket_owner}:{bucket_name}"

例如："acs:oss:*:*:wefe-fc/wefe_transfer/*" 表示允许访问 "wefe_fc" bucket 下的 wefe_transfer 目录下的所有文件
```

- 创建 RAM 角色，如图：

![](../../images/fc/create_ram_role.png)

> 填写角色名称：wefe-fc-ossRead，如图：

![](../../images/fc/create_fc_role_1.png)

![](../../images/fc/create_ram_role_oss.png)

> 添加权限，将之前创建好的权限 oss-readOnly 添加到 wefe-fc-ossRead 角色

![](../../images/fc/add_policy_oss.png)

> 注：添加权限前，将最大会话时间调整到最大值，如图:

![](../../images/fc/oss_max_duration_time.png)

> 添加  oss-readOnly 权限

![](../../images/fc/choice_oss_readOnly_policy.png)


### 2. LOG 服务配置

#### 2.1、创建 Project

![](../../images/fc/create_log_project.png)

![](../../images/fc/create_log_project_1.png)

注：记录创建的 Project 名称，后续修改配置需用到

#### 2.2、创建 Logstore

![](../../images/fc/create_log_project_2.png)

注：记录创建的 Logstore 名称，后续修改配置需用到


### 3. FC 函数计算服务配置

#### 3.1、函数部署相关权限配置

- 创建自定义策略方法同上。

> 策略名称：wefe-fc-policy

以下是权限配置的json

```json
{
  "Version": "1",
  "Statement": [
    {
      "Action": [
        "ecs:CreateNetworkInterface",
        "ecs:DeleteNetworkInterface",
        "ecs:DescribeNetworkInterfaces",
        "ecs:CreateNetworkInterfacePermission",
        "ecs:DescribeNetworkInterfacePermissions",
        "ecs:DeleteNetworkInterfacePermission",
        "ecs:CreateSecurityGroup",
        "ecs:AuthorizeSecurityGroup"
      ],
      "Resource": "*",
      "Effect": "Allow"
    },
    {
      "Action": [
        "ram:DeletePolicyVersion",
        "ram:CreatePolicyVersion",
        "ram:CreateServiceLinkedRole",
        "ram:GetRole",
        "ram:PassRole"
      ],
      "Resource": "*",
      "Effect": "Allow"
    },
    {
      "Action": [
        "sts:AssumeRole"
      ],
      "Resource": "*",
      "Effect": "Allow"
    },
    {
      "Action": [
        "vpc:CreateVSwitch",
        "vpc:DescribeVSwitchAttributes"
      ],
      "Resource": "*",
      "Effect": "Allow"
    }
  ]
}
```  

- 创建用于配置函数计算 template.yml文件的role角色(RAM 角色)

> 角色名称：wefe-fc-role

1、创建RAM 角色

- 选择阿里云服务

![](../../images/fc/create_fc_role_1.png)

- 填写角色名称：wefe-fc-role, 同时选择受信服务：函数计算, 创建完成。

![](../../images/fc/create_fc_role_2.png)

2、授权

- 给 wefe-fc-role 赋予相关系统权限，如下所示：

```
AliyunOTSFullAccess
AliyunFCFullAccess
AliyunLogFullAccess
AliyunNASFullAccess
```

- 添加自定义权限：

将上述步骤创建的策略：wefe-fc-policy, 添加到该 RAM 角色中，最终如下图所示：

![](../../images/fc/create_fc_role.png)


> 创建 API 用户（子账户），同时赋予 API 用户相关权限

![](../../images/fc/create_api_user.png)

1、赋予相关系统权限，如下所示：

```
AliyunOSSFullAccess
AliyunRAMReadOnlyAccess
AliyunSTSAssumeRoleAccess
AliyunLogFullAccess
AliyunFCFullAccess
AliyunNASFullAccess
AliyunVPCFullAccess
AliyunCloudMonitorFullAccess
```

注：系统权限添加一次只能5个，剩下的请再次添加进去，如下：

![](../../images/fc/add_policy.png)

2、添加自定义权限：

> 将上述步骤创建的策略：wefe-fc-policy, 添加到该 API 用户

3、创建完成后，注意保存 <font color='red'> AccessKey ID、AccessKey Secret </font>等信息，后续配置需要用到

![](../../images/fc/create_Access_key.png)

### 4、项目参数配置

- 统一修改 wefe.cfg 文件进行配置

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




