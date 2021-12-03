![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

```text
 
 ___       __   _______   ________ _______      
|\  \     |\  \|\  ___ \ |\  _____\\  ___ \     
\ \  \    \ \  \ \   __/|\ \  \__/\ \   __/|    
 \ \  \  __\ \  \ \  \_|/_\ \   __\\ \  \_|/__  
  \ \  \|\__\_\  \ \  \_|\ \ \  \_| \ \  \_|\ \ 
   \ \____________\ \_______\ \__\   \ \_______\
    \|____________|\|_______|\|__|    \|_______|


```

WeFe ( WeLab Federated Learning ) 是 Welab 汇立集团子公司[天冕](https://www.tianmiantech.com)科技发起的开源项目，为联邦学习生态系统提供了一套好用的可靠的安全计算框架。

`Documentation：` https://tianmiantech.github.io/WeFe/

# 项目特点

混合联邦，纵向联邦学习与横向联邦学习结合的行业解决方案；

使用联盟链作为联邦中心存证共享方案；

支持流程可视化，托拉拽编辑流程的交互形式；

基于函数计算与云存储对象实现动态资源拓展方案；

支持 GPU 加速同态加密运算（实验室）。


# 联邦学习算法

WeFe 目前支持的联邦学习算法：横向联邦、纵向联邦、混合联邦、深度学习。

基于FATE，改进并新增了相关算法，算法细节请参考 Kernel 模块文档 [kernel/README.md](./kernel)。

# 安装使用

WeFe 支持 Linux 操作系统，推荐使用 Centos 7+ 的版本。

WeFe 提供了一套基于 Docker 的便捷部署方式。

WeFe 提供了一套完整的在线体验环境 Demo ENV。

## Docker 部署

单机部署，详见 [release/docker/README.md](./release/docker)

## 在线体验

WeFe 不仅支持本地部署运行测试，并且提供了一套完整的线上体验环境；

用户可以通过线上体验环境，模拟联邦中三位成员间的建模操作；

体验环境的联邦成员角色有 DemoMember1、DemoMember2、DemoMember3；

详情访问[在线体验平台](https://tianmiantech.com/federal)体验。

# 系统架构

WeFe 系统由两大模块 union 与 member 组成；

member 是 WeFe 联邦学习平台中进行联邦建模的最小成员单位；

union 是一个去中心化的公共服务平台，存储了联邦中的可公开信息并提供给联邦中的 member 访问。

模块详情：

​	union 模块，详见 [union/README.md](./union)；

​	member 模块，详见 [README_MEMBER.md](./README_MEMBER.md)。

# 发行版本

2021-09-23：v.2.2

# 成为项目贡献者

项目的构建与编写在一定程度上遵循以下代码规范

Python 代码规范：[Google Python Style Guide](https://google.github.io/styleguide/pyguide.html)

Java 代码规范：[《阿里巴巴 Java 开发手册》](https://github.com/alibaba/p3c)

Java 代码规范化插件：[IDEA 插件 alibaba-java-coding-guidelines](https://plugins.jetbrains.com/plugin/10046-alibaba-java-coding-guidelines)

# 版权

Apache 2.0

# 联系我们

欢迎扫码添加 WeFe 小助手（微信号：tianmiantech001）；

<div align=left>
  <img src="./images/WeFeWechatQR03.png" style="zoom:50%;" />
</div>

添加后滴滴小助手，小助手会拉你进 WeFe 技术交流群哦~；

欢迎关注天冕科技公众号
<div align=left>
  <img src="./images/TianmianOfficialAccount.jpg" style="zoom:50%;" />
</div>
快来寻找志同道合的伙伴吧！