# board-service

board-service 是 board-website 的后台服务，提供 HTTP API 供其调用。

## 项目的打包与启动

board-service 使用 java 开发，基于 spring-boot 框架，使用 maven 进行包管理。

board-service 的各种功能依赖 WeFe 中其他的服务，但就 board-service 本身的运行而言，只依赖 mysql 数据库，所以在启动前需要先准备好 mysql 服务。

这里只描述如何编译并启动 board-service 服务，完整的 WeFe 系统部署过程详见 [WeFe 部署文档](/install/install)。

**初始化数据库（建表）**

执行 `wefe_board.sql` 中的 sql 脚本。

**编译 / 打包**

基于 maven 进行打包（部署机器上需要有 maven 环境）。

```bash
mvn clean install -Dmaven.test.skip=true -am -pl board/board-service
```

**修改配置文件**

board-service 的启动依赖 `config.properties` 配置文件，需要在 jar 包同目录放置该配置文件，程序会自动从 jar 包目录读取。

修改 mysql 相关配置项

```bash
db.mysql.url=jdbc:mysql://0.0.0.0:3306/wefe_board?serverTimezone=GMT%2B8
db.mysql.host=0.0.0.0
db.mysql.port=3306
db.mysql.database=wefe_board
db.mysql.username=wefe
db.mysql.password=password
```

修改文件上传相关配置项

```bash
# 文件上传相关功能会将文件统一上传到此目录
wefe.file.upload.dir=/data/wefe_file_upload_dir
```

**启动**

```bash
java -jar wefe-board-service.jar
```

**访问**

在浏览器中访问 [http://localhost:8080/board-service/apis](http://localhost:8080/board-service/apis) ，如果能看到系统 api 列表，即代表服务启动成功。