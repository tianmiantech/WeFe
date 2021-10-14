kernel 是算法组件的核心模块。

# 模块结构

**base**

一些基础类的定义，包括模型参数、数据结构、基础运算等。

**components**

算法组件的具体实现，包括数据集加载、样本对齐、特征工程、lr、xgboost、神经网络等 。

组件列表参考文档：[组件列表文档](./components)。

算法组件开发参考文档：[components/README_DOCUMENT.md](./components/README_DOCUMENT.md)。

**examples**

提供可直接运行的算法组件案例，具体运行方式参考[案例运行文档](./examples/README.md)。

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