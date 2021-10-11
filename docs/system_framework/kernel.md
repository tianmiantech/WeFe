# kernel

kernel 是算法组件的核心模块。

# 模块结构

**base**

一些基础类的定义，包括模型参数、数据结构、基础运算等。

**components**

| 算法                          | 模块名                        | 描述                                                         | 数据输入                                          | 数据输出                                                     | 模型输入 | 模型输出                                     |
| ----------------------------- | ----------------------------- | ------------------------------------------------------------ | ------------------------------------------------- | ------------------------------------------------------------ | -------- | -------------------------------------------- |
| DataIO                        | DataIO                        | 该组件将原始数据转换为Instance对象                           | Table，值为原始数据                               | 转换后的数据表，值为在 : /kernel/base/instance.py中定义的Data Instance的实例 |          | DataIO模型                                   |
| Intersect                     | Intersection                  | 计算两方的相交数据集，而不会泄漏任何差异数据集的信息。主要用于纵向任务 | Table                                             | 两方Table中相交的部分                                        |          |                                              |
| Vertical Feature Binning      | Vertical Feature Binning      | 使用分箱的输入数据，计算每个列的iv和woe，并根据合并后的信息转换数据 | Table，在promoter中有标签y，在provider中没有标签y | 转换后的Table                                                |          | 每列的iv/woe，分裂点，事件计数，非事件计数等 |
| Horizontal Feature Binning    | Horizontal Feature Binning    | 计算横向场景的等频分箱                                       | Table                                             | 转换后的Table                                                |          | 每列的iv/woe，分裂点，事件计数，非事件计数等 |
| Mix Feature Binning           | Mix Feature Binning           | 计算混合场景的等频分箱                                       | Table                                             | 转换后的Table                                                |          | 每列的iv/woe，分裂点，事件计数，非事件计数等 |
| Vertical Pearson              | Vertical Pearson              | 计算来自不同方的特征的Pearson相关系数                        | Table                                             |                                                              |          |                                              |
| Horizontal Pearson            | Horizontal Pearson            | 计算横向场景的Pearson相关系数                                | Table                                             |                                                              |          |                                              |
| WOE                           | WOE                           | WOE编码                                                      | Table                                             | 转换后的Table                                                |          |                                              |
| Feature Transform             | Feature Transform             | 特征转换编码                                                 | Table                                             | 转换后的Table                                                |          |                                              |
| Sample Filter                 | Sample Filter                 | 样本过滤，根据条件筛选样本                                   | Table                                             |                                                              |          |                                              |
| OneHot                        | OneHot                        | 将一列转换为One-Hot格式。                                    | Table                                             | 转换了带有新列名的Table                                      |          |                                              |
| Feature standardized          | Feature standardized          | 特征归一化和标准化。                                         | Table，其值为instance                             | 转换后的Table                                                |          |                                              |
| Feature Soften                | Feature Soften                | 特征异常平滑                                                 | Table，其值为instance                             | 转换后的Table                                                |          |                                              |
| Vertical Statistic            | Vertical Statistic            | 计算样本统计量，均值，最大值，最小值，方差，标准差，缺失量   | Table，其值为instance                             |                                                              |          | Statistic Result                             |
| Horizontal Statistic          | Horizontal Statistic          | 计算横向场景的统计量                                         | Table，其值为instance                             |                                                              |          | Statistic Result                             |
| Mix Statistic                 | Mix Statistic                 | 计算混合场景的统计量                                         | Table，其值为instance                             |                                                              |          | Statistic Result                             |
| Feature Filling Missing Value | Feature Filling Missing Value | 使用最大值、最小值、均值、自定义值填充缺失值                 | Table，其值为instance                             | 转换后的Table                                                |          |                                              |
| PCA                           | PCA                           | 主成分分析                                                   | Table，其值为instance                             |                                                              |          |                                              |
| K Means                       | 构建K均值模块                   | 构建K均值模块                                                | Table，其值为instance                             |                                                              |          |                                              |
| Vertical Logistic Regression   | VertLR                        | 多方共同完成纵向逻辑回归建模                           | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | LR模型                                   |
| Horizontal Logistic Regression | HorzLR                        | 多方共同完成横向逻辑回归建模                           | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | LR模型                                   |
| Mixture Logistic Regression    | MixLR                         | 多方共同完成混合逻辑回归建模                           | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | LR模型                                   |
| Vertical SecureBoost           | VertSecureBoost               | 多方共同完成纵向xgboost建模                           | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | Xgboost模型                                   |
| Horizontal SecureBoost         | HorzSecureBoost               | 多方共同完成横向xgboost建模                           | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | Xgboost模型                                   |
| Mixture SecureBoost            | MixSecureBoost                | 多方共同完成混合xgboost建模                           | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | Xgboost模型                                   |
| Vertical neural network        | VertNN                        | 多方共同完成纵向神经网络建模                           | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | 神经网络模型                                   |
| Horizontal neural network      | HorzNN                        | 多方共同完成横向神经网络建模                           | Table，数据切割后的结果                               | talbe，模型预测的结果 |          | 神经网络模型                                   |
| evaluation                     | evaluation                    | 模型评估组件                                         | Table，算法建模后的结果                               | talbe，模型评估的结果 |          |                                   |


**examples**

example 模块提供了算法组件的本地测试事例。

对于建模的算法组件 LR、XGB 等，提供了传统的本地训练方式运行脚本以供比较。

# 模块的运行与测试

**配置介绍**

config.yaml 为全局配置文件；组件目录下的 xxx.yaml 文件是组件的配置文件，皆可使用默认参数。

默认使用本地数据库 LMDB、SQLite，数据库数据保存在 [Root Dir]/data 目录下。

组件的结果保存在 SQLite 的 `wefe_board.task_result` 表中。

**运行测试**

算法组件运行依赖于项目启动环境，环境的搭建详见[flow](/system_framework/flow)。

运行 ./demo/upload/upload-headler.py 文件，上传 upload_config.yaml 中声明的各方数据。

数据集从 ./data 目录读取。

./demo 目录下提供了已有的算法组件的测试运行脚本。

**案例**

运行纵向 LR 算法组件任务，所需配置文件和运行脚本在 ./demo/vert_lr 目录下。

```
# 二分类 LR 的数据集及算法参数的配置在 binary_config.yaml 配置
python wefe-vert-lr-binary.py # 联邦学习训练的二分类 LR
python sklearn-lr-binary.py # 本地训练的二分类 LR

# 二分类 LR 的数据集及算法参数的配置在 multi_config.yaml 配置
python wefe-vert-lr-multi.py # 联邦学习训练的二分类 LR
python sklearn-lr-multi.py # 本地训练的二分类 LR
```

**metrics**

评估指标公共方法。

**model_selection**

定义模型的验证与选择的相关算法。

**optimizer**

定义损失函数及梯度下降相关优化算法。

**protobuf**

协议文件，包括数据传输交互的协议及部分存储结构的协议。

**security**

安全算法及协议，包括同态加密、DH 密钥交换、SPDZ、密文压缩等。

**tracker**

提供数据库相关操作。

**transfer**

提供横纵向传输基础框架及协议。

**utils**

常用的工具类的方法，包括文件、数据、基础算子、随机数生成等。