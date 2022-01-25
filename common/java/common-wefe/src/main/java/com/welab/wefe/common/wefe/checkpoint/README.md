# 服务可用性检查

为了快速确认服务已部署成功并且可用，我们对几乎所有服务添加了用于可用性检查的接口。

这个接口大多数情况下名为 `server/available`，这个接口中会对预设的所有检查点进行检查。

为了避免在可用性检查过程中，过长的耗时导致接口超时而前端无法正常展示检查结果，
我们使用 `java.util.concurrent.Future` 对 `check` 动作进行了封装，使得单个检查点 `check` 行为默认超时时间 5 秒。
再加上检查点列表是被并行执行的，这样一来，前端在大约 5 秒内一定可以展示可用性接口对所有检查点的检查结果。

## 创建检查点

只要在任意一个地方创建一个类，并且这个类继承 `AbstractCheckpoint`，这便意味着添加了一个检查点，
这个检查点会在程序启动时被扫描到，并在调用 `CheckpointManager.checkAll()` 时被执行。