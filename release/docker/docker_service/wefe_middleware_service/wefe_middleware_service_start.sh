
#!/bin/bash

# 导入配置
source ../wefe.cfg

# mysql 镜像配置修改
sed -i "/mysql_port/s/-.*:/- $MYSQL_PORT:/g" ./resources/docker-compose.yml
sed -i "/MYSQL_USER/s/:.*/: \"$MYSQL_USERNAME\"/g" ./resources/docker-compose.yml
sed -i "/MYSQL_PASSWORD/s/:.*/: \"$MYSQL_PASSWORD\"/g" ./resources/docker-compose.yml
sed -i "/MYSQL_DATABASE/s/:.*/: \"$MYSQL_DATABASE\"/g" ./resources/docker-compose.yml

# clickhouse 镜像配置修改
sed -i "/clickhouse_port/s/-.*:/- $CLICKHOUSE_PORT:/g" ./resources/docker-compose.yml
sed -i "/clickhouse_tcp_port/s/-.*:/- $CLICKHOUSE_TCP_PORT:/g" ./resources/docker-compose.yml

sed -i "/ck_username_start/s/\<.*\!/$CLICKHOUSE_USERNAME\> \<\!/g" ./resources/mount/clickhouse/users.xml
sed -i "/ck_username_end/s/\<.*\!/$CLICKHOUSE_USERNAME\> \<\!/g" ./resources/mount/clickhouse/users.xml
sed -i "/ck_password/s/\>.*\!/>$CLICKHOUSE_PASSWORD\<\/password\> \<\!/g" ./resources/mount/clickhouse/users.xml

# docker-compose 配置修改
sed -i "/clickhouse_data/s@-.*:@- \"$DATA_PATH/clickhouse:@g" ./resources/docker-compose.yml
sed -i "/mysql_data/s@-.*:@- '$DATA_PATH/mysql:@g" ./resources/docker-compose.yml

docker-compose -p $WEFE_ENV -f resources/docker-compose.yml up -d
