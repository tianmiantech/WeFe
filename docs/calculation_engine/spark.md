# Spark

WeFe 内置的 Spark 版本为 `spark-3.0.1-bin-hadoop2.7`，且仅支持单机，暂不支持集群环境的 Spark 服务，后续我们会增加集群支持。

## 指定计算引擎为 Spark

在部署的配置文件 `wefe.cfg` 编辑指定配置项 `CALCULATION_ENGINE=SPARK`，然后启动服务。

>  Tips：各服务的启动与重启请查看 [安装指南](/install/install) 章节

## 相关配置项

```shell
# 若服务器内存 ≥ 32，可优化为如下配置，否则保持默认配置即可
SPARK_DRIVER_MEMORY=15g
SPARK_DRIVER_MAXRESULTSIZE=2g
SPARK_NUM_EXECUTORS=6
SPARK_EXECUTOR_MEMORY=2g
SPARK_EXECUTOR_CORES=1
SPARK_NUM_SLICES=32
```