-- account definition

CREATE TABLE `account`
(
    `id`               varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`       varchar(32)  DEFAULT NULL COMMENT '创建人',
    `created_time`     datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`       varchar(32)  DEFAULT NULL COMMENT '更新人',
    `updated_time`     datetime(6) DEFAULT NULL COMMENT '更新时间',
    `phone_number`     varchar(32)  NOT NULL COMMENT '手机号',
    `password`         varchar(128) NOT NULL COMMENT '密码',
    `salt`             varchar(128) NOT NULL COMMENT '盐',
    `nickname`         varchar(32)  NOT NULL COMMENT '昵称',
    `email`            varchar(128) NOT NULL COMMENT '邮箱',
    `super_admin_role` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是超级管理员 超级管理员通常是第一个创建并初始化系统的那个人',
    `admin_role`       tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是管理员 管理员有更多权限，比如设置 menber 是否对外可见。',
    `audit_status`     varchar(32)  NOT NULL COMMENT '审核状态',
    `audit_comment`    varchar(512) DEFAULT NULL COMMENT '审核意见',
    `enable`           tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_unique_phonenumber` (`phone_number`),
    KEY                `idx_create_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号';


-- bloom_filter definition

CREATE TABLE `bloom_filter`
(
    `id`                   varchar(64)  NOT NULL,
    `name`                 varchar(255) NOT NULL,
    `e`                    text,
    `n`                    text,
    `d`                    text,
    `src`                  varchar(255) DEFAULT NULL,
    `created_by`           varchar(255) DEFAULT NULL,
    `updated_by`           varchar(255) DEFAULT NULL,
    `created_time`         datetime     DEFAULT NULL,
    `updated_time`         datetime     DEFAULT NULL,
    `data_source_id`       varchar(32)  DEFAULT NULL,
    `source_path`          varchar(255) DEFAULT NULL COMMENT '数据源地址',
    `description`          varchar(32)  DEFAULT NULL,
    `data_resource_source` varchar(32)  DEFAULT NULL,
    `row_count`            int(255) DEFAULT NULL,
    `used_count`           int(255) DEFAULT NULL,
    `statement`            varchar(255) DEFAULT NULL COMMENT 'sql语句',
    `rows`                 text,
    `process`              varchar(255) DEFAULT NULL,
    `process_count`        int(255) DEFAULT NULL,
    `hash_function`        varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- data_set definition

CREATE TABLE `data_set`
(
    `id`                   varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`           varchar(32)   DEFAULT NULL COMMENT '创建人',
    `created_time`         datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`           varchar(32)   DEFAULT NULL COMMENT '更新人',
    `updated_time`         datetime      DEFAULT NULL COMMENT '更新时间',
    `name`                 varchar(128)  DEFAULT NULL COMMENT '数据集名称',
    `description`          varchar(3072) DEFAULT NULL COMMENT '描述',
    `data_resource_source` varchar(32)   DEFAULT NULL COMMENT '存储类型',
    `used_count`           int(20) DEFAULT NULL COMMENT '数据行数',
    `row_count`            int(20) DEFAULT NULL,
    `source_path`          varchar(255)  DEFAULT NULL COMMENT '文件源地址',
    `data_source_id`       varchar(32)   DEFAULT NULL,
    `is_storaged`          tinyint(255) DEFAULT '0',
    `statement`            varchar(255)  DEFAULT NULL COMMENT 'sql语句',
    `rows`                 varchar(255)  DEFAULT NULL,
    `process`              varchar(255)  DEFAULT NULL,
    `process_count`        int(255) DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='数据集';


-- data_source definition

CREATE TABLE `data_source`
(
    `database_type` varchar(255) DEFAULT NULL,
    `host`          varchar(255) DEFAULT NULL,
    `port`          int(255) DEFAULT NULL,
    `database_name` varchar(255) DEFAULT NULL,
    `user_name`     varchar(255) DEFAULT NULL,
    `password`      varchar(255) DEFAULT NULL,
    `created_by`    varchar(255) DEFAULT NULL,
    `id`            varchar(32) NOT NULL,
    `created_time`  datetime     DEFAULT NULL,
    `updated_time`  datetime     DEFAULT NULL,
    `name`          varchar(255) DEFAULT NULL,
    `updated_by`    varchar(255) DEFAULT '',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;


-- field_info definition

CREATE TABLE `field_info`
(
    `id`           varchar(64)  NOT NULL,
    `business_id`  varchar(64)  NOT NULL,
    `columns`      varchar(255) NOT NULL COMMENT '字段集合',
    `options`      varchar(32)  NOT NULL COMMENT '处理方式',
    `frist_index`  int(11) DEFAULT NULL COMMENT '处理起始位',
    `end_index`    int(11) DEFAULT NULL COMMENT '处理终止位',
    `created_by`   varchar(32) DEFAULT NULL,
    `updated_by`   varchar(32) DEFAULT NULL,
    `created_time` datetime     NOT NULL,
    `updated_time` datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `position`     tinyint(1) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- global_config definition

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局设置。';


-- global_setting definition

CREATE TABLE `global_setting`
(
    `id`               varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`       varchar(32)           DEFAULT NULL COMMENT '创建人',
    `created_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by`       varchar(32)           DEFAULT NULL COMMENT '更新人',
    `updated_time`     datetime              DEFAULT NULL COMMENT '更新时间',
    `partner_id`       varchar(32)  NOT NULL COMMENT '联邦成员 Id 全局唯一，默认为uuid。',
    `partner_name`     varchar(128) NOT NULL COMMENT '联邦成员名称',
    `rsa_private_key`  text         NOT NULL,
    `rsa_public_key`   text         NOT NULL COMMENT '公钥',
    `open_socket_port` int(6) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局设置 全局设置，这个表永远有且只有一条数据。';


-- partner definition

CREATE TABLE `partner`
(
    `id`             varchar(64)  NOT NULL,
    `member_id`      varchar(64)  NOT NULL COMMENT '合作方id',
    `member_name`    varchar(255) NOT NULL COMMENT '合作方',
    `rsa_public_key` text COMMENT '公钥',
    `base_url`       varchar(255) NOT NULL COMMENT '调用路径',
    `created_by`     varchar(32) DEFAULT NULL,
    `updated_by`     varchar(32) DEFAULT NULL,
    `created_time`   datetime     NOT NULL,
    `updated_time`   datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;


-- task definition

CREATE TABLE `task`
(
    `id`                 varchar(64)  NOT NULL,
    `business_id`        varchar(64)  NOT NULL COMMENT '业务ID',
    `name`               varchar(255) NOT NULL COMMENT '任务名称',
    `status`             varchar(32)  NOT NULL COMMENT '任务状态',
    `error`              text COMMENT '任务错误信息',
    `partner_member_id`  varchar(32)  NOT NULL COMMENT '合作伙伴id',
    `data_resource_id`   varchar(32)   DEFAULT NULL COMMENT '数据集id',
    `data_resource_type` varchar(21)   DEFAULT NULL,
    `row_count`          int(11) DEFAULT NULL COMMENT '对齐数据行数',
    `psi_actuator_role`  varchar(32)   DEFAULT NULL,
    `algorithm`          varchar(32)   DEFAULT NULL,
    `data_count`         int(11) DEFAULT NULL COMMENT '处理总数',
    `fusion_count`       int(11) DEFAULT NULL COMMENT '已融合数',
    `spend`              bigint(20) DEFAULT NULL,
    `created_by`         varchar(32)   DEFAULT NULL,
    `updated_by`         varchar(32)   DEFAULT NULL,
    `created_time`       datetime     NOT NULL,
    `updated_time`       datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `description`        varchar(1024) DEFAULT NULL COMMENT '描述',
    `is_trace`           tinyint(1) NOT NULL DEFAULT '0',
    `trace_column`       varchar(255)  DEFAULT NULL,
    `my_role`            varchar(255)  DEFAULT NULL,
    `processed_count`    int(10) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;