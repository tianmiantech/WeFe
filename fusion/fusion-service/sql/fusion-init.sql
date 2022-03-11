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
    `id`                   varchar(64)  NOT NULL COMMENT '全局唯一标识',
    `name`                 varchar(255) NOT NULL COMMENT '布隆过滤器名称',
    `e`                    text COMMENT '加密密钥e',
    `n`                    text COMMENT '模数N',
    `d`                    text COMMENT '解密密钥d',
    `src`                  varchar(255) DEFAULT NULL COMMENT '布隆过滤器文件路径',
    `created_by`           varchar(255) DEFAULT NULL COMMENT '创建人',
    `updated_by`           varchar(255) DEFAULT NULL COMMENT '更新人',
    `created_time`         datetime     DEFAULT NULL COMMENT '创建时间',
    `updated_time`         datetime     DEFAULT NULL COMMENT '更新时间',
    `data_source_id`       varchar(32)  DEFAULT NULL COMMENT '数据源id',
    `source_path`          varchar(255) DEFAULT NULL COMMENT '数据源地址',
    `description`          varchar(32)  DEFAULT NULL COMMENT '描述',
    `data_resource_source` varchar(32)  DEFAULT NULL COMMENT '存储类型',
    `row_count`            int(255) DEFAULT NULL COMMENT '数据行数',
    `used_count`           int(255) DEFAULT NULL COMMENT '使用次数',
    `statement`            varchar(255) DEFAULT NULL COMMENT 'sql语句',
    `rows`                 text COMMENT '数据列名',
    `process`              varchar(255) DEFAULT NULL COMMENT '处理进度',
    `process_count`        int(255) DEFAULT NULL COMMENT '处理总数',
    `hash_function`        varchar(255) DEFAULT NULL COMMENT '加密组合方式',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='布隆过滤器';


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
    `used_count`           int(20) DEFAULT NULL COMMENT '使用次数',
    `row_count`            int(20) DEFAULT NULL COMMENT '数据行数',
    `source_path`          varchar(255)  DEFAULT NULL COMMENT '文件源地址',
    `data_source_id`       varchar(32)   DEFAULT NULL COMMENT '数据源id',
    `is_storaged`          tinyint(255) DEFAULT '0'  COMMENT '是否已存',
    `statement`            varchar(255)  DEFAULT NULL COMMENT 'sql语句',
    `rows`                 varchar(255)  DEFAULT NULL COMMENT '数据列名',
    `process`              varchar(255)  DEFAULT NULL COMMENT '处理进度',
    `process_count`        int(255) DEFAULT NULL COMMENT '处理总数',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='数据集';


-- data_source definition

CREATE TABLE `data_source`
(
    `database_type` varchar(255) DEFAULT NULL COMMENT '数据库类型',
    `host`          varchar(255) DEFAULT NULL COMMENT '数据库ip',
    `port`          int(255) DEFAULT NULL COMMENT '数据库端口',
    `database_name` varchar(255) DEFAULT NULL COMMENT '库名',
    `user_name`     varchar(255) DEFAULT NULL COMMENT '连接数据库用户名',
    `password`      varchar(255) DEFAULT NULL COMMENT '连接数据库密码',
    `created_by`    varchar(255) DEFAULT NULL COMMENT '创建人',
    `id`            varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_time`  datetime     DEFAULT NULL COMMENT '创建时间',
    `updated_time`  datetime     DEFAULT NULL COMMENT '更新时间',
    `name`          varchar(255) DEFAULT NULL COMMENT '名称',
    `updated_by`    varchar(255) DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='源数据库';


-- field_info definition

CREATE TABLE `field_info`
(
    `id`           varchar(64)  NOT NULL COMMENT '全局唯一标识',
    `business_id`  varchar(64)  NOT NULL COMMENT '商户id',
    `columns`      varchar(255) NOT NULL COMMENT '字段集合',
    `options`      varchar(32)  NOT NULL COMMENT '处理方式',
    `frist_index`  int(11) DEFAULT NULL COMMENT '处理起始位',
    `end_index`    int(11) DEFAULT NULL COMMENT '处理终止位',
    `created_by`   varchar(32) DEFAULT NULL COMMENT '创建人',
    `updated_by`   varchar(32) DEFAULT NULL COMMENT '更新人',
    `created_time` datetime     NOT NULL COMMENT '创建时间',
    `updated_time` datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `position`     tinyint(1) NOT NULL COMMENT '位置',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加密信息';


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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局设置';


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
    `rsa_private_key`  text         NOT NULL COMMENT '私钥',
    `rsa_public_key`   text         NOT NULL COMMENT '公钥',
    `open_socket_port` int(6) NOT NULL COMMENT '开放socket端口',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局设置 全局设置，这个表永远有且只有一条数据。';


-- partner definition

CREATE TABLE `partner`
(
    `id`             varchar(64)  NOT NULL COMMENT '全局唯一标识',
    `member_id`      varchar(64)  NOT NULL COMMENT '合作方id',
    `member_name`    varchar(255) NOT NULL COMMENT '合作方',
    `rsa_public_key` text COMMENT '公钥',
    `base_url`       varchar(255) NOT NULL COMMENT '调用路径',
    `created_by`     varchar(32) DEFAULT NULL COMMENT '创建人',
    `updated_by`     varchar(32) DEFAULT NULL COMMENT '更新人',
    `created_time`   datetime     NOT NULL COMMENT '创建时间',
    `updated_time`   datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='合作方';


-- task definition

CREATE TABLE `task`
(
    `id`                 varchar(64)  NOT NULL COMMENT '全局唯一标识',
    `business_id`        varchar(64)  NOT NULL COMMENT '业务ID',
    `name`               varchar(255) NOT NULL COMMENT '任务名称',
    `status`             varchar(32)  NOT NULL COMMENT '任务状态',
    `error`              text COMMENT '任务错误信息',
    `partner_member_id`  varchar(32)  NOT NULL COMMENT '合作伙伴id',
    `data_resource_id`   varchar(32)   DEFAULT NULL COMMENT '数据源id',
    `data_resource_type` varchar(21)   DEFAULT NULL COMMENT '数据源存储类型',
    `row_count`          int(11) DEFAULT NULL COMMENT '对齐数据行数',
    `psi_actuator_role`  varchar(32)   DEFAULT NULL COMMENT 'rsa-psi算法角色',
    `algorithm`          varchar(32)   DEFAULT NULL COMMENT '算法',
    `data_count`         int(11) DEFAULT NULL COMMENT '处理总数',
    `fusion_count`       int(11) DEFAULT NULL COMMENT '已融合数',
    `spend`              bigint(20) DEFAULT NULL COMMENT '处理总数',
    `created_by`         varchar(32)   DEFAULT NULL COMMENT '创建人',
    `updated_by`         varchar(32)   DEFAULT NULL COMMENT '更新人',
    `created_time`       datetime     NOT NULL COMMENT '创建时间',
    `updated_time`       datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `description`        varchar(1024) DEFAULT NULL COMMENT '描述',
    `is_trace`           tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否回溯',
    `trace_column`       varchar(255)  DEFAULT NULL COMMENT '回溯字段',
    `my_role`            varchar(255)  DEFAULT NULL COMMENT '角色',
    `processed_count`    int(10) DEFAULT NULL COMMENT '处理总数',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC COMMENT='融合任务';