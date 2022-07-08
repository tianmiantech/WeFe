# v2.2.0
`2021年10月12日`

阿里云 OSS 下载源：

    https://welab-wefe-release.oss-cn-shenzhen.aliyuncs.com/welab_wefe_v.2.2.tar

#### What's new
* 开源的第一个版本

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
