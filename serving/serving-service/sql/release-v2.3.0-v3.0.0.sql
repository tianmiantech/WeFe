-- 服务表
DROP TABLE IF EXISTS service;
CREATE TABLE `service`
(
    `id`             varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`     varchar(32)           DEFAULT NULL COMMENT '创建人',
    `created_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`     varchar(32)           DEFAULT NULL COMMENT '更新人',
    `updated_time`   datetime              DEFAULT NULL COMMENT '更新时间',
    `name`           varchar(32)  NOT NULL COMMENT '服务名',
    `url`            varchar(128) NOT NULL COMMENT '服务地址',
    `service_type`   tinyint(2) NOT NULL COMMENT '服务类型  1匿踪查询，2交集查询，3安全聚合',
    `query_params`   text COMMENT '查询参数配置',
    `data_source`    text COMMENT 'SQL配置',
    `service_config` text COMMENT '服务配置',
    `status`         tinyint(2) DEFAULT '0' COMMENT '是否在线 1在线，0离线',
    `ids_table_name` varchar(100)          DEFAULT NULL COMMENT '主键对应的表名',
    `operator`       varchar(10)           DEFAULT NULL COMMENT '操作 sum / avg',
    PRIMARY KEY (`id`),
    UNIQUE KEY `url_unique` (`url`),
    KEY              `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务';

-- 数据源
DROP TABLE IF EXISTS data_source;
CREATE TABLE `data_source`
(
    `id`            varchar(32) NOT NULL COMMENT '全局唯一标识',
    `database_type` varchar(255) DEFAULT NULL COMMENT '数据源类型',
    `host`          varchar(255) DEFAULT NULL COMMENT '数据源主机',
    `port`          int(255) DEFAULT NULL COMMENT '数据源端口',
    `database_name` varchar(255) DEFAULT NULL COMMENT '数据库名',
    `user_name`     varchar(255) DEFAULT NULL COMMENT '用户名',
    `password`      varchar(255) DEFAULT NULL COMMENT '用户密码',
    `created_by`    varchar(255) DEFAULT NULL COMMENT '创建人',
    `created_time`  datetime     DEFAULT NULL COMMENT '创建时间',
    `updated_time`  datetime     DEFAULT NULL COMMENT '更新时间',
    `name`          varchar(255) DEFAULT NULL COMMENT '数据源名称',
    `updated_by`    varchar(255) DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- 客户表
DROP TABLE IF EXISTS client;
CREATE TABLE client
(
    id           VARCHAR(32)  NOT NULL COMMENT '客户id',
    name         VARCHAR(255) NOT NULL COMMENT '客户名称',
    created_by   varchar(32)           DEFAULT NULL COMMENT '创建人',
    created_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by   varchar(32)           DEFAULT NULL COMMENT '更新人',
    updated_time datetime              DEFAULT NULL COMMENT '更新时间',
    email        VARCHAR(255) COMMENT '邮箱',
    ip_add       VARCHAR(255) NOT NULL COMMENT 'ip地址',
    pub_key      text         NOT NULL COMMENT '公钥',
    code         VARCHAR(255) NOT NULL UNIQUE COMMENT '客户 code',
    remark       text COMMENT '备注',
    status       INT          NOT NULL DEFAULT 1 COMMENT '客户状态;1正常、0删除',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '客户基本信息表';


-- 客户-服务表
DROP TABLE IF EXISTS client_service;
CREATE TABLE client_service
(
    id           VARCHAR(32)  NOT NULL COMMENT '客户服务id',
    service_id   VARCHAR(32)  NOT NULL COMMENT '服务id',
    client_id    VARCHAR(32)  NOT NULL COMMENT '客户id',
    client_name  VARCHAR(255) COMMENT '客户名称',
    service_name VARCHAR(255) COMMENT '服务名称',
    service_type tinyint(2) COMMENT '服务类型',
    unit_price   double       NOT NULL COMMENT '调用单价',
    url          varchar(128) NOT NULL COMMENT '服务地址',
    pay_type     tinyint(1) NOT NULL COMMENT '付费类型: 1 预付费、0 后付费',
    ip_add       VARCHAR(255) NOT NULL COMMENT 'ip地址',
    created_by   varchar(32)           DEFAULT NULL COMMENT '创建人',
    created_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by   varchar(32)           DEFAULT NULL COMMENT '更新人',
    updated_time datetime              DEFAULT NULL COMMENT '更新时间',
    status       TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT = '客户服务表';
CREATE UNIQUE INDEX service_client_index ON client_service (id, service_id, client_id);

-- 计费规则配置表
DROP TABLE IF EXISTS fee_config;
CREATE TABLE fee_config
(
    id           VARCHAR(32) NOT NULL COMMENT '全局唯一标识',
    service_id   VARCHAR(32) COMMENT '服务id',
    client_id    VARCHAR(32) COMMENT '客户id',
    created_by   varchar(32)          DEFAULT NULL COMMENT '创建人',
    created_time datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by   varchar(32)          DEFAULT NULL COMMENT '更新人',
    updated_time datetime             DEFAULT NULL COMMENT '更新时间',
    unit_price   double      NOT NULL COMMENT '调用单价',
    pay_type     TINYINT(1) NOT NULL COMMENT '付费类型: 1 预付费、0 后付费',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '计费配置';

-- API 调用统计表
DROP TABLE IF EXISTS api_request_record;
CREATE TABLE api_request_record
(
    id             VARCHAR(32)  NOT NULL COMMENT '租户号',
    service_id     VARCHAR(32)  NOT NULL COMMENT '服务id',
    client_id      VARCHAR(32)  NOT NULL COMMENT '客户id',
    client_name    VARCHAR(32)  NOT NULL COMMENT '客户名称',
    service_name   VARCHAR(32)  NOT NULL COMMENT '服务名称',
    service_type   tinyint(2) NOT NULL COMMENT '服务类型  1匿踪查询，2交集查询，3安全聚合',
    ip_add         VARCHAR(255) NOT NULL COMMENT '请求ip地址',
    spend          BIGINT       NOT NULL COMMENT '耗时',
    request_result INT          NOT NULL COMMENT '请求结果',
    created_by     varchar(32)           DEFAULT NULL COMMENT '创建人',
    created_time   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by     varchar(32)           DEFAULT NULL COMMENT '更新人',
    updated_time   datetime              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = 'API 调用记录';
CREATE UNIQUE INDEX service_client_index ON api_request_record (service_id, client_id, id);

-- 计费详情表
DROP TABLE IF EXISTS fee_detail;
CREATE TABLE fee_detail
(
    id                  VARCHAR(32) NOT NULL COMMENT '全局唯一标识',
    service_id          VARCHAR(32) NOT NULL COMMENT '服务id',
    client_id           VARCHAR(32) NOT NULL COMMENT '客户id',
    fee_config_id       varchar(32) NOT NULL COMMENT '计费规则id',
    total_fee           DECIMAL(24, 6) COMMENT '总费用',
    unit_price          DECIMAL(24, 6) COMMENT '单价(￥)',
    pay_type            TINYINT(1) NOT NULL COMMENT '付费类型: 1 预付费、0 后付费',
    total_request_times INT         NOT NULL DEFAULT 0 COMMENT '总调用次数',
    created_by          varchar(32)          DEFAULT NULL COMMENT '创建人',
    created_time        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by          varchar(32)          DEFAULT NULL COMMENT '更新人',
    updated_time        datetime             DEFAULT NULL COMMENT '更新时间',
    service_name        varchar(32)          DEFAULT NULL COMMENT '服务名称',
    client_name         varchar(32)          DEFAULT NULL COMMENT '客户名称',
    service_type        INT COMMENT '服务类型',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '结算详情表';
CREATE UNIQUE INDEX fee_detail_index ON fee_detail (id, service_id, client_id);

-- 收支记录表
DROP TABLE IF EXISTS payments_records;
CREATE TABLE payments_records
(
    id           VARCHAR(255) NOT NULL COMMENT '全局唯一标识',
    created_by   varchar(32)           DEFAULT NULL COMMENT '创建人',
    created_time datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by   varchar(32)           DEFAULT NULL COMMENT '更新人',
    updated_time datetime              DEFAULT NULL COMMENT '更新时间',
    pay_type     INT COMMENT '收支类型，1充值 2 支出',
    client_id    VARCHAR(255) COMMENT '客户id',
    client_name  VARCHAR(255) COMMENT '客户名称',
    service_id   VARCHAR(255) COMMENT '服务id',
    service_name VARCHAR(255) COMMENT '服务名称',
    service_type INT COMMENT '服务类型',
    amount       DECIMAL(24, 6) COMMENT '金额',
    balance      DECIMAL(24, 6) COMMENT '余额',
    remark       VARCHAR(900) COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '收支记录';
CREATE UNIQUE INDEX payments_records_index ON payments_records (id, service_id, client_id);


alter table `account`
    add column `audit_status` varchar(32) NOT NULL DEFAULT '' COMMENT '审核状态';
alter table `account`
    add column `audit_comment` varchar(512) DEFAULT NULL COMMENT '审核意见';
alter table `account`
    add column `enable` tinyint(1) NOT NULL COMMENT '是否可用';
update `account`
set enable = 1;
update `account`
set audit_status = 'agree';
