# 常见问题
- [常见问题](#常见问题)
  - [一、WeFe 编译、部署](#一wefe-编译部署)
    - [1、Union 如何部署？](#1union-如何部署)
    - [2、Maven 打包出现 jar 包读取出错](#2maven-打包出现-jar-包读取出错)
    - [3、前台执行 npm install 会出现 not a git respoitory (or any of the parent directories): .git](#3前台执行-npm-install-会出现-not-a-git-respoitory-or-any-of-the-parent-directories-git)
    - [4、执行 npm install 出现 cannot find module](#4执行-npm-install-出现-cannot-find-module)
    - [5、board 验证码刷不出来？](#5board-验证码刷不出来)
    - [6、Flow单独启动时，intranet_base_url值没有初始化报'BoardConfigModel' object has no attribute 'intranet_base_url'](#6flow单独启动时intranet_base_url值没有初始化报boardconfigmodel-object-has-no-attribute-intranet_base_url)
    - [7、serving前端install报错 找不到这个welabx](#7serving前端install报错-找不到这个welabx)
    - [8、serving前端启动 npm run dev 报错](#8serving前端启动-npm-run-dev-报错)
    - [9、前端项目 npm run build 后，访问空白页？](#9前端项目-npm-run-build-后访问空白页)
  - [二、wefe.cfg 配置填写相关问题](#二wefecfg-配置填写相关问题)
    - [1、wefe.cfg配置`INTRANET_IP`值配置为0.0.0.0访问不到Board后端问题](#1wefecfg配置intranet_ip值配置为0000访问不到board后端问题)
    - [2、union模块下的`block.chain.toml.file.path`配置是指什么？](#2union模块下的blockchaintomlfilepath配置是指什么)
  - [三、WeFe 建模流程相关问题](#三wefe-建模流程相关问题)
    - [1、oot 是什么？](#1oot-是什么)
    - [2、通过docker部署运行的系统，编辑数据集可见性中，选择对指定成员可见，为什么没有操作按钮呢？(我把docker中的union地址改成了我这里自己搭建的union）？](#2通过docker部署运行的系统编辑数据集可见性中选择对指定成员可见为什么没有操作按钮呢我把docker中的union地址改成了我这里自己搭建的union)
    - [3、添加数据集，没显示关键字](#3添加数据集没显示关键字)
    - [4、横向建模选择数据集时选择不到协作方的数据。](#4横向建模选择数据集时选择不到协作方的数据)
    - [5、初始化系统，联邦信息从哪里获取？ 私钥和公钥信息哪里获取？](#5初始化系统联邦信息从哪里获取-私钥和公钥信息哪里获取)
    - [6、WeFe 系统提示没有成员是因为什么呢?](#6wefe-系统提示没有成员是因为什么呢)
    - [7、请问任务启动后再次启动提示是否使用缓存，这个缓存指什么？](#7请问任务启动后再次启动提示是否使用缓存这个缓存指什么)
    - [8、项目中创建的流程包含多个节点，节点间的数据是通过什么存储引擎进行存储的？](#8项目中创建的流程包含多个节点节点间的数据是通过什么存储引擎进行存储的)
  - [四、服务搭建相关](#四服务搭建相关)
    - [1、自己部署的`Union`服务其他人也可以无限制的加入?](#1自己部署的union服务其他人也可以无限制的加入)
    - [2、是否支持spark集群版？](#2是否支持spark集群版)
    - [3、kernel 需要启动吗?](#3kernel-需要启动吗)
  - [五、开发环境](#五开发环境)
    - [1、JDK用的哪个版本？](#1jdk用的哪个版本)
  - [六、算法相关](#六算法相关)
    - [1、WeFe 只采用了`Paillier`一种加密算法吗？](#1wefe-只采用了paillier一种加密算法吗)
    - [2、请问什么时候用到同态加密算法？](#2请问什么时候用到同态加密算法)
    - [3、两种同态加密算法有何区别？](#3两种同态加密算法有何区别)
    - [4、请问`IterativeAffine`算法的参数是怎么样设置的？](#4请问iterativeaffine算法的参数是怎么样设置的)
  - [六、区块链](#六区块链)
    - [1、A和B达成协议，创建了金链盟服务。而C如果也搭建金链盟服务，以共识节点加入A、B的区块链。是否可以拿到A、B的交易信息？](#1a和b达成协议创建了金链盟服务而c如果也搭建金链盟服务以共识节点加入ab的区块链是否可以拿到ab的交易信息)
    - [2、在fisbcos用快速搭建方法时，union模块的block.chain.toml.file.path配置项不清楚征书的问题？](#2在fisbcos用快速搭建方法时union模块的blockchaintomlfilepath配置项不清楚征书的问题)
    - [3、block.chain.toml.file.path路径配置正确，证书也确认无误，但还是认证失败？](#3blockchaintomlfilepath路径配置正确证书也确认无误但还是认证失败)
    - [4、本地虚拟机 Ubuntu 20 在安装 fisco bcos 时报错](#4本地虚拟机-ubuntu-20-在安装-fisco-bcos-时报错)
  - [七、文档相关](#七文档相关)
    - [1、有横向lr训练过程的流程图之类的资料吗？](#1有横向lr训练过程的流程图之类的资料吗)

## 一、WeFe 编译、部署

#### 1、Union 如何部署？
> 答：目前Union服务暂时不提供docker的快速部署方式，如果要部署目前只能手动打包。具体可参考[**Union 文档**](system_framework/union.md)

#### 2、Maven 打包出现 jar 包读取出错
> 答：可能是下载依赖包时，网络中断导致没下载完，建议把`Maven`仓库下对应出问题的文件夹删除，然后重新执行 mvn install

#### 3、前台执行 npm install 会出现 not a git respoitory (or any of the parent directories): .git
> 答：打开 package.json 删除 script prepare 这行脚本

#### 4、执行 npm install 出现 cannot find module
> 答: 1) 尝试移除`package-lock.json`文件重试，果仍然失败， 尝试使用`yarn`进行安装

#### 5、board 验证码刷不出来？
> 答：可能是数据库链接不上，Board 未正常启动

#### 6、Flow单独启动时，intranet_base_url值没有初始化报'BoardConfigModel' object has no attribute 'intranet_base_url'
> 答：目前不建议单独启动，如果要单独启动则要修改数据库初始化intranet_base_url值

#### 7、serving前端install报错 找不到这个welabx
> 答：删除package-lock 重试，或者执行 npm install --force

#### 8、serving前端启动 npm run dev 报错
> 答：试试npm 6.x

#### 9、前端项目 npm run build 后，访问空白页？
> 答：npm run build 打包时必须带有上下文  避免Nginx与其他系统冲突, 如 npm run build --prod=wefe

## 二、wefe.cfg 配置填写相关问题

#### 1、wefe.cfg配置`INTRANET_IP`值配置为0.0.0.0访问不到Board后端问题
> 答：请填写真实的本机IP地址（windows使用ipconfig命令查看，unix系统使用ifconfig查看）

#### 2、union模块下的`block.chain.toml.file.path`配置是指什么？
> 答：该配置是指连接区块链所需的证书位置

## 三、WeFe 建模流程相关问题

#### 1、oot 是什么？
> 答：oot 是 out of time 的简称，表示时间外的数据，即拿不同时间段的数据对模型进行打分验证。

#### 2、通过docker部署运行的系统，编辑数据集可见性中，选择对指定成员可见，为什么没有操作按钮呢？(我把docker中的union地址改成了我这里自己搭建的union）？
> 答：git项目代码都正常，本地服务里也正常，建议重新打包验证验证。

#### 3、添加数据集，没显示关键字
> 答：当前版本如果自己搭建union的由于默认关键词没有初始化所以在添加数据集的时候查询不到，你添加数据集可以先自定义关键词，`默认关键词`由另外一个服务维护(manager)。

#### 4、横向建模选择数据集时选择不到协作方的数据。
> 答：可能是协作方的数据不包含 Y，因为横向建模在选择数据集时是强制包含过滤条件：包含Y的数据集

#### 5、初始化系统，联邦信息从哪里获取？ 私钥和公钥信息哪里获取？
> 答：联邦信息在WeFe系统设置里，密钥在数据库 表名global_config中查看

#### 6、WeFe 系统提示没有成员是因为什么呢?
> 答：可能是因为连不上公网`Union`,如需要访问公网`Union`,需要您的服务器允许访问外网

#### 7、请问任务启动后再次启动提示是否使用缓存，这个缓存指什么？
> 答：缓存是指上次任务中节点的执行的结果缓存后就下次执行就直接跳过，保留结果，不会重新执行。

#### 8、项目中创建的流程包含多个节点，节点间的数据是通过什么存储引擎进行存储的？
> 答：根据配置db.storage.type，存储到LMDB或者Clickhouse中。如果是组件计算过程中产生的中间数据，会由Cleaner类在过程中清理，另外也会在组件执行结束清理。如果是组件的最终输出，则会持久化，不清理。


## 四、服务搭建相关

#### 1、自己部署的`Union`服务其他人也可以无限制的加入?
> 答：是的

#### 2、是否支持spark集群版？
> 答：是的，在2.3版本后会支持

#### 3、kernel 需要启动吗?
> 答：kernel 是不需要手动启动的。

## 五、开发环境

#### 1、JDK用的哪个版本？
> 答：JDK 1.8

## 六、算法相关

#### 1、WeFe 只采用了`Paillier`一种加密算法吗？
> 答：两种，分别是`Paillier`和`IterativeAffine`

#### 2、请问什么时候用到同态加密算法？
> 答：横向不用同态加密，纵向跟混合需要同态加密。

#### 3、两种同态加密算法有何区别？
> 答：性能上面有差异，`IterativeAffine`更快些

#### 4、请问`IterativeAffine`算法的参数是怎么样设置的？
> 答：该算法无需设置参数

## 六、区块链

#### 1、A和B达成协议，创建了金链盟服务。而C如果也搭建金链盟服务，以共识节点加入A、B的区块链。是否可以拿到A、B的交易信息？
> 答：是的

#### 2、在fisbcos用快速搭建方法时，union模块的block.chain.toml.file.path配置项不清楚征书的问题？
答：如果是参考fisbcos的快速搭建方法，直接配置到该目录即可：${root}/nodes/127.0.0.1/sdk

#### 3、block.chain.toml.file.path路径配置正确，证书也确认无误，但还是认证失败？
> 答：换成openjdk 1.8尝试下

#### 4、本地虚拟机 Ubuntu 20 在安装 fisco bcos 时报错
> 答：文档的默认操作是基于 Centos 7.5 的，Ubuntu 的操作请参考官方文档：https://fisco-bcos-documentation.readthedocs.io/zh_CN/latest/docs/manual/get_executable.html 

## 七、文档相关

#### 1、有横向lr训练过程的流程图之类的资料吗？
> 答：请参考WeFe项目中`kernel/docs/逻辑回归.md`文档

