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


-- 添加融合主键展示字段
ALTER TABLE bloom_filter ADD hash_function varchar(255) NULL;

-- 修改合作伙伴成员字段
ALTER TABLE partner CHANGE partner_id member_id  varchar(64)  NOT NULL COMMENT '合作方id';
ALTER TABLE partner CHANGE `name` member_name  varchar(255)  NOT NULL COMMENT '合作方名称';

-- 修改任务相关字段
ALTER TABLE task CHANGE partner_id partner_member_id  varchar(32)  NOT NULL COMMENT '合作成员id';
ALTER TABLE task ADD `my_role` varchar(255) DEFAULT NULL;
ALTER TABLE task ADD `processed_count` int(10) DEFAULT NULL;



