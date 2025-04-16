# v2.5.0
`2025年04月14日`

当前版本不提供Docker的部署方案，如果想通过源码编译当前版本请参考原来系统架构下各模块的[源码编译方式。]( https://tianmiantech.github.io/WeFe/#/release/release )  
当前版本提供v3.8.6版本（源码暂时未开源）的联邦学习平台以及联邦SQL服务的Docker部署方案，部署文档如下：
- 联邦学习平台的部署文档请参考[WeFe隐私计算服务部署说明文档.md]( https://github.com/tianmiantech/WeFe/blob/main/deploy/WeFe%E9%9A%90%E7%A7%81%E8%AE%A1%E7%AE%97%E6%9C%8D%E5%8A%A1%E9%83%A8%E7%BD%B2%E8%AF%B4%E6%98%8E%E6%96%87%E6%A1%A3.md )；
- 联邦SQL服务的部署文档请参考[WeFe隐私计算服务联邦SQL部署说明文档.md]( https://github.com/tianmiantech/WeFe/blob/main/deploy/WeFe%E9%9A%90%E7%A7%81%E8%AE%A1%E7%AE%97%E6%9C%8D%E5%8A%A1%E8%81%94%E9%82%A6SQL%E9%83%A8%E7%BD%B2%E8%AF%B4%E6%98%8E%E6%96%87%E6%A1%A3.md )；



#### What's new
**「 feature & optimize 」**  

部署文档：
- 支持简单快速的基于Docker的部署脚本，便于用户快速搭建联邦学习平台。
- 支持简单快速的基于Docker的部署脚本，便于用户快速搭建联邦SQL服务。

Kernel：
- 修复已知bug；

Board：
- 接入IAM改造；
- 数据库敏感字段加密保存处理；
- 支持腾讯云函数计算实现；
- 系统增加 cors 域名限定配置项；
- storage 模块重构,配置从配置文件迁移到页面；
- 启动任务时对数据集格式进行检查；
- 优化组件参数编辑面板中的保存按钮位置；
- 对部分组件入参做优化处理；
- 修复已知bug；

Gateway:
- 支持TLS传输；
- 修复已知bug；


Serving：
- 接入IAM改造；
- 系统增加 cors 域名限定配置项；
- 修复已知bug；

Union：
- 对新增gateway的TLS支持修改
- 配置项的优化；
- 修复已知bug；

Manager：
- 接入IAM改造；
- 配置项的优化；
- 完善CA 证书管理功能；
- 修复已知bug；

Fusion：
- 接入IAM改造；
- 系统增加 cors 域名限定配置项；
- 修复已知bug；

# v2.4.0
`2023年11月23日`

阿里云 OSS 下载源：

    https://welab-wefe-release.oss-cn-shenzhen.aliyuncs.com/welab_wefe_v.2.4.0.tar

#### What's new

**「 feature & optimize 」**

Kernel：
- 优化Paillier同态运算的性能；
- 重构并增加了若干组件，满足深度使用时的更多需求；
- 纵向xgb增加工作模式normal、layered、skip、dp
- 纵向lr增添sshe-lr方式
- 将部分函数改用 GPU 执行，提升了建模效率；
- 增加了基于联邦深度学习的图像视觉（CV）算法，可用于图像分类和目标检测；
- 大量的优化，更快，更稳定。


Board：
- 配合 CV 任务，增加了在线标注图片功能；
- 增加了系统检测功能，可快速查看系统中各服务的可用状态和异常原因；
- 增加了数据融合功能；
- 大量的优化。


Serving：
- 增加导出 sdk 功能，可使用 sdk 快速开发服务调用程序；
- 增加多方安全统计服务类型，可安全地对多个成员的数据进行统计；
- 增加计费模块，可自定义计费规则，能统计与查看费用明细；
- 重构了 serving 模块。


Union：
- 优化了区块链数据同步程序，延迟更低，更稳定；
- 重构了 union 模块。


Manager（新增）：
- 成员管理，可对联邦中的成员进行冻结、恢复等操作；
- 资源管理，可对联邦中的资源进行禁用、启动等操作；
- 可对成员进行实名认证审核，审核通过后发放 CA 证书；
- 其它对联邦的管理功能。


Fusion：
- 重构了 fusion 模块；


Other：
- 重构了配置文件体系，将绝大部分配置改用界面可视化编辑；
- 支持国密算法。


**「 Bugs 」**

- 修复 fastjson 漏洞；
- 修复 log4j 漏洞；
- 增加了舆情关键词检测，避免用户上传不合法的内容；
- 增加了图片上传时的木马病毒过滤，避免上传的图片包含不安全的内容；
- 增加了 xss 检测，避免上传的文件包含可执行脚本；
- 其它问题。

---

# v2.3.0
`2022年2月14日`

阿里云 OSS 下载源：

    https://welab-wefe-release.oss-cn-shenzhen.aliyuncs.com/welab_wefe_v.2.3.tar

#### What's new

「 新增功能 」

- 新增深度学习组件；
- 新增混合特征分箱，混合特征统计组件；
- 新增热编码，两方 PCA 组件, 样本筛选, 特征转换等组件；
- 新增横向特征分箱，横向特征统计组件；
- 新增模型列表/衍生数据集类型；
- 新增混合流程模板；

「 Bugs 修复 」

- 启动流程时，如果指定了结束节点，但结束节点没有父节点打通，应当提示相应信息而不该报错；
- 成员被其他正式成员拒绝加入后重新再添加时，之前的审核记录表没有被删除；
- serving 在线调试功能 API 入参格式问题；
- serving 横向联邦 LR 预测 sigmod 函数处理问题。

---

# v2.2.0
`2021年10月12日`

阿里云 OSS 下载源：

    https://welab-wefe-release.oss-cn-shenzhen.aliyuncs.com/welab_wefe_v.2.2.tar

#### What's new
* 开源的第一个版本


