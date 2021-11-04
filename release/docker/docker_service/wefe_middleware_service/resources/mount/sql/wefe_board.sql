/*
 Navicat Premium Data Transfer

 Source Server         : wefe_board-fat
 Source Server Type    : MySQL
 Source Server Version : 50720
 Source Host           : 10.**.**.33:3306
 Source Schema         : wefe_board

 Target Server Type    : MySQL
 Target Server Version : 50720
 File Encoding         : 65001

 Date: 27/08/2020 13:34:03
*/

SET NAMES utf8mb4;
SET
FOREIGN_KEY_CHECKS = 0;

-- drop all tables
-- select concat("DROP TABLE IF EXISTS ", table_name, ";") from information_schema.tables where table_schema = "wefe_board";

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`
(
    `id`               varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`       varchar(32) COMMENT '创建人',
    `created_time`     datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`       varchar(32) COMMENT '更新人',
    `updated_time`     datetime(6) COMMENT '更新时间',
    `phone_number`     varchar(32)  NOT NULL COMMENT '手机号',
    `password`         varchar(128) NOT NULL COMMENT '密码',
    `salt`             varchar(128) NOT NULL COMMENT '盐',
    `nickname`         varchar(32)  NOT NULL COMMENT '昵称',
    `email`            varchar(128) NOT NULL COMMENT '邮箱',
    `super_admin_role` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是超级管理员 超级管理员通常是第一个创建并初始化系统的那个人',
    `admin_role`       tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是管理员 管理员有更多权限，比如设置 member 是否对外可见。',
    `audit_status`     varchar(32)  NOT NULL COMMENT '审核状态',
    `audit_comment`    varchar(512) COMMENT '审核意见',
    `enable`           tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_unique_phonenumber` (`phone_number`),
    KEY                `idx_create_time` (`created_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='账号';

-- ----------------------------
-- Table structure for blacklist
-- ----------------------------
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist`
(
    `id`                  varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32) COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`          varchar(32) COMMENT '更新人',
    `updated_time`        datetime(6) COMMENT '更新时间',
    `member_id`           varchar(32)  NOT NULL COMMENT '成员id',
    `blacklist_member_id` varchar(32)  NOT NULL COMMENT '被加入黑名单成员id',
    `remark`              varchar(128) NOT NULL COMMENT '备注',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='黑名单';

-- ----------------------------
-- Table structure for cur_best_model
-- ----------------------------
DROP TABLE IF EXISTS `cur_best_model`;
CREATE TABLE `cur_best_model`
(
    `id`             varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `job_id`         varchar(32)  NOT NULL COMMENT 'job Id',
    `task_id`        varchar(100) NOT NULL COMMENT '任务 Id',
    `component_name` varchar(32)  NOT NULL COMMENT '组件名称',
    `role`           varchar(32)  NOT NULL COMMENT '角色',
    `member_id`      varchar(32)  NOT NULL COMMENT '成员id',
    `model_meta`     text COMMENT '模型信息',
    `model_param`    mediumtext COMMENT '模型参数',
    `iteration`      int(11) DEFAULT '0' COMMENT '当前迭代索引',
    `created_by`     varchar(100) COMMENT '创建人',
    `updated_by`     varchar(100) COMMENT '修改人',
    `created_time`   datetime(6) NOT NULL COMMENT '创建时间',
    `updated_time`   datetime(6) COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='当前最优模型';

-- ----------------------------
-- Table structure for data_set
-- ----------------------------
DROP TABLE IF EXISTS `data_set`;
CREATE TABLE `data_set`
(
    `id`                     varchar(32)   NOT NULL COMMENT '全局唯一标识',
    `created_by`             varchar(32) COMMENT '创建人',
    `created_time`           datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`             varchar(32) COMMENT '更新人',
    `updated_time`           datetime(6) COMMENT '更新时间',
    `name`                   varchar(128)  NOT NULL COMMENT '数据集名称',
    `tags`                   varchar(128) COMMENT '标签',
    `description`            varchar(3072) COMMENT '描述',
    `storage_type`           varchar(32) COMMENT '存储类型',
    `namespace`              varchar(1000) NOT NULL COMMENT '命名空间',
    `table_name`             varchar(1000) NOT NULL COMMENT '表名',
    `row_count`              bigint(20) NOT NULL COMMENT '数据行数',
    `primary_key_column`     varchar(32)   NOT NULL COMMENT '主键字段',
    `column_count`           int(11) NOT NULL COMMENT '数据集列数',
    `column_name_list`       text          NOT NULL COMMENT '数据集字段列表',
    `feature_count`          int(11) NOT NULL COMMENT '特征数量',
    `feature_name_list`      text COMMENT '特征列表',
    `contains_y`             tinyint(1) NOT NULL COMMENT '是否包含 Y 值',
    `y_count`                int(11) NOT NULL COMMENT 'y列的数量',
    `y_name_list`            text COMMENT 'y列名称列表',
    `public_level`           varchar(32) COMMENT '数据集的可见性',
    `public_member_list`     varchar(3072) COMMENT '可见成员列表 只有在列表中的联邦成员才可以看到该数据集的基本信息',
    `usage_count_in_job`     int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    `usage_count_in_flow`    int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    `usage_count_in_project` int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    `source_type`            varchar(32) COMMENT '来源类型，枚举（原始、对齐、分箱）',
    `source_flow_id`         varchar(64) COMMENT '来源流程id',
    `source_job_id`          varchar(64) COMMENT '来源任务id',
    `source_task_id`         varchar(100) COMMENT '来源子任务id',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据集';

-- ----------------------------
-- Table structure for data_set_column
-- ----------------------------
DROP TABLE IF EXISTS `data_set_column`;
CREATE TABLE `data_set_column`
(
    `id`                 varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`         varchar(32) COMMENT '创建人',
    `created_time`       datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`         varchar(32) COMMENT '更新人',
    `updated_time`       datetime(6) COMMENT '更新时间',
    `data_set_id`        varchar(32)  NOT NULL COMMENT '数据集Id',
    `index`              int(32) NOT NULL COMMENT '序号',
    `name`               varchar(255) NOT NULL COMMENT '字段名称',
    `data_type`          varchar(32)  NOT NULL COMMENT '数据类型',
    `comment`            varchar(255) COMMENT '注释',
    `empty_rows`         bigint(255) DEFAULT '0' COMMENT '空值数据行数',
    `value_distribution` json COMMENT '数值分布',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据集字段';


-- ----------------------------
-- Table structure for flow_action_log
-- ----------------------------
DROP TABLE IF EXISTS `flow_action_log`;
CREATE TABLE `flow_action_log`
(
    `id`           varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`   varchar(32) COMMENT '创建人',
    `created_time` datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`   varchar(32) COMMENT '更新人',
    `updated_time` datetime(6) COMMENT '更新时间',
    `producer`     varchar(32) NOT NULL COMMENT '消息生产者',
    `priority`     int(11) NOT NULL COMMENT '优先级',
    `action`       varchar(32) NOT NULL COMMENT '动作名称',
    `params`       text COMMENT '动作参数',
    `status`       varchar(32) COMMENT '执行状态 枚举（success/fail）',
    `remark`       varchar(1024) COMMENT '备注信息',
    `consumer_ip`  varchar(100) COMMENT '消费者ip',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息消费日志';

-- ----------------------------
-- Table structure for flow_action_queue
-- ----------------------------
DROP TABLE IF EXISTS `flow_action_queue`;
CREATE TABLE `flow_action_queue`
(
    `id`           varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`   varchar(32) COMMENT '创建人',
    `created_time` datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`   varchar(32) COMMENT '更新人',
    `updated_time` datetime(6) COMMENT '更新时间',
    `producer`     varchar(32) NOT NULL COMMENT '消息生产者',
    `priority`     int(11) NOT NULL DEFAULT '0' COMMENT '优先级 优先级大的会被先消费',
    `action`       varchar(32) NOT NULL COMMENT '动作名称',
    `params`       text COMMENT '动作参数',
    `channel`      varchar(50) DEFAULT NULL COMMENT '消息产生渠道',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息队列 ';

-- ----------------------------
-- Table structure for global_setting
-- ----------------------------
DROP TABLE IF EXISTS `global_setting`;
CREATE TABLE `global_setting`
(
    `id`                           varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`                   varchar(32) COMMENT '创建人',
    `created_time`                 datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`                   varchar(32) COMMENT '更新人',
    `updated_time`                 datetime(6) COMMENT '更新时间',
    `member_id`                    varchar(32)  NOT NULL COMMENT '联邦成员 Id 全局唯一，默认为uuid。',
    `member_name`                  varchar(128) NOT NULL COMMENT '联邦成员名称',
    `member_email`                 varchar(32) COMMENT '联邦成员邮箱',
    `member_mobile`                varchar(32) COMMENT '联邦成员电话',
    `member_gateway_uri`           varchar(512) COMMENT '联邦成员网关访问地址',
    `member_allow_public_data_set` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许对外公开数据集基础信息',
    `member_logo`                  mediumblob COMMENT '成员头像',
    `member_hidden`                tinyint(1) NOT NULL DEFAULT '0' COMMENT '成员隐身状态，默认否',
    `rsa_private_key`              text         NOT NULL COMMENT '私钥',
    `rsa_public_key`               text         NOT NULL COMMENT '公钥',
    `gateway_uri`                  varchar(512) COMMENT '网关通信地址',
    `board_uri`                    varchar(512) COMMENT 'board通信地址',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='全局设置 全局设置，这个表永远有且只有一条数据。';

-- ----------------------------
-- Table structure for job
-- ----------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job`
(
    `id`                       varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`               varchar(32) COMMENT '创建人',
    `created_time`             datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`               varchar(32) COMMENT '更新人',
    `updated_time`             datetime(6) COMMENT '更新时间',
    `project_id`               varchar(32)  NOT NULL COMMENT '项目ID',
    `flow_id`                  varchar(32)  NOT NULL COMMENT '流程ID',
    `job_id`                   varchar(32)  NOT NULL COMMENT '任务ID',
    `federated_learning_type`  varchar(32)  NOT NULL COMMENT '联邦任务类型（横向/纵向）',
    `name`                     varchar(128) NOT NULL COMMENT '名称',
    `my_role`                  varchar(32)  NOT NULL COMMENT '我方身份 枚举（promoter/provider/arbiter）',
    `status`                   varchar(32)  NOT NULL DEFAULT 'created' COMMENT '状态 枚举',
    `status_updated_time`      datetime(6) COMMENT '状态更新时间',
    `start_time`               datetime(6) COMMENT '开始时间',
    `finish_time`              datetime(6) COMMENT '结束时间',
    `progress`                 int(11) NOT NULL DEFAULT '0' COMMENT '进度',
    `progress_updated_time`    datetime(6) COMMENT '进度更新时间',
    `message`                  text COMMENT '消息备注 失败原因/备注',
    `graph`                    longtext COMMENT '有向无环图',
    `has_modeling_result`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否包含建模结果',
    `star`                     tinyint(1) NOT NULL DEFAULT '0' COMMENT '收藏/置顶/标记',
    `job_middle_data_is_clear` tinyint(1) NOT NULL DEFAULT '0' COMMENT '中间数据是否已清理',
    `remark`                   text COMMENT '备注',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`job_id`, `my_role`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务';

-- ----------------------------
-- Table structure for job_member
-- ----------------------------
DROP TABLE IF EXISTS `job_member`;
CREATE TABLE `job_member`
(
    `id`           varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`   varchar(32) COMMENT '创建人',
    `created_time` datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`   varchar(32) COMMENT '更新人',
    `updated_time` datetime(6) COMMENT '更新时间',
    `project_id`   varchar(32) NOT NULL COMMENT '项目Id',
    `flow_id`      varchar(32) NOT NULL COMMENT '流程Id',
    `job_id`       varchar(32) NOT NULL COMMENT '任务Id',
    `job_role`     varchar(32) NOT NULL COMMENT '在任务中的角色 枚举（promoter/provider/arbiter）',
    `member_id`    varchar(32) NOT NULL COMMENT '成员 Id',
    `data_set_id`  varchar(32) COMMENT '数据集 Id',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`job_id`, `job_role`, `member_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务成员 每个任务中各参与方的相关信息';

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`
(
    `id`           varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`   varchar(32) COMMENT '创建人',
    `created_time` datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`   varchar(32) COMMENT '更新人',
    `updated_time` datetime(6) COMMENT '更新时间',
    `producer`     varchar(32) NOT NULL COMMENT '消息生产者 枚举（board/gateway）',
    `level`        varchar(32) COMMENT '消息级别 枚举（info/success/error/warning）',
    `title`        varchar(128) COMMENT '标题',
    `content`      text COMMENT '内容',
    `unread`       tinyint(1) NOT NULL DEFAULT '0' COMMENT '未读',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息列表 ';

-- ----------------------------
-- Table structure for message_queue
-- ----------------------------
DROP TABLE IF EXISTS `message_queue`;
CREATE TABLE `message_queue`
(
    `id`           varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`   varchar(32) COMMENT '创建人',
    `created_time` datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`   varchar(32) COMMENT '更新人',
    `updated_time` datetime(6) COMMENT '更新时间',
    `producer`     varchar(32) NOT NULL COMMENT '消息生产者',
    `priority`     int(11) NOT NULL DEFAULT '0' COMMENT '优先级 优先级大的会被先消费',
    `action`       varchar(32) NOT NULL COMMENT '动作名称',
    `params`       text COMMENT '动作参数',
    `channel`      varchar(50) COMMENT '消息产生渠道',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='消息队列';


-- ----------------------------
-- Table structure for operator_log
-- ----------------------------
DROP TABLE IF EXISTS `operator_log`;
CREATE TABLE `operator_log`
(
    `id`             varchar(32) NOT NULL COMMENT '操作日志编号',
    `log_interface`  varchar(50) COMMENT '请求接口',
    `interface_name` varchar(50) COMMENT '请求接口名称',
    `request_ip`     varchar(20) COMMENT '请求IP',
    `operator_id`    varchar(32) COMMENT '操作人员编号',
    `operator_phone` varchar(13) COMMENT '操作人员手机号',
    `token`          varchar(100) COMMENT '请求token',
    `log_action`     varchar(50) COMMENT '操作行为',
    `result_code`    int(20) COMMENT '请求结果code',
    `result_message` text COMMENT '请求结果消息',
    `request_time`   datetime(6) COMMENT '请求时间',
    `spend`          int(11) COMMENT '处理时长',
    `created_time`   datetime(6) COMMENT '创建时间',
    `updated_time`   datetime(6) COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;


-- ----------------------------
-- Table structure for server
-- ----------------------------
DROP TABLE IF EXISTS `server`;
CREATE TABLE `server`
(
    `id`                varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`        varchar(32) COMMENT '创建人',
    `created_time`      datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`        varchar(32) COMMENT '更新人',
    `updated_time`      datetime(6) COMMENT '更新时间',
    `server_name`       varchar(32) COMMENT '服务器名称',
    `server_ip`         varchar(32) COMMENT '服务器 IP',
    `cpu_core_count`    varchar(32) COMMENT 'CUP 核心数',
    `memory_total_size` varchar(32) COMMENT '内存总量',
    `fs_total_size`     varchar(32) COMMENT '文件系统总大小',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='服务器 ';


-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`
(
    `id`                               varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`                       varchar(32) COMMENT '创建人',
    `created_time`                     datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`                       varchar(32) COMMENT '更新人',
    `updated_time`                     datetime(6) COMMENT '更新时间',
    `board_url`                        varchar(32) COMMENT 'board域名',
    `board_port`                       int(11) COMMENT 'board端口',
    `gateway_ip_white_list`            text COMMENT '关网IP白名单,多个用英文逗号分隔',
    `open_mail_tips`                   int(11) DEFAULT '0' COMMENT '是否开启邮件提醒功能（0：关闭；1：打开）',
    `mail_host`                        varchar(255) COMMENT '邮件服务器地址',
    `mail_port`                        int(11) COMMENT '邮件服务器端口',
    `mail_username`                    varchar(255) COMMENT '邮件用户名',
    `mail_password`                    varchar(255) COMMENT '邮件密码',
    `mail_content`                     text COMMENT '邮件内容',
    `mail_subject`                     text COMMENT '邮件主题',
    `account_need_audit_when_register` tinyint(1) NOT NULL DEFAULT '1' COMMENT '新注册的账号是否需要管理员审核',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统配置 ';

-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task`
(
    `id`                  varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32) COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`          varchar(32) COMMENT '更新人',
    `updated_time`        datetime(6) COMMENT '更新时间',
    `project_id`          varchar(32) DEFAULT NULL COMMENT '项目Id',
    `flow_id`             varchar(32)  NOT NULL COMMENT '流程Id',
    `job_id`              varchar(32)  NOT NULL COMMENT '任务Id',
    `role`                varchar(32)  NOT NULL COMMENT '成员角色',
    `flow_node_id`        varchar(32)  NOT NULL COMMENT '任务在流程中的节点Id',
    `task_id`             varchar(100) NOT NULL COMMENT '业务Id',
    `deep`                int(8) COMMENT '深度，同一深度的 task 允许并行执行。',
    `name`                varchar(100) NOT NULL COMMENT '名称，在 job 中唯一。',
    `parent_task_id_list` varchar(512) COMMENT '子任务的父节点',
    `dependence_list`     varchar(512) COMMENT '子任务依赖',
    `task_type`           varchar(32)  NOT NULL COMMENT '子任务类型 枚举（DataIO/Intersection/HeteroLR...）',
    `task_conf`           text         NOT NULL COMMENT '任务conf_json',
    `status`              varchar(32)  NOT NULL COMMENT '状态 枚举（created/running/canceled/success/error）',
    `start_time`          datetime(6) COMMENT '开始时间',
    `finish_time`         datetime(6) COMMENT '结束时间',
    `spend`               int(11) COMMENT '执行耗时',
    `message`             varchar(3072) COMMENT '消息备注 失败原因/备注',
    `error_cause`         text COMMENT '发生错误的详细原因，通常是堆栈信息。',
    `position`            int(11) COMMENT 'task执行顺序',
    `pid`                 int(11) COMMENT '进程号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique_task` (`task_id`, `role`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='子任务 task 是 job 的基本构成单元，也是发送到 wefe-flow 被执行的标准对象。';


-- ----------------------------
-- Table structure for task_result
-- ----------------------------
DROP TABLE IF EXISTS `task_result`;
CREATE TABLE `task_result`
(
    `id`             varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`     varchar(32) COMMENT '创建人',
    `created_time`   datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`     varchar(32) COMMENT '更新人',
    `updated_time`   datetime(6) COMMENT '更新时间',
    `project_id`     varchar(32) DEFAULT NULL COMMENT '项目Id',
    `job_id`         varchar(32)  NOT NULL COMMENT '任务Id',
    `flow_id`        varchar(32)  NOT NULL COMMENT '流程Id',
    `flow_node_id`   varchar(32)  NOT NULL COMMENT '流程节点Id',
    `task_id`        varchar(100) NOT NULL COMMENT '子任务Id',
    `name`           varchar(256) NOT NULL COMMENT '任务名称，例如：vert_lr_0',
    `component_type` varchar(32)  NOT NULL COMMENT '组件类型',
    `role`           varchar(32)  NOT NULL COMMENT '成员角色',
    `type`           varchar(128) NOT NULL COMMENT '类型，一个 task 会有多行不同类型的 result',
    `result`         longtext     NOT NULL COMMENT '执行结果',
    `serving_model`  tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是可以导出到 serving 的模型',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_unique` (`task_id`, `type`, `role`),
    KEY              `idx_create_time` (`created_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='task 执行结果';


--
-- 项目表 project
--
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project`
(
    `id`                       varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`               varchar(32) COMMENT '创建人',
    `created_time`             datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`               varchar(32) COMMENT '更新人',
    `updated_time`             datetime(6) COMMENT '更新时间',
    `deleted`                  tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除',
    `name`                     varchar(128) NOT NULL COMMENT '名称',
    `project_desc`             text COMMENT '描述',
    `audit_status`             varchar(32)  NOT NULL COMMENT '综合审核状态',
    `audit_status_from_myself` varchar(32)  NOT NULL COMMENT '自己是否同意',
    `audit_status_from_others` varchar(32) COMMENT '其他人是否同意',
    `audit_comment`            varchar(512) COMMENT '审核意见',
    `status_updated_time`      datetime(6) COMMENT '状态更新时间',
    `start_time`               datetime(6) COMMENT '开始时间',
    `finish_time`              datetime(6) COMMENT '结束时间',
    `progress`                 int(11) NOT NULL DEFAULT '0' COMMENT '进度',
    `progress_updated_time`    datetime(6) COMMENT '进度更新时间',
    `message`                  text COMMENT '消息备注 失败原因/备注',
    `project_id`               varchar(32)  NOT NULL COMMENT '项目ID',
    `member_id`                varchar(32)  NOT NULL COMMENT '该项目的创建者ID',
    `my_role`                  varchar(32)  NOT NULL COMMENT '我方角色',
    `exited`                   tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已退出',
    `closed`                   tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已关闭',
    `flow_status_statistics`   varchar(512) COMMENT '流程状态统计',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目';

--
-- 项目数据集表 project_data_set
--
DROP TABLE IF EXISTS `project_data_set`;
CREATE TABLE `project_data_set`
(
    `id`                  varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32) COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`          varchar(32) COMMENT '更新人',
    `updated_time`        datetime(6) COMMENT '更新时间',
    `project_id`          varchar(32) NOT NULL COMMENT '项目 Id',
    `member_id`           varchar(32) NOT NULL COMMENT '项目成员 Id',
    `member_role`         varchar(32) NOT NULL COMMENT '在任务中的角色 枚举',
    `data_set_id`         varchar(32) COMMENT '数据集 Id',
    `audit_status`        varchar(32) COMMENT '审核状态',
    `audit_comment`       varchar(512) COMMENT '审核意见',
    `status_updated_time` datetime(6) COMMENT '状态更新时间',
    `source_type`         varchar(32) COMMENT '来源类型，枚举（原始、对齐、分箱）',
    `source_job_id`       varchar(64) COMMENT '来源任务id',
    `source_task_id`      varchar(100) COMMENT '来源子任务id',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`project_id`, `member_role`, `data_set_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目数据集';


--
-- 项目成员表   project_member
--
DROP TABLE IF EXISTS `project_member`;
CREATE TABLE `project_member`
(
    `id`                       varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`               varchar(32) COMMENT '创建人',
    `created_time`             datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`               varchar(32) COMMENT '更新人',
    `updated_time`             datetime(6) COMMENT '更新时间',
    `project_id`               varchar(32) NOT NULL COMMENT '项目 Id',
    `member_id`                varchar(32) NOT NULL COMMENT '成员 Id',
    `inviter_id`               varchar(32) COMMENT '邀请方成员Id',
    `from_create_project`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是初始化项目时添加进来的（关系到审核流程不同）',
    `member_role`              varchar(32) NOT NULL COMMENT '在任务中的角色 枚举',
    `member_status`            varchar(32) NOT NULL DEFAULT 'created' COMMENT '状态',
    `audit_status`             varchar(32) NOT NULL COMMENT '综合审核状态',
    `audit_status_from_myself` varchar(32) NOT NULL COMMENT '自己是否同意',
    `audit_status_from_others` varchar(32) COMMENT '其他人是否同意',
    `audit_comment`            varchar(512) COMMENT '审核意见',
    `status_updated_time`      datetime(6) COMMENT '状态更新时间',
    `exited`                   tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已退出',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`project_id`, `member_id`, `member_role`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目成员';


--
-- 项目流程表
--
DROP TABLE IF EXISTS `project_flow`;
CREATE TABLE `project_flow`
(
    `id`                      varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`              varchar(32) COMMENT '创建人',
    `created_time`            datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`              varchar(32) COMMENT '更新人',
    `updated_time`            datetime(6) COMMENT '更新时间',
    `deleted`                 tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除',
    `project_id`              varchar(32)  NOT NULL COMMENT '项目ID',
    `flow_id`                 varchar(36)  NOT NULL COMMENT '流程ID',
    `flow_status`             varchar(32)  NOT NULL COMMENT '流程状态',
    `federated_learning_type` varchar(32)  NOT NULL COMMENT '联邦任务类型（横向/纵向）',
    `status_updated_time`     datetime(6) COMMENT '状态更新时间',
    `message`                 text COMMENT '相关消息',
    `my_role`                 varchar(32)  NOT NULL COMMENT '我方角色',
    `flow_name`               varchar(256) NOT NULL COMMENT '流程名称',
    `flow_desc`               text COMMENT '流程描述',
    `graph`                   longtext COMMENT '画布中编辑的图',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`flow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目流程';


DROP TABLE IF EXISTS `project_flow_node`;
CREATE TABLE `project_flow_node`
(
    `id`                  varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32) COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`          varchar(32) COMMENT '更新人',
    `updated_time`        datetime(6) COMMENT '更新时间',
    `start_node`          tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是起始节点',
    `node_id`             varchar(64) NOT NULL COMMENT '前端画布中的节点id，由前端生成',
    `project_id`          varchar(32) NOT NULL COMMENT '项目ID',
    `flow_id`             varchar(32) NOT NULL COMMENT '流程ID',
    `parent_node_id_list` varchar(1024) COMMENT '父节点',
    `component_type`      varchar(32) NOT NULL COMMENT '组件类型',
    `params`              longtext COMMENT '组件参数',
    `params_version`      long        NOT NULL COMMENT '组件参数版本号（时间戳）',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`flow_id`, `node_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='流程中的节点';


--
-- 项目中追加成员时的审核记录表
--
DROP TABLE IF EXISTS `project_member_audit`;
CREATE TABLE `project_member_audit`
(
    `id`            varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`    varchar(32) COMMENT '创建人',
    `created_time`  datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`    varchar(32) COMMENT '更新人',
    `updated_time`  datetime(6) COMMENT '更新时间',
    `project_id`    varchar(32) NOT NULL COMMENT '项目id',
    `member_id`     varchar(32) NOT NULL COMMENT '新增成员的 Id',
    `auditor_id`    varchar(32) NOT NULL COMMENT '审核者 Id',
    `audit_result`  varchar(45) COMMENT '审核结果',
    `audit_comment` varchar(200) COMMENT '审核意见',

    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`project_id`, `member_id`, `auditor_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='成员审核表';


DROP TABLE IF EXISTS `project_flow_template`;
CREATE TABLE `project_flow_template`
(
    `id`           varchar(32) NOT NULL COMMENT '全局唯一标识',
    `graph`        text COMMENT '流程图',
    `name`         varchar(32) DEFAULT NULL COMMENT '模板名称',
    `description`  varchar(32) DEFAULT NULL COMMENT '模板描述',
    `created_by`   varchar(32) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`   varchar(32) DEFAULT NULL COMMENT '更新人',
    `updated_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='流程模板';

-- ----------------------------
-- Table structure for provider_model_params
-- ----------------------------
DROP TABLE IF EXISTS `provider_model_params`;
CREATE TABLE `provider_model_params`
(
    `id`                   varchar(100) NOT NULL COMMENT '全局唯一标识',
    `job_id`               varchar(200) NOT NULL COMMENT 'job Id',
    `task_id`              varchar(200) NOT NULL COMMENT '任务 Id',
    `component_name`       varchar(200) NOT NULL COMMENT '组件名称',
    `role`                 varchar(200) NOT NULL COMMENT '角色',
    `member_id`            varchar(200) NOT NULL COMMENT '成员id',
    `provider_member_id`   varchar(200) NOT NULL COMMENT '数据方成员id',
    `provider_model_param` mediumtext COMMENT '模型参数',
    `created_by`           varchar(100) DEFAULT NULL COMMENT '创建人',
    `updated_by`           varchar(100) DEFAULT NULL COMMENT '修改人',
    `created_time`         datetime(6) NOT NULL COMMENT '创建时间',
    `updated_time`         datetime(6) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据方模型参数';

-- ----------------------------
-- Table structure for task_progress
-- ----------------------------
DROP TABLE IF EXISTS `task_progress`;
CREATE TABLE `task_progress`
(
    `id`                 varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`         varchar(32) COMMENT '创建人',
    `created_time`       datetime(6)  NOT NULL COMMENT '创建时间',
    `updated_by`         varchar(32) COMMENT '更新人',
    `updated_time`       datetime(6) COMMENT '更新时间',
    `project_id`         varchar(32) DEFAULT NULL COMMENT '项目Id',
    `flow_id`            varchar(32)  NOT NULL COMMENT '流程Id',
    `job_id`             varchar(32)  NOT NULL COMMENT '任务Id',
    `role`               varchar(32)  NOT NULL COMMENT '成员角色',
    `flow_node_id`       varchar(32)  NOT NULL COMMENT '任务在流程中的节点Id',
    `task_id`            varchar(100) NOT NULL COMMENT '业务Id',
    `task_type`          varchar(32)  NOT NULL COMMENT '子任务类型 枚举（DataIO/Intersection/HeteroLR...）',
    `expect_work_amount` int(8) COMMENT '预计总工程量',
    `really_work_amount` int(8) COMMENT '实际总工程量',
    `progress`           int(8) COMMENT '进度',
    `progress_rate`      decimal(6, 2) COMMENT '进度百分比',
    `spend`              int(8) COMMENT 'updated_time - created_time，毫秒。',
    `expect_end_time`    datetime(6) COMMENT '预计结束时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique_task_progress` (`task_id`, `role`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务执行进度，与 task 一对一。';

-- ----------------------------
-- Table structure for data_set_task
-- ----------------------------
DROP TABLE IF EXISTS `data_set_task`;
CREATE TABLE `data_set_task`
(
  `id` varchar(32) NOT NULL COMMENT '唯一标识',
  `name` varchar(128) DEFAULT NULL COMMENT '数据集名',
  `data_set_id` varchar(32) DEFAULT NULL COMMENT '数据集id',
  `row_count` bigint(20) DEFAULT NULL COMMENT '数据行数',
  `progress` int(10) DEFAULT NULL COMMENT '任务进度',
  `estimate_time` int(64) DEFAULT NULL COMMENT '预计耗时',
  `repeat_data_count` int(64) DEFAULT NULL COMMENT '重复记录',
  `created_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  DEFAULT CHARSET = utf8mb4 COMMENT ='添加数据集的任务表。';

-- ----------------------------
-- Table structure for chat_last_account
-- ----------------------------
CREATE TABLE `chat_last_account` (
  `id` varchar(100) NOT NULL COMMENT '全局唯一标识',
  `account_id` varchar(100) DEFAULT NULL COMMENT '用户id',
  `member_id` varchar(100) DEFAULT NULL COMMENT '成员id',
  `account_name` varchar(100) DEFAULT NULL COMMENT '用户名称',
  `member_name` varchar(100) DEFAULT NULL COMMENT '成员名称',
  `liaison_member_id` varchar(100) DEFAULT NULL COMMENT '联系人成员id',
  `liaison_account_id` varchar(100) DEFAULT NULL COMMENT '联系人账号id',
  `liaison_account_name` varchar(100) DEFAULT NULL COMMENT '联系人用户名称',
  `liaison_member_name` varchar(100) DEFAULT NULL COMMENT '联系人成员名称',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最近联系人表';

-- ----------------------------
-- Table structure for chat_unread_message
-- ----------------------------
CREATE TABLE `chat_unread_message` (
  `id` varchar(255) NOT NULL COMMENT '全局唯一标识',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `from_member_id` varchar(100) DEFAULT NULL COMMENT '发送方成员id',
  `from_account_id` varchar(100) DEFAULT NULL COMMENT '发送方账号id',
  `to_member_id` varchar(100) DEFAULT NULL COMMENT '接收方成员id',
  `to_account_id` varchar(100) DEFAULT NULL COMMENT '接收方账号id',
  `num` int(10) DEFAULT NULL COMMENT '未读消息数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未读消息汇总表';

-- ----------------------------
-- Table structure for member_chat
-- ----------------------------
CREATE TABLE `member_chat` (
  `id` varchar(100) NOT NULL COMMENT '全局唯一标识',
  `from_account_id` varchar(100) DEFAULT NULL COMMENT '发送方用户id',
  `from_member_id` varchar(100) DEFAULT NULL COMMENT '发送方成员id',
  `to_member_id` varchar(100) DEFAULT NULL COMMENT '接收方成员id',
  `to_account_id` varchar(100) DEFAULT NULL COMMENT '接收方的账号id',
  `created_time` datetime(3) DEFAULT NULL COMMENT '发送时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `content` text COMMENT '消息id',
  `direction` tinyint(4) DEFAULT NULL COMMENT '方向：收：0 或发送：1',
  `status` tinyint(4) DEFAULT NULL COMMENT '状态：（0：已读、1：未读、2、发送成功、3、发送失败）',
  `message_id` varchar(100) DEFAULT NULL COMMENT '消息编号',
  `from_account_name` varchar(100) DEFAULT NULL COMMENT '发送方的账号名称',
  `from_member_name` varchar(100) DEFAULT NULL COMMENT '发送方成员名称',
  `to_account_name` varchar(100) DEFAULT NULL COMMENT '收接方的账号名称',
  `to_member_name` varchar(100) DEFAULT NULL COMMENT '接收方成员名称',
  PRIMARY KEY (`id`),
  KEY `index_from_account_id` (`from_account_id`),
  KEY `index_to_account_id` (`to_account_id`),
  KEY `index_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息明细表';

-- ----------------------------
-- Table structure for notice
-- ----------------------------
CREATE TABLE `notice` (
  `id` varchar(255) NOT NULL COMMENT '全局唯一标识',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `name` varchar(255) DEFAULT NULL COMMENT '通知名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ----------------------------
-- Table structure for notice_unread_message
-- ----------------------------
CREATE TABLE `notice_unread_message` (
  `id` varchar(255) NOT NULL COMMENT '全局唯一标识',
  `created_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `account_id` varchar(255) DEFAULT NULL COMMENT '账号id',
  `message_id` varchar(255) DEFAULT NULL COMMENT '消息id',
  `notice_id` varchar(255) DEFAULT NULL COMMENT '通知id',
  `unread_num` int(11) DEFAULT NULL COMMENT '未读消息数量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未读通知信息表';

SET
FOREIGN_KEY_CHECKS = 1;

SET GLOBAL max_allowed_packet = 1024*1024*32;

ALTER TABLE `wefe_board`.`task_progress` ADD COLUMN `pid_success` INT(8) NULL DEFAULT 0 COMMENT 'spark任务是否成功，1=成功' AFTER `expect_end_time`;

ALTER TABLE `project_flow_template` ADD COLUMN `enname` VARCHAR(45) NULL COMMENT '英文名称' AFTER `name`;

INSERT INTO `project_flow_template` (`id`,`graph`,`name`,`enname`,`description`,`created_by`,`created_time`,`updated_by`,`updated_time`) VALUES ('cc2f6f2a733b48548a317a820fe12131','{\"nodes\":[{\"layoutOrder\":0,\"data\":{\"nodeType\":\"system\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"x\":279,\"y\":150,\"anchorPoints\":[[0.5,1]],\"style\":{\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"id\":\"start\",\"label\":\"开始\",\"singleEdge\":true,\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"DataIO\",\"jsonParams\":false,\"autoSave\":false},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":243,\"y\":243,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987735668056\",\"singleEdge\":true,\"label\":\"选择数据集\",\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Intersection\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":212,\"y\":337,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987757288191\",\"singleEdge\":true,\"label\":\"样本对齐\",\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Segment\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":184,\"y\":433.5,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196030570887798\",\"singleEdge\":true,\"label\":\"数据切割\",\"type\":\"rect-node\"},{\"data\":{\"componentType\":\"HorzSecureBoost\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":140,\"y\":560,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"1619675296228189\",\"singleEdge\":true,\"label\":\"横向XGBoost\",\"type\":\"rect-node\",\"layoutOrder\":0},{\"id\":\"1622541875885621\",\"x\":176,\"y\":650,\"anchorPoints\":[[0.5,0],[0.5,1]],\"singleEdge\":true,\"label\":\"评估模型\",\"data\":{\"componentType\":\"Evaluation\",\"jsonParams\":false,\"autoSave\":true},\"type\":\"rect-node\",\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}}}],\"edges\":[{\"endPoint\":{\"x\":243,\"y\":222.5,\"anchorIndex\":0},\"sourceAnchor\":0,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":279,\"y\":170.5,\"anchorIndex\":0},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"1619598777907116\",\"source\":\"start\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987735668056\"},{\"endPoint\":{\"x\":212,\"y\":316.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":243,\"y\":263.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"16195987793774742\",\"source\":\"16195987735668056\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987757288191\"},{\"endPoint\":{\"x\":184,\"y\":413,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":212,\"y\":357.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"16196030589406312\",\"source\":\"16195987757288191\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196030570887798\"},{\"endPoint\":{\"x\":140,\"y\":539.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":184,\"y\":454,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\"},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196752982662060\",\"source\":\"16196030570887798\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"1619675296228189\"},{\"id\":\"16225418976071951\",\"source\":\"1619675296228189\",\"target\":\"1622541875885621\",\"sourceAnchor\":1,\"targetAnchor\":0,\"label\":\"\",\"type\":\"cubic-edge\",\"style\":{\"edgeState:default\":{\"stroke\":\"#aab7c3\",\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":20,\"endArrow\":true},\"startPoint\":{\"x\":140,\"y\":580.5,\"anchorIndex\":1},\"endPoint\":{\"x\":176,\"y\":629,\"anchorIndex\":0},\"curveOffset\":[-20,20],\"curvePosition\":[0.5,0.5]}],\"combos\":[]}','横向xgb','horz_xgb','横向xgb','35537bb55a1348218d010f07d875ab24','2021-06-01 16:50:40.473000',NULL,NULL);
INSERT INTO `project_flow_template` (`id`,`graph`,`name`,`enname`,`description`,`created_by`,`created_time`,`updated_by`,`updated_time`) VALUES ('e5dd5ef59edf4ae2a21c8d8d4bd3b90b','{\"nodes\":[{\"layoutOrder\":0,\"data\":{\"nodeType\":\"system\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"x\":279,\"y\":150,\"anchorPoints\":[[0.5,1]],\"style\":{\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"id\":\"start\",\"label\":\"开始\",\"singleEdge\":true,\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"type\":\"rect-node\"},{\"data\":{\"componentType\":\"DataIO\",\"jsonParams\":false,\"autoSave\":false},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":219,\"y\":212,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196070742596633\",\"singleEdge\":true,\"label\":\"选择数据集\",\"type\":\"rect-node\",\"layoutOrder\":0},{\"data\":{\"componentType\":\"Intersection\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":160,\"y\":301,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196070752644906\",\"singleEdge\":true,\"label\":\"样本对齐\",\"type\":\"rect-node\",\"layoutOrder\":0},{\"data\":{\"componentType\":\"Segment\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":163,\"y\":403,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196070784837294\",\"singleEdge\":true,\"label\":\"数据切割\",\"type\":\"rect-node\",\"layoutOrder\":0},{\"data\":{\"componentType\":\"VertSecureBoost\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":102,\"y\":509,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"1619607079605235\",\"singleEdge\":true,\"label\":\"纵向XGBoost\",\"type\":\"rect-node\",\"layoutOrder\":0},{\"id\":\"16225416491336596\",\"x\":208,\"y\":534,\"anchorPoints\":[[0.5,0],[0.5,1]],\"singleEdge\":true,\"label\":\"评估模型\",\"data\":{\"componentType\":\"Evaluation\",\"jsonParams\":false,\"autoSave\":true},\"type\":\"rect-node\",\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}}}],\"edges\":[{\"endPoint\":{\"x\":219,\"y\":191.5,\"anchorIndex\":0},\"sourceAnchor\":0,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":279,\"y\":170.5,\"anchorIndex\":0},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"161960708162624\",\"source\":\"start\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196070742596633\"},{\"endPoint\":{\"x\":160,\"y\":280.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":219,\"y\":232.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196070840168287\",\"source\":\"16196070742596633\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196070752644906\"},{\"endPoint\":{\"x\":163,\"y\":382.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":160,\"y\":321.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"1619607094709153\",\"source\":\"16196070752644906\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196070784837294\"},{\"endPoint\":{\"x\":102,\"y\":488.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":163,\"y\":423.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196071043665596\",\"source\":\"16196070784837294\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"1619607079605235\"},{\"id\":\"16225416542085797\",\"source\":\"1619607079605235\",\"target\":\"16225416491336596\",\"sourceAnchor\":1,\"targetAnchor\":0,\"label\":\"\",\"type\":\"cubic-edge\",\"style\":{\"edgeState:default\":{\"stroke\":\"#aab7c3\",\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"}},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":20,\"endArrow\":true},\"startPoint\":{\"x\":102,\"y\":529.5,\"anchorIndex\":1},\"endPoint\":{\"x\":208,\"y\":513,\"anchorIndex\":0},\"curveOffset\":[-20,20],\"curvePosition\":[0.5,0.5]}],\"combos\":[]}','纵向xgb','vert_xgb','纵向xgb','35537bb55a1348218d010f07d875ab24','2021-06-01 16:48:41.107000',NULL,NULL);
INSERT INTO `project_flow_template` (`id`,`graph`,`name`,`enname`,`description`,`created_by`,`created_time`,`updated_by`,`updated_time`) VALUES ('ed4084575e12487d8761104f1de11c80','{\"nodes\":[{\"layoutOrder\":0,\"data\":{\"nodeType\":\"system\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"x\":279,\"y\":150,\"anchorPoints\":[[0.5,1]],\"style\":{\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"id\":\"start\",\"label\":\"开始\",\"singleEdge\":true,\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"DataIO\",\"jsonParams\":false,\"autoSave\":false},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":243,\"y\":243,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987735668056\",\"singleEdge\":true,\"label\":\"选择数据集\",\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Intersection\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":210,\"y\":323,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987757288191\",\"singleEdge\":true,\"label\":\"样本对齐\",\"type\":\"rect-node\"},{\"data\":{\"componentType\":\"Segment\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":201,\"y\":412,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196751191832815\",\"singleEdge\":true,\"label\":\"数据切割\",\"type\":\"rect-node\",\"layoutOrder\":0},{\"data\":{\"componentType\":\"HorzLR\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":175,\"y\":492,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196751401397136\",\"singleEdge\":true,\"label\":\"横向逻辑回归\",\"type\":\"rect-node\",\"layoutOrder\":0},{\"id\":\"16225420030922147\",\"x\":243,\"y\":589,\"anchorPoints\":[[0.5,0],[0.5,1]],\"singleEdge\":true,\"label\":\"评估模型\",\"data\":{\"componentType\":\"Evaluation\",\"jsonParams\":false,\"autoSave\":true},\"type\":\"rect-node\",\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}}}],\"edges\":[{\"endPoint\":{\"x\":243,\"y\":222.5,\"anchorIndex\":0},\"sourceAnchor\":0,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":279,\"y\":170.5,\"anchorIndex\":0},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"1619598777907116\",\"source\":\"start\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987735668056\"},{\"endPoint\":{\"x\":210,\"y\":302.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":243,\"y\":263.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"16195987793774742\",\"source\":\"16195987735668056\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987757288191\"},{\"endPoint\":{\"x\":201,\"y\":391.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":210,\"y\":343.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\"},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196751304774083\",\"source\":\"16195987757288191\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196751191832815\"},{\"endPoint\":{\"x\":175,\"y\":471.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":201,\"y\":432.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\"},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196751423124660\",\"source\":\"16196751191832815\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196751401397136\"},{\"id\":\"16225420064161251\",\"source\":\"16196751401397136\",\"target\":\"16225420030922147\",\"sourceAnchor\":1,\"targetAnchor\":0,\"label\":\"\",\"type\":\"cubic-edge\",\"style\":{\"edgeState:default\":{\"stroke\":\"#aab7c3\",\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"}},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":20,\"endArrow\":true},\"startPoint\":{\"x\":175,\"y\":512.5,\"anchorIndex\":1},\"endPoint\":{\"x\":243,\"y\":568,\"anchorIndex\":0},\"curveOffset\":[-20,20],\"curvePosition\":[0.5,0.5]}],\"combos\":[]}','横向LR','horz_lr','横向LR','35537bb55a1348218d010f07d875ab24','2021-06-01 16:49:30.406000',NULL,NULL);
INSERT INTO `project_flow_template` (`id`,`graph`,`name`,`enname`,`description`,`created_by`,`created_time`,`updated_by`,`updated_time`) VALUES ('fb6f02ac8ab1472fa49bcf8067f6b552','{\"nodes\":[{\"layoutOrder\":0,\"data\":{\"nodeType\":\"system\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"x\":279,\"y\":150,\"anchorPoints\":[[0.5,1]],\"style\":{\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"id\":\"start\",\"label\":\"开始\",\"singleEdge\":true,\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"DataIO\",\"jsonParams\":false,\"autoSave\":false},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":243,\"y\":243,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987735668056\",\"singleEdge\":true,\"label\":\"选择数据集\",\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Intersection\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":212,\"y\":337,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987757288191\",\"singleEdge\":true,\"label\":\"样本对齐\",\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Segment\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":184,\"y\":433.5,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196030570887798\",\"singleEdge\":true,\"label\":\"数据切割\",\"type\":\"rect-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"VertLR\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":123,\"y\":513,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196034768611638\",\"singleEdge\":true,\"label\":\"纵向逻辑回归\",\"type\":\"rect-node\"},{\"id\":\"16225420809312593\",\"x\":259,\"y\":595,\"anchorPoints\":[[0.5,0],[0.5,1]],\"singleEdge\":true,\"label\":\"评估模型\",\"data\":{\"componentType\":\"Evaluation\",\"jsonParams\":false,\"autoSave\":true},\"type\":\"rect-node\",\"style\":{\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}}}],\"edges\":[{\"endPoint\":{\"x\":243,\"y\":222.5,\"anchorIndex\":0},\"sourceAnchor\":0,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":279,\"y\":170.5,\"anchorIndex\":0},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"1619598777907116\",\"source\":\"start\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987735668056\"},{\"endPoint\":{\"x\":212,\"y\":316.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":243,\"y\":263.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"16195987793774742\",\"source\":\"16195987735668056\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987757288191\"},{\"endPoint\":{\"x\":184,\"y\":413,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":212,\"y\":357.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"16196030589406312\",\"source\":\"16195987757288191\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196030570887798\"},{\"endPoint\":{\"x\":123,\"y\":492.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":184,\"y\":454,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\"},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196034786442552\",\"source\":\"16196030570887798\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196034768611638\"},{\"id\":\"16225420841924398\",\"source\":\"16196034768611638\",\"target\":\"16225420809312593\",\"sourceAnchor\":1,\"targetAnchor\":0,\"label\":\"\",\"type\":\"cubic-edge\",\"style\":{\"edgeState:default\":{\"stroke\":\"#aab7c3\",\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"}},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"lineAppendWidth\":20,\"endArrow\":true},\"startPoint\":{\"x\":123,\"y\":533.5,\"anchorIndex\":1},\"endPoint\":{\"x\":259,\"y\":574,\"anchorIndex\":0},\"curveOffset\":[-20,20],\"curvePosition\":[0.5,0.5]}],\"combos\":[]}','纵向LR','vert_lr','纵向LR','35537bb55a1348218d010f07d875ab24','2021-06-01 16:47:45.605000',NULL,NULL);

ALTER TABLE `project_flow_template` ADD COLUMN `federated_learning_type` VARCHAR(32) NULL COMMENT '联邦学习类型' AFTER `description`;

UPDATE `project_flow_template` SET `federated_learning_type` = 'horizontal' WHERE (`id` = 'cc2f6f2a733b48548a317a820fe12131');
UPDATE `project_flow_template` SET `federated_learning_type` = 'horizontal' WHERE (`id` = 'ed4084575e12487d8761104f1de11c80');
UPDATE `project_flow_template` SET `federated_learning_type` = 'vertical' WHERE (`id` = 'e5dd5ef59edf4ae2a21c8d8d4bd3b90b');
UPDATE `project_flow_template` SET `federated_learning_type` = 'vertical' WHERE (`id` = 'fb6f02ac8ab1472fa49bcf8067f6b552');

