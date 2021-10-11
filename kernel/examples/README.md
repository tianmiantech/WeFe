example 模块提供了算法组件的本地测试事例。

对于建模的算法组件 LR、XGB 等，提供了传统的本地训练方式运行脚本以供比较。

# 模块的运行与测试

**配置介绍**

config.yaml 为全局配置文件；组件目录下的 xxx.yaml 文件是组件的配置文件，皆可使用默认参数。

默认使用本地数据库 LMDB、SQLite，数据库数据保存在 [Root Dir]/data 目录下。

组件的结果保存在 SQLite 的 `wefe_board.task_result` 表中。

**运行测试**

算法组件运行依赖于项目启动环境，环境的搭建详见[项目运行启动文档](../../flow/README.md)。

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