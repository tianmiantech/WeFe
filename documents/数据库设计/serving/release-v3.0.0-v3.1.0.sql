-- 服务调用日志表
DROP TABLE IF EXISTS service_call_log;
CREATE TABLE service_call_log
(
    id                    VARCHAR(32)  NOT NULL COMMENT '序号id',
    order_id              VARCHAR(255) NOT NULL COMMENT '订单号',
    call_by_me            BOOL         NOT NULL DEFAULT 0 COMMENT '是否是我方发起的请求',
    request_partner_id    VARCHAR(255) NOT NULL COMMENT '请求方id',
    request_partner_name  VARCHAR(32)  NOT NULL COMMENT '请求方名称',
    response_partner_id   VARCHAR(255) NOT NULL COMMENT '响应方id',
    response_partner_name VARCHAR(32)  NOT NULL COMMENT '响应方名称',
    service_id            VARCHAR(255) NOT NULL COMMENT '服务id',
    service_name          VARCHAR(255) NOT NULL COMMENT '服务名称',
    service_type          tinyint(2) NOT NULL COMMENT '服务类型',
    request_id            VARCHAR(255) NOT NULL COMMENT '请求id',
    response_id           VARCHAR(255) COMMENT '相应id',
    request_data          TEXT         NOT NULL COMMENT '请求内容',
    response_data         TEXT COMMENT '响应内容',
    response_code         INT                   DEFAULT 0 COMMENT '响应码',
    response_status       VARCHAR(255) COMMENT '响应状态',
    request_ip            VARCHAR(255) NOT NULL COMMENT '请求ip',
    spend_time            INT COMMENT '响应时间',
    created_by            VARCHAR(32) COMMENT '创建人',
    created_time          DATETIME COMMENT '创建时间',
    updated_by            VARCHAR(32) COMMENT '更新人',
    updated_time          DATETIME COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '服务调用日志表';
CREATE INDEX service_order_index ON service_call_log (service_id, order_id);
CREATE INDEX req_resp_index ON service_call_log (request_id, response_id);
CREATE INDEX req_resp_partner ON service_call_log (request_partner_id, response_partner_id);

-- 订单表
DROP TABLE IF EXISTS service_order;
CREATE TABLE service_order
(
    id                    VARCHAR(32) NOT NULL COMMENT '订单号',
    service_id            VARCHAR(255) COMMENT '服务id',
    service_name          VARCHAR(255) COMMENT '服务名称',
    service_type          tinyint(2) COMMENT '服务类型',
    order_type            BOOL        NOT NULL DEFAULT 1 COMMENT '是否为己方生成的订单;1 是, 0否',
    status                VARCHAR(32) NOT NULL COMMENT '订单状态;成功、失败、进行中',
    request_partner_id    VARCHAR(32) NOT NULL COMMENT '请求方id',
    request_partner_name  VARCHAR(32) COMMENT '请求方名称',
    response_partner_id   VARCHAR(32) COMMENT '响应方id',
    response_partner_name VARCHAR(32) COMMENT '响应方名称',
    created_by            VARCHAR(32) COMMENT '创建人',
    created_time          DATETIME COMMENT '创建时间',
    updated_by            VARCHAR(32) COMMENT '更新人',
    updated_time          DATETIME COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '订单表';
CREATE INDEX service_req_resp_index ON service_order (service_id, request_partner_id, response_partner_id);

-- 订单统计表
DROP TABLE IF EXISTS order_statistics;
CREATE TABLE order_statistics
(
    id                    VARCHAR(32)  NOT NULL COMMENT '序号',
    call_times            INT COMMENT '调用次数',
    success_times         INT COMMENT '成功次数',
    failed_times          INT COMMENT '失败次数',
    minute                VARCHAR(255) COMMENT '每分钟统计',
    hour                  VARCHAR(255) COMMENT '每小时统计',
    day                   VARCHAR(255) COMMENT '每天统计',
    month                 VARCHAR(255) COMMENT '每月统计',
    service_id            VARCHAR(255) NOT NULL COMMENT '服务id',
    service_name          VARCHAR(255) NOT NULL COMMENT '服务名称',
    request_partner_id    VARCHAR(32)  NOT NULL COMMENT '请求方id',
    request_partner_name  VARCHAR(32)  NOT NULL COMMENT '请求方名称',
    response_partner_id   VARCHAR(32)  NOT NULL COMMENT '响应方id',
    response_partner_name VARCHAR(32)  NOT NULL COMMENT '响应方名称',
    save_ip               VARCHAR(32)  COMMENT '统计方ip',
    created_by            VARCHAR(32) COMMENT '创建人',
    created_time          DATETIME COMMENT '创建时间',
    updated_by            VARCHAR(32) COMMENT '更新人',
    updated_time          DATETIME COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '订单统计表';
CREATE INDEX service_req_resp_index ON order_statistics (service_id, request_partner_id, response_partner_id);
CREATE INDEX minute_index ON order_statistics (minute);
CREATE INDEX hour_index ON order_statistics (hour);
CREATE INDEX day_index ON order_statistics (day);
CREATE INDEX month_index ON order_statistics (month);

-- 移除两个表： global_setting
-- 使用新的 global_config 表替代旧表
-- 全局配置表
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


insert into global_config(`id`, `group`, `name`, `comment`, `value`)
values (replace(uuid(), '-', ''), 'identity_info', 'member_id', '系统身份 Id 全局唯一，联邦模式为memberId,独立模式为uuid。',
        (select member_id from global_setting limit 1)),
(replace(uuid(),'-',''),'identity_info','member_name','系统名称',(select member_name from global_setting limit 1)),
(replace(uuid(),'-',''),'identity_info','rsa_private_key','私钥',(select rsa_private_key from global_setting limit 1)),
(replace(uuid(),'-',''),'identity_info','rsa_public_key','公钥',(select rsa_public_key from global_setting limit 1)),
(replace(uuid(),'-',''),'identity_info','serving_base_url','系统url地址',(select serving_base_url from global_setting limit 1)),
(replace(uuid(),'-',''),'identity_info','mode','模式 standalone-独立模式 union-联邦模式','union')

-- 合作者
-- https://www.tapd.cn/53885119/prong/stories/view/1153885119001085243
CREATE TABLE `partner`
(
    `id`               varchar(32) NOT NULL,
    `name`             varchar(64)  DEFAULT NULL COMMENT '合作者名称',
    `email`            varchar(255) DEFAULT NULL COMMENT '邮箱',
    `serving_base_url` varchar(255) DEFAULT NULL COMMENT 'Serving服务地址',
    `code`             varchar(255) DEFAULT '' COMMENT '客户 code',
    `remark`           text COMMENT '备注',
    `is_union_member`  tinyint(1) NOT NULL COMMENT '是否是联邦成员',
    `status`           int(11) NOT NULL DEFAULT '1' COMMENT '合作者状态;1正常、0删除',
    `created_time`     datetime    NOT NULL,
    `updated_time`     datetime     DEFAULT NULL,
    `created_by`       varchar(32)  DEFAULT NULL COMMENT '创建人',
    `updated_by`       varchar(32)  DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合作者';


-- https://www.tapd.cn/53885119/prong/stories/view/1153885119001085582    
alter table `client_service`
    add column `public_key` text COMMENT '调用者公钥' after `client_name`;
alter table `client_service`
    add column `private_key` text COMMENT '调用者私钥' after `public_key`;
alter table `client_service`
    add column `type` tinyint(1) COMMENT '服务类型 0开通，1激活 ' after `client_name`
alter table `client_service`
    add column `code` varchar(255) COMMENT '调用者code' after `private_key`;

ALTER TABLE model_member
    ADD `status` varchar(64) DEFAULT 'offline' NOT NULL COMMENT '成员模型状态 offline-成员失联 unavailable-模型不可用 available-模型可用';


alter table client_service modify column `service_id` varchar (256) NOT NULL DEFAULT '' COMMENT '服务id';
alter table client_service modify column `url` varchar (128) DEFAULT '' COMMENT '服务地址';
alter table client_service modify column `ip_add` varchar (100) DEFAULT NULL COMMENT 'IP 白名单';
alter table client_service modify column `unit_price` double (20,4) DEFAULT NULL COMMENT '单价';
alter table client_service modify column `pay_type` tinyint(4) DEFAULT NULL COMMENT '付费类型';
alter table client_service modify column `service_type` tinyint(4) DEFAULT NULL COMMENT '服务类型';

alter table `service`
    add column `query_params_config` varchar(255) DEFAULT NULL comment "服务配置" after `query_params`;

alter table fee_config modify column `service_id` varchar (255) COMMENT '服务Id';

alter table fee_detail
    add column `save_ip` varchar(32) COMMENT '统计方ip';

CREATE TABLE `base_service`
(
    `id`           varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `service_id`   varchar(256)          DEFAULT NULL COMMENT '服务ID',
    `created_by`   varchar(32)           DEFAULT NULL COMMENT '创建人',
    `created_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`   varchar(32)           DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime              DEFAULT NULL COMMENT '更新时间',
    `name`         varchar(255) NOT NULL COMMENT '服务名',
    `url`          varchar(128)          DEFAULT '' COMMENT '服务地址',
    `service_type` tinyint(2) NOT NULL COMMENT '服务类型',
    `status`       tinyint(2) DEFAULT '0' COMMENT '是否在线 1在线，0离线',
    PRIMARY KEY (`id`),
    KEY            `url_unique` (`url`),
    KEY            `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务';

CREATE TABLE `table_service`
(
    `id`                  varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32)          DEFAULT NULL COMMENT '创建人',
    `created_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`          varchar(32)          DEFAULT NULL COMMENT '更新人',
    `updated_time`        datetime             DEFAULT NULL COMMENT '更新时间',
    `query_params`        text COMMENT '查询参数配置',
    `query_params_config` varchar(255)         DEFAULT NULL,
    `data_source`         text COMMENT 'SQL配置',
    `service_config`      text COMMENT '服务配置',
    `ids_table_name`      varchar(100)         DEFAULT NULL COMMENT '主键对应的表名',
    `operator`            varchar(10)          DEFAULT NULL COMMENT '操作 sum / avg',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务表';

CREATE TABLE `table_model`
(
    `id`                  varchar(32) NOT NULL,
    `algorithm`           varchar(64)          DEFAULT NULL COMMENT '算法',
    `fl_type`             varchar(64)          DEFAULT '' COMMENT '联邦学习类型',
    `feature_source`      varchar(64)          DEFAULT '',
    `model_param`         mediumtext COMMENT '模型参数',
    `source_path`         varchar(255)         DEFAULT NULL COMMENT '文件路径',
    `filename`            varchar(255)         DEFAULT NULL COMMENT '文件名',
    `use_count`           int(11) DEFAULT '0' COMMENT '使用计数',
    `created_by`          varchar(32)          DEFAULT NULL COMMENT '创建人',
    `created_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`        datetime             DEFAULT NULL COMMENT '更新时间',
    `updated_by`          varchar(32)          DEFAULT NULL COMMENT '更新人',
    `sql_script`          varchar(1024)        DEFAULT NULL COMMENT 'sql脚本',
    `sql_condition_field` varchar(100)         DEFAULT NULL COMMENT 'sql查询条件字段',
    `data_source_id`      varchar(100)         DEFAULT NULL COMMENT '数据源ID',
    `scores_distribution` TEXT                 DEFAULT NULL COMMENT '得分分布',
    `score_card_info`     TEXT                 DEFAULT NULL COMMENT '评分卡信息',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型表';


CREATE TABLE `model_predict_score_statistics`
(
    `id`           varchar(32) NOT NULL,
    `service_id`   varchar(255)         DEFAULT NULL COMMENT '服务ID',
    `count`        int(11) DEFAULT 0 COMMENT '分箱计数',
    `day`          datetime    not NULL COMMENT '日期',
    `split_point`  double               DEFAULT NULL COMMENT '分箱分割点',
    `created_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by`   varchar(32)          DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime             DEFAULT NULL COMMENT '更新时间',
    `updated_by`   varchar(32)          DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分箱统计表';



CREATE TABLE `model_predict_score_record`
(
    `id`           varchar(32) NOT NULL,
    `service_id`   varchar(255)         DEFAULT NULL COMMENT '服务ID',
    `score`        double COMMENT '概率或分数',
    `created_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by`   varchar(32)          DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime             DEFAULT NULL COMMENT '更新时间',
    `updated_by`   varchar(32)          DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测记录表';

alter table `partner`  add column `is_me` tinyint(1) NOT NULL COMMENT '是否是我自己';

-- 删除手机号唯一索引
ALTER TABLE `account` DROP INDEX `index_unique_phonenumber`;

ALTER TABLE client_service ADD secret_key_type varchar(10) NULL;

CREATE TABLE `psi_service_result`(
    `id` bigint(20) NOT NULL primary key AUTO_INCREMENT COMMENT 'ID',
    `request_id` varchar(32) DEFAULT NULL COMMENT '请求ID',
    `service_id` varchar(32) DEFAULT NULL COMMENT '服务ID',
    `service_name` varchar(32) DEFAULT NULL COMMENT '服务名称',
    `result` text COMMENT '结果',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='psi 服务交集结果表';
