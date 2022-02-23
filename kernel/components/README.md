# WEFE联邦学习平台算法清单

## 算法列表

| 算法                          | 描述                                                         | 模块名                        | 数据输入                                          | 数据输出                                                     | 模型输入 | 模型输出                                     |
| ----------------------------- | ------------------------------------------------------------ | ----------------------------- | ------------------------------------------------- | ------------------------------------------------------------ | -------- | -------------------------------------------- |
| [DataIO](../docs/特征工程.md)                        | 该组件将原始数据转换为Instance对象                           | DataIO                        | Table，值为原始数据                               | 转换后的数据表，值为在 : /kernel/base/instance.py中定义的Data Instance的实例 |          | DataIO模型                                   |
| [Intersect](../docs/样本对齐.md)                     | 计算两方的相交数据集，而不会泄漏任何差异数据集的信息。主要用于纵向任务 | Intersection                  | Table                                             | 两方Table中相交的部分                                        |          |                                              |
| [Vertical  Binning](../docs/特征工程.md)       | 使用分箱的输入数据，计算每个列的iv和woe，并根据合并后的信息转换数据 | Vertical Feature Binning      | Table，在promoter中有标签y，在provider中没有标签y | 转换后的Table                                                |          | 每列的iv/woe，分裂点，事件计数，非事件计数等 |
| [Horizontal Feature Binning](../docs/特征工程.md)     | 计算横向场景的等频分箱                                       | Horizontal Feature Binning    | Table                                             | 转换后的Table                                                |          | 每列的iv/woe，分裂点，事件计数，非事件计数等 |
| [Mix Feature Binning](../docs/混合联邦.md)           | 计算混合场景的等频分箱                                       | Mix Feature Binning           | Table                                             | 转换后的Table                                                |          | 每列的iv/woe，分裂点，事件计数，非事件计数等 |
| [Vertical Pearson](../docs/特征工程.md)                   | 计算横向场景的Pearson相关系数                                | Horizontal Pearson            | Table                                             |                                                              |          |                                              |
| [WOE](../docs/特征工程.md)                           | WOE编码                                                      | WOE                           | Table                                             | 转换后的Table                                                |          |                                              |
| [Feature Transform](../docs/特征工程.md)             | 特征转换编码                                                 | Feature Transform             | Table                                             | 转换后的Table                                                |          |                                              |
| [Sample Filter](../docs/特征工程.md)                 | 样本过滤，根据条件筛选样本                                   | Sample Filter                 | Table                                             |                                                              |          |                                              |
| [OneHot](../docs/特征工程.md)                        | 将一列转换为One-Hot格式。                                    | OneHot                        | Table                                             | 转换了带有新列名的Table                                      |          |                                              |
| [Feature standardized](../docs/特征工程.md)          | 特征归一化和标准化。                                         | Feature standardized          | Table，其值为instance                             | 转换后的Table                                                |          |                                              |
| [Feature Soften](../docs/特征工程.md)                | 特征异常平滑                                                 | Feature Soften                | Table，其值为instance                             | 转换后的Table                                                |          |                                              |
| [Vertical Statistic](../docs/特征工程.md)            | 计算样本统计量，均值，最大值，最小值，方差，标准差，缺失量   | Vertical Statistic            | Table，其值为instance                             |                                                              |          | Statistic Result                             |
| [Horizontal Statistic](../docs/特征工程.md)          | 计算横向场景的统计量                                         | Horizontal Statistic          | Table，其值为instance                             |                                                              |          | Statistic Result                             |
| [Mix Statistic](../docs/混合联邦.md)                | 计算混合场景的统计量                                         | Mix Statistic                 | Table，其值为instance                             |                                                              |          | Statistic Result                             |
| [Feature Filling Missing Value](../docs/特征工程.md)  | 使用最大值、最小值、均值、自定义值填充缺失值                 | Feature Filling Missing Value | Table，其值为instance                             | 转换后的Table                                                |          |                                              |
| [PCA](../docs/特征工程.md)                            | 主成分分析                                                   | PCA                           | Table，其值为instance                             |                                                              |          |                                              |
| K Means                       | 构建K均值模块                                                | 构建K均值模块                   | Table，其值为instance                             |                                                              |          |                                              |
| [Vertical Logistic Regression](../docs/逻辑回归.md)    | 多方共同完成纵向逻辑回归建模                           | VertLR                        | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | LR模型                                   |
| [Horizontal Logistic Regression](../docs/逻辑回归.md) | 多方共同完成横向逻辑回归建模                           | HorzLR                        | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | LR模型                                   |
| [Mixture Logistic Regression](../docs/混合联邦.md)    | 多方共同完成混合逻辑回归建模                           | MixLR                         | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | LR模型                                   |
| [Vertical SecureBoost](../docs/SecureBoost.md)           | 多方共同完成纵向xgboost建模                           | VertSecureBoost               | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | Xgboost模型                                   |
| [Horizontal SecureBoost](../docs/SecureBoost.md)         | 多方共同完成横向xgboost建模                           | HorzSecureBoost               | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | Xgboost模型                                   |
| [Mixture SecureBoost](../docs/混合联邦.md)            | 多方共同完成混合xgboost建模                           | MixSecureBoost                | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | Xgboost模型                                   |
| Vertical neural network        | 多方共同完成纵向神经网络建模                           | VertNN                        | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | 神经网络模型                                   |
| Horizontal neural network      | 多方共同完成横向神经网络建模                           | HorzNN                        | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | 神经网络模型                                   |
| [evaluation](../docs/特征工程.md)                     | 模型评估组件                                         | evaluation                    | Table，算法建模后的结果                               | talbe，模型评估的结果 |          |                                   |



## 安全算法

1. 公钥密码算法

   - [Paillier算法](../docs/同态加密算法.md)

   - [RSA算法](../docs/RSA密码算法和DH密码算法.md.md)

2. [Diffie-Hellman密钥协商算法](../docs/RSA密码算法和DH密码算法.md.md)

3. [SecretShare MPC 协议（SPDZ）](../docs/SPDZ.md)

4. 密码算法优化定理
   - [中国剩余定理](../docs/中国剩余定理.md)
