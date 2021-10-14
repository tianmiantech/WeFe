# 总览

为帮助大家理解 WeFe 的系统架构设计，我们从宏观到微观，以不同的视角观察 WeFe 联邦学习建模平台的架构设计。


# 联邦结构

成员间通过 union 互相发现并产生交流，所以联邦是基于 union 服务形成的。

> union：联邦信息登记服务，使用区块链作为底层存储，是各成员在发起建模前互相了解的媒介。
> 
<img src="http://assets.processon.com/chart_image/615fbf34e401fd06aaa68ab2.png" style="max-height:700px;" />



# 成员系统架构

为充分满足系统的可扩展性、可维护性和可用性，WeFe 采取了多模块化设计。

各模块职责如下：

* board：WeFe 系统的可视化操控支持服务，用于维护资源、查看状态、生成建模任务等操作。
* flow：建模任务的调度服务，负责任务的启停、任务排队、状态更新等。
* kernel：建模算法组件包，由 flow 服务调起，目前可以在 spark 和 函数计算 两个计算环境中执行。
* gateway：网关，是各成员间交互的唯一通信出入口，包含身份验证、防篡改检查、防重播、黑名单等功能。

<img src="http://assets.processon.com/chart_image/607d4e305653bb2e1c7756a7.png" style="max-height:700px;" />

<br>

WeFe 中另有两个特殊的服务，这两个服务没有依赖模块，可以单独部署，独立运行。

* serving：模型在线预测服务，包含模型管理、预测、账务记录与统计等功能。
* fusion：一个轻量高效的样本对齐工具。

# 建模时多成员结构

联邦学习中，多个成员之间的价值与地位是相同的。

由于建模过程中成员间需要互相观察对方的状态，请确保各成员之间 gateway 服务能互通有无，否则无法进行建模活动。

<img src="http://assets.processon.com/chart_image/615fe2860e3e747620f540e9.png" style="max-height:700px;" />

> 各成员间在执行不同类型的联邦建模任务时，其结构稍有差异，更多内容请查看 [联邦学习](federated_learning/federated_learning) 相关章节。