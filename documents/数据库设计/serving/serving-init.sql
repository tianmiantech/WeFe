-- 用户账号表

CREATE TABLE `account`
(
    `id`                    varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`            varchar(32)           DEFAULT NULL COMMENT '创建人',
    `created_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`            varchar(32)           DEFAULT NULL COMMENT '更新人',
    `updated_time`          datetime              DEFAULT NULL COMMENT '更新时间',
    `cancelled`             tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已注销',
    `last_action_time`      datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP (6) COMMENT '最后活动时间',
    `history_password_list` text COMMENT '历史曾用密码',
    `phone_number`          varchar(200)          DEFAULT NULL,
    `password`              varchar(128) NOT NULL COMMENT '密码',
    `salt`                  varchar(128) NOT NULL COMMENT '盐',
    `nickname`              varchar(32)  NOT NULL COMMENT '昵称',
    `email`                 varchar(128) NOT NULL COMMENT '邮箱',
    `super_admin_role`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是超级管理员 超级管理员通常是第一个创建并初始化系统的那个人',
    `admin_role`            tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是管理员 管理员有更多权限，比如设置 menber 是否对外可见。',
    `audit_status`          varchar(32)  NOT NULL DEFAULT '' COMMENT '审核状态',
    `audit_comment`         varchar(512)          DEFAULT NULL COMMENT '审核意见',
    `enable`                tinyint(1) NOT NULL COMMENT '是否可用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_unique_phonenumber` (`phone_number`),
    KEY                     `idx_create_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号 ';

-- 全局设置表

CREATE TABLE `global_setting`
(
    `id`              varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`      varchar(32)           DEFAULT NULL COMMENT '创建人',
    `created_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`      varchar(32)           DEFAULT NULL COMMENT '更新人',
    `updated_time`    datetime              DEFAULT NULL COMMENT '更新时间',
    `member_id`       varchar(32)  NOT NULL COMMENT '联邦成员 Id 全局唯一，默认为uuid。',
    `member_name`     varchar(128) NOT NULL COMMENT '联邦成员名称',
    `rsa_private_key` text         NOT NULL COMMENT '私钥',
    `rsa_public_key`  text         NOT NULL COMMENT '公钥',
    `gateway_uri`     varchar(512)          DEFAULT NULL COMMENT '网关通信地址',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局设置 全局设置，这个表永远有且只有一条数据。';


-- 成员配置信息表

CREATE TABLE `member`
(
    `id`           varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `member_id`    varchar(256) NOT NULL COMMENT '成员id',
    `name`         varchar(64)  NOT NULL COMMENT '成员名称',
    `api`          varchar(256) DEFAULT NULL COMMENT '调用路警',
    `public_key`   text         NOT NULL COMMENT '公钥',
    `created_time` datetime     NOT NULL COMMENT '创建日期',
    `updated_time` datetime     DEFAULT NULL COMMENT '最后操作日期',
    `created_by`   varchar(32)  DEFAULT NULL COMMENT '创建人',
    `updated_by`   varchar(32)  DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员配置信息表';


-- 模型表

CREATE TABLE `model`
(
    `id`             varchar(32)  NOT NULL,
    `model_id`       varchar(256) NOT NULL COMMENT '模型id',
    `algorithm`      varchar(64)  NOT NULL COMMENT '算法',
    `fl_type`        varchar(64)  NOT NULL COMMENT '联邦学习类型',
    `feature_source` varchar(64)  NOT NULL COMMENT '特征获取方法',
    `model_param`    mediumtext   NOT NULL COMMENT '模型参数',
    `created_time`   datetime     NOT NULL COMMENT '创建时间',
    `updated_time`   datetime    DEFAULT NULL COMMENT '最后更新时间',
    `created_by`     varchar(32) DEFAULT NULL COMMENT '创建人',
    `updated_by`     varchar(32) DEFAULT NULL COMMENT '更新人',
    `enable`         tinyint(1) NOT NULL DEFAULT '0' COMMENT 'true-在线 false-下线',
    `name`           varchar(256) COMMENT '模型名称',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型表';

-- 参与模型成员表

CREATE TABLE `model_member`
(
    `id`           varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `model_id`     varchar(256) NOT NULL COMMENT '模型id',
    `member_id`    varchar(256) DEFAULT NULL COMMENT '成员id',
    `role`         varchar(64)  DEFAULT NULL COMMENT '角色',
    `created_time` datetime     NOT NULL COMMENT '创建时间',
    `updated_time` datetime     DEFAULT NULL COMMENT '最后更新时间',
    `created_by`   varchar(32)  DEFAULT NULL COMMENT '创建人',
    `updated_by`   varchar(32)  DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练模型成员表';

-- 模型sql配置表

CREATE TABLE `model_sql_config`
(
    `id`           varchar(32) NOT NULL,
    `model_id`     varchar(256) DEFAULT NULL COMMENT '模型id',
    `type`         varchar(64)  DEFAULT NULL COMMENT 'db类型',
    `url`          varchar(255) DEFAULT NULL COMMENT '数据链接路径',
    `username`     varchar(255) DEFAULT NULL COMMENT '账号',
    `password`     varchar(255) DEFAULT NULL COMMENT '密码',
    `sql_context`  text COMMENT '执行SQL',
    `created_time` datetime    NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by`   varchar(32)  DEFAULT NULL COMMENT '创建人',
    `updated_by`   varchar(32)  DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT '模型SQL配置表';


-- 调用日志表

CREATE TABLE `predict_log`
(
    `id`           varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `seq_no`       varchar(64)  NOT NULL COMMENT '流水号',
    `member_id`    varchar(256) NOT NULL COMMENT '成员id',
    `model_id`     varchar(256) NOT NULL COMMENT '模型id',
    `algorithm`    varchar(64) DEFAULT NULL COMMENT '算法类型',
    `fl_type`      varchar(64) DEFAULT NULL COMMENT '训练类型 横向/纵向联邦',
    `my_role`      varchar(64) DEFAULT NULL COMMENT '我的训练角色',
    `created_time` timestamp    NOT NULL COMMENT '创建时间',
    `request`      text COMMENT '请求参数',
    `response`     text COMMENT '返回结果',
    `spend`        bigint(20) DEFAULT NULL COMMENT '调用耗时',
    `result`       tinyint(1) NOT NULL DEFAULT '0' COMMENT '调用结果：1成功，0失败',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调用日志表';


-- 调用统计表


CREATE TABLE `predict_statistics`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT '全局位置标识',
    `member_id`    varchar(256) NOT NULL DEFAULT '' COMMENT '成员id',
    `model_id`     varchar(256) NOT NULL COMMENT '模型id',
    `month`        varchar(20)  NOT NULL COMMENT '月份',
    `day`          varchar(20)  NOT NULL COMMENT '天',
    `hour`         varchar(20)  NOT NULL COMMENT '小时',
    `minute`       varchar(20)  NOT NULL COMMENT '分',
    `total`        bigint(20) NOT NULL DEFAULT '0' COMMENT '调用数量',
    `success`      bigint(20) NOT NULL DEFAULT '0' COMMENT '成功数量',
    `fail`         bigint(20) NOT NULL DEFAULT '0' COMMENT '失败数量',
    `created_time` datetime     NOT NULL COMMENT '创建时间',
    `updated_time` datetime              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY            `idx_member_id_month` (`member_id`,`month`),
    KEY            `idx_member_id_day` (`member_id`,`day`),
    KEY            `idx_member_id_hours` (`member_id`,`hour`),
    KEY            `idx_member_id_time` (`member_id`,`minute`),
    KEY            `idx_model_id_month` (`model_id`,`month`),
    KEY            `idx_model_id_day` (`model_id`,`day`),
    KEY            `idx_model_id_hours` (`model_id`,`hour`),
    KEY            `idx_model_id_time` (`model_id`,`minute`),
    KEY            `idx_day` (`day`),
    KEY            `idx_hours` (`hour`),
    KEY            `idx_time` (`minute`)
) ENGINE=InnoDB AUTO_INCREMENT=6895578 DEFAULT CHARSET=utf8mb4 COMMENT='调用统计表';


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
    name         VARCHAR(255) NOT NULL UNIQUE COMMENT '客户名称',
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
    id                  VARCHAR(32) NOT NULL COMMENT '',
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

CREATE TABLE `operator_log`
(
    `id`             varchar(32) NOT NULL COMMENT '操作日志编号',
    `log_interface`  varchar(50)   DEFAULT NULL COMMENT '请求接口',
    `interface_name` varchar(1024) DEFAULT NULL COMMENT '请求接口名称',
    `request_ip`     varchar(20)   DEFAULT NULL COMMENT '请求IP',
    `operator_id`    varchar(32)   DEFAULT NULL COMMENT '操作人员编号',
    `token`          varchar(100)  DEFAULT NULL COMMENT '请求token',
    `log_action`     varchar(50)   DEFAULT NULL COMMENT '操作行为',
    `result_code`    int(20) DEFAULT NULL COMMENT '请求结果code',
    `result_message` text COMMENT '请求结果消息',
    `request_time`   datetime(6) DEFAULT NULL COMMENT '请求时间',
    `spend`          int(11) DEFAULT NULL COMMENT '处理时长',
    `created_time`   datetime(6) DEFAULT NULL COMMENT '创建时间',
    `updated_time`   datetime(6) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY              `query_index` (`created_time`,`log_action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `verification_code`
(
    `id`            varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`    varchar(32)  DEFAULT NULL COMMENT '创建人',
    `created_time`  datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`    varchar(32)  DEFAULT NULL COMMENT '更新人',
    `updated_time`  datetime(6) DEFAULT NULL COMMENT '更新时间',
    `mobile`        varchar(200) DEFAULT NULL,
    `code`          varchar(30) NOT NULL COMMENT '验证码',
    `success`       varchar(10)  DEFAULT NULL COMMENT 'true：成功，false：失败',
    `send_channel`  varchar(10)  DEFAULT NULL COMMENT '发送渠道，sms：短信、email：邮件',
    `business_type` varchar(30)  DEFAULT NULL COMMENT '业务类型，memberRegister：成员注册、accountForgetPassword：账号忘记密码',
    `resp_content`  varchar(500) DEFAULT NULL COMMENT '响应内容',
    `biz_id`        varchar(64)  DEFAULT NULL COMMENT '业务ID',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码';



DROP TABLE IF EXISTS `global_config`;
CREATE TABLE `global_config`
(
    `id`           varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`   varchar(32) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`   varchar(32) DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
    `group`        varchar(32) DEFAULT NULL COMMENT '配置项所在的组',
    `name`         varchar(32) DEFAULT NULL COMMENT '配置项名称',
    `value`        text COMMENT '配置项的值',
    `comment`      text COMMENT '配置项的解释说明',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique_group_name` (`group`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局设置';