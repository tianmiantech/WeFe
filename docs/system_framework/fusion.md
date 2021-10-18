# fusion

fusion 是一套轻量级的数据融合系统，基于 [RSA 的 PSI 算法](https://encrypto.de/papers/KLSAP17.pdf) 提供了安全可靠轻量快速的数据融合服务。

数据融合，即样本对齐，一般为纵向联邦学习的初始步骤。拥有不同维度特征样本的合作方通过数据融合匹配样本，使样本的特征维度更加多元化。

# 项目特点

对采用 RSA-PSI 算法进行样本对齐融合；

对齐样本进行 RSA 算法保证数据安全；

数据样本预处理，生成布隆过滤器并持久化到硬盘中，可重复使用；

支持数据样本的特征按时间回溯；

具有可扩展性，支持后续拓展算法。

# 打包部署

fusion 包含一整套的前端与后台服务。

## fusion-service

fusion-service 基于 SpringBoot 框架，采用 Maven 进行包管理。

**环境依赖**

JDK 1.8、Maven、MySQL

**项目打包**

```shell
# 基于 Maven，请保证服务器上 Maven 环境正常
mvn clean install -Dmaven.test.skip=true -am -pl fusion/fusion-service 
```

**项目运行**

```shell
# 初始化数据库
mysql -uroot -p < fusion-init.sql

# 运行项目
java -jar fusion-service.jar
```

## fusion-website

fusion-website 提供了用户友好的可视化界面。

**运行编译**

```shell
# 直接运行
npm run dev

# 打包命令
npm run build -- dev
```

**项目配置**

配置见 proxy.js, 默认端口 5001, 可通过 port 字段重设, /api 为接口转发。

**项目访问**

`[ip:port]/fusion-website`

**Nginx 配置**

```js
// Nginx 参考配置
server {

  	listen  80;
  	server_name  127.0.0.1;

		root   /opt/website/html;

		client_max_body_size 100m;

    location /fusion-website/ {
       try_files $uri /fusion-website/index.html;
    }
    
    location /fusion-service/ {
    	add_header Access-Control-Allow-Origin *;
    	add_header Access-Control-Allow-Methods 'GET, POST, OPTIONS';
    	add_header Access-Control-Allow-Headers 'DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization';

    	if ($request_method = 'OPTIONS') {
    		return 204;
		}

		proxy_pass http://[ip:port];
		proxy_read_timeout 1800;
    }

}
```

**参考文档**

前端框架  [Element](https://element.eleme.cn/#/zh-CN)。

