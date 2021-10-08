# 贡献代码

WeFe 秉承开放、平等、协作、快速、分享的互联网精神，欢迎大家积极参与，共同成长。

我们保证尊重所有参与贡献的人，不限于提出问题、文档和代码贡献、解决bug以及其它贡献。

# 代码规范

WeFe 中包含多种语言的代码，但目前仅针对 Python 和 Java 有规范声明。


### python 代码规范

Python 代码规范：[Google Python Style Guide](https://google.github.io/styleguide/pyguide.html)


    ⚠️ 注意
    WeFe 开发人员主要使用 IDEA/PyCharm 进行开发，代码格式与 import 整理以编辑器默认风格为准。
    当发现 python 代码规范与现存代码风格有不一致时，以现存代码风格为准。


### java 代码规范

Java 代码规范：[《阿里巴巴 Java 开发手册》](https://github.com/alibaba/p3c)

Java 代码规范化插件：[IDEA 插件 alibaba-java-coding-guidelines](https://plugins.jetbrains.com/plugin/10046-alibaba-java-coding-guidelines)

    ⚠️ 注意
    WeFe 开发人员主要使用 IDEA 进行开发，代码格式与 import 整理以编辑器默认风格为准。


# 本地开发

1. fork 仓库，并拉取最新 `main` 分支。
2. 基于 main 分支创建自己的分支后进行开发。
3. 开发完成后拉取 `main` 分支最新代码 merge 到自己的分支，检查是否有冲突。
4. 创建 pull request 到 WeFe 仓库。

pull request 应当遵守以下规范：

* 一需求一合并：不要在一个 PR 中包含多个需求，尽量一个 PR 解决一个问题。
* 最小范围原则：不做无关的代码优化与重构，如有需要，请另开 PR。

# 合并代码

当 WeFe 仓库收到 pull request 请求后，天冕科技会安排人员对代码进行审核并测试，最终由仓库管理员将提交的代码合并到主分支。