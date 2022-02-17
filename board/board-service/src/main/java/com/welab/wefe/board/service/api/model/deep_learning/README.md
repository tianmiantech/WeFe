# 深度学习模型在线推理
这里的 api 提供的是非生产环境的在线推理功能，如果需要将模型部署到生产环境，请将模型下载后在 serving 服务中导入，serving 服务是一个专门针对生产环境设计的应用。

推理流程：
1. 上传文件：file/upload
2. 开始预测：/model/deep_learning/call/start
3. 获取预测结果：flow/job/task/detail
4. 下载原始图片：