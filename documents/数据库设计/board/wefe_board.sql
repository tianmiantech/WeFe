/*
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
    `id`                    varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`            varchar(32) COMMENT '创建人',
    `created_time`          datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`            varchar(32) COMMENT '更新人',
    `updated_time`          datetime(6) COMMENT '更新时间',
    `phone_number`          varchar(200) NOT NULL COMMENT '手机号',
    `password`              varchar(128) NOT NULL COMMENT '密码',
    `salt`                  varchar(128) NOT NULL COMMENT '盐',
    `nickname`              varchar(32)  NOT NULL COMMENT '昵称',
    `email`                 varchar(128) NOT NULL COMMENT '邮箱',
    `super_admin_role`      tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是超级管理员 超级管理员通常是第一个创建并初始化系统的那个人',
    `admin_role`            tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是管理员 管理员有更多权限，比如设置 member 是否对外可见。',
    `audit_status`          varchar(32)  NOT NULL COMMENT '审核状态',
    `audit_comment`         varchar(512) COMMENT '审核意见',
    `enable`                tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可用',
    `cancelled`             tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已注销',
    `last_action_time`      datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP (6) COMMENT '最后活动时间',
    `history_password_list` text NULL COMMENT '历史曾用密码',
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_unique_phonenumber` (`phone_number`),
    KEY                     `idx_create_time` (`created_time`)
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
    `created_time`        datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time`   datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `id`                       varchar(32)   NOT NULL COMMENT '全局唯一标识',
    `created_by`               varchar(32) COMMENT '创建人',
    `created_time`             datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`               varchar(32) COMMENT '更新人',
    `updated_time`             datetime(6) COMMENT '更新时间',
    `name`                     varchar(128)  NOT NULL COMMENT '数据集名称',
    `tags`                     varchar(128) COMMENT '标签',
    `description`              varchar(3072) COMMENT '描述',
    `storage_type`             varchar(32) COMMENT '存储类型',
    `namespace`                varchar(1000) NOT NULL COMMENT '命名空间',
    `table_name`               varchar(1000) NOT NULL COMMENT '表名',
    `row_count`                bigint(20) NOT NULL COMMENT '数据行数',
    `primary_key_column`       varchar(32)   NOT NULL COMMENT '主键字段',
    `column_count`             int(11) NOT NULL COMMENT '数据集列数',
    `column_name_list`         text          NOT NULL COMMENT '数据集字段列表',
    `feature_count`            int(11) NOT NULL COMMENT '特征数量',
    `feature_name_list`        text COMMENT '特征列表',
    `contains_y`               tinyint(1) NOT NULL COMMENT '是否包含 Y 值',
    `y_count`                  int(11) NOT NULL COMMENT 'y列的数量',
    `y_name_list`              text COMMENT 'y列名称列表',
    `public_level`             varchar(32) COMMENT '数据集的可见性',
    `public_member_list`       varchar(3072) COMMENT '可见成员列表 只有在列表中的联邦成员才可以看到该数据集的基本信息',
    `usage_count_in_job`       int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    `usage_count_in_flow`      int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    `usage_count_in_project`   int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    `source_type`              varchar(32) COMMENT '来源类型，枚举（原始、对齐、分箱）',
    `source_flow_id`           varchar(64) COMMENT '来源流程id',
    `source_job_id`            varchar(64) COMMENT '来源任务id',
    `source_task_id`           varchar(100) COMMENT '来源子任务id',
    `y_positive_example_count` bigint(20) COMMENT '正例数量',
    `y_positive_example_ratio` double(10, 4
) COMMENT '正例比例',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据集';


-- ----------------------------
-- Table structure for data_source
-- ----------------------------
DROP TABLE IF EXISTS `data_source`;
CREATE TABLE `data_source`
(
    `database_type` varchar(255) COMMENT '数据库类型',
    `host`          varchar(255) COMMENT '数据库ip',
    `port`          int(255) COMMENT '数据库端口',
    `database_name` varchar(255) COMMENT '数据库名',
    `user_name`     varchar(255) COMMENT '数据库用户名',
    `password`      varchar(255) COMMENT '数据库密码',
    `id`            varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_time`  datetime(0) COMMENT '创建时间',
    `updated_time`  datetime(0) COMMENT '更新时间',
    `name`          varchar(255) COMMENT '记录名',
    `created_by`    varchar(255) COMMENT '创建者',
    `updated_by`    varchar(255) COMMENT '更新者',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据源';


-- ----------------------------
-- Table structure for data_set_column
-- ----------------------------
DROP TABLE IF EXISTS `data_set_column`;
CREATE TABLE `data_set_column`
(
    `id`                 varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`         varchar(32) COMMENT '创建人',
    `created_time`       datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time` datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time` datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
-- Table structure for job
-- ----------------------------
DROP TABLE IF EXISTS `job`;
CREATE TABLE `job`
(
    `id`                       varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`               varchar(32) COMMENT '创建人',
    `created_time`             datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time` datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time` datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time` datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    PRIMARY KEY (`id`),
    KEY              `idx_query_by_interface` (`log_interface`, `operator_id`, `created_time`) USING BTREE,
    KEY              `idx_query_by_operator` (`operator_id`, `log_interface`, `created_time`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户操作日志';


-- ----------------------------
-- Table structure for task
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task`
(
    `id`                  varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32) COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `task_conf`           longtext     NOT NULL COMMENT '任务conf_json',
    `status`              varchar(32)  NOT NULL COMMENT '状态 枚举（created/running/canceled/success/error）',
    `start_time`          datetime(6) COMMENT '开始时间',
    `finish_time`         datetime(6) COMMENT '结束时间',
    `spend`               int(11) COMMENT '执行耗时',
    `message`             varchar(3072) COMMENT '消息备注 失败原因/备注',
    `error_cause`         text COMMENT '发生错误的详细原因，通常是堆栈信息。',
    `position`            int(11) COMMENT 'task执行顺序',
    `pid`                 int(11) COMMENT '进程号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique_task` (`task_id`, `role`),
    KEY                   `index_job_id__role` (`job_id`,`role`) USING BTREE
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
    `created_time`   datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    KEY              `idx_create_time` (`created_time`),
    KEY              `index_project_serving_model`(`project_id`, `serving_model`, `flow_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='task 执行结果';

--
-- 项目表 project
--
DROP TABLE IF EXISTS `project`;
CREATE TABLE `project`
(
    `id`                       varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`               varchar(32) COMMENT '创建人',
    `created_time`             datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `exited_by`                varchar(32) NULL COMMENT '退出项目的操作者',
    `exited_time`              datetime(6) NULL COMMENT '退出时间',
    `closed`                   tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已关闭',
    `closed_by`                varchar(32) NULL COMMENT '关闭者',
    `closed_time`              datetime(6) NULL COMMENT '关闭时间',
    `flow_status_statistics`   varchar(512) COMMENT '流程状态统计',
    `project_type`             varchar(36)  NOT NULL DEFAULT 'MachineLearning' COMMENT '项目类型',
    `top`                      tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `sort_num`                 int          NOT NULL DEFAULT 0 COMMENT '排序序号',
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
    `created_time`        datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `data_set_type`       varchar(36) NOT NULL DEFAULT 'TableDataSet' COMMENT '数据集类型',
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
    `created_time`             datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time`            datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`              varchar(32) COMMENT '更新人',
    `updated_time`            datetime(6) COMMENT '更新时间',
    `deleted`                 tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除',
    `project_id`              varchar(32)  NOT NULL COMMENT '项目ID',
    `flow_id`                 varchar(36)  NOT NULL COMMENT '流程ID',
    `flow_status`             varchar(32)  NOT NULL COMMENT '流程状态',
    `federated_learning_type` varchar(32)  NOT NULL COMMENT '联邦任务类型（横向/纵向）',
    `deep_learning_job_type`  varchar(32) COMMENT '深度学习任务类型（classify/detection）',
    `status_updated_time`     datetime(6) COMMENT '状态更新时间',
    `message`                 text COMMENT '相关消息',
    `my_role`                 varchar(32)  NOT NULL COMMENT '我方角色',
    `flow_name`               varchar(256) NOT NULL COMMENT '流程名称',
    `flow_desc`               text COMMENT '流程描述',
    `graph`                   longtext COMMENT '画布中编辑的图',
    `creator_member_id`       varchar(36) COMMENT '创建此流程的成员的ID',
    `top`                     tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `sort_num`                int          NOT NULL DEFAULT 0 COMMENT '排序序号',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique` (`flow_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='项目流程';


DROP TABLE IF EXISTS `project_flow_node`;
CREATE TABLE `project_flow_node`
(
    `id`                  varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32) COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time`  datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `id`                      varchar(32) NOT NULL COMMENT '全局唯一标识',
    `graph`                   text COMMENT '流程图',
    `name`                    varchar(32) DEFAULT NULL COMMENT '模板名称',
    `enname`                  VARCHAR(45) NULL '模板英文名称',
    `description`             varchar(32) DEFAULT NULL COMMENT '模板描述',
    `federated_learning_type` VARCHAR(32) NULL COMMENT '联邦任务类型（横向/纵向）',
    `created_by`              varchar(32) DEFAULT NULL COMMENT '创建人',
    `created_time`            datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`              varchar(32) DEFAULT NULL COMMENT '更新人',
    `updated_time`            datetime(6) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='流程模板';

INSERT INTO `project_flow_template` (`id`, `graph`, `name`, `enname`, `description`, `created_by`, `created_time`,
                                     `updated_by`, `updated_time`)
VALUES ('cc2f6f2a733b48548a317a820fe12131',
        '{\"nodes\":[{\"layoutOrder\":0,\"data\":{\"nodeType\":\"system\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"x\":509,\"y\":164,\"anchorPoints\":[[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"id\":\"start\",\"label\":\"开始\",\"singleEdge\":true,\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"DataIO\",\"jsonParams\":false,\"autoSave\":false},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":511,\"y\":253,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987735668056\",\"singleEdge\":true,\"label\":\"选择数据集\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Segment\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":510,\"y\":374.5,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196030570887798\",\"singleEdge\":true,\"label\":\"数据切割\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"HorzSecureBoost\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":512,\"y\":502,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"1619675296228189\",\"singleEdge\":true,\"label\":\"横向XGBoost\",\"type\":\"flow-node\"},{\"data\":{\"componentType\":\"Evaluation\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":512,\"y\":596,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"1622541875885621\",\"singleEdge\":true,\"label\":\"评估模型\",\"type\":\"flow-node\"}],\"edges\":[{\"endPoint\":{\"x\":511,\"y\":232.5,\"anchorIndex\":0},\"sourceAnchor\":0,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":509,\"y\":184.5,\"anchorIndex\":0},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"1619598777907116\",\"source\":\"start\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987735668056\"},{\"endPoint\":{\"x\":512,\"y\":481.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":510,\"y\":395,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196752982662060\",\"source\":\"16196030570887798\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"1619675296228189\"},{\"endPoint\":{\"x\":512,\"y\":575.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":512,\"y\":522.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":20,\"endArrow\":true},\"id\":\"16225418976071951\",\"source\":\"1619675296228189\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"1622541875885621\"},{\"id\":\"16323784729993608\",\"source\":\"16195987735668056\",\"target\":\"16196030570887798\",\"sourceAnchor\":1,\"targetAnchor\":0,\"label\":\"\",\"type\":\"flow-edge\",\"style\":{\"edgeState:default\":{\"stroke\":\"#aab7c3\",\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":20,\"endArrow\":true},\"startPoint\":{\"x\":511,\"y\":273.5,\"anchorIndex\":1},\"endPoint\":{\"x\":510,\"y\":354,\"anchorIndex\":0},\"curveOffset\":[-20,20],\"curvePosition\":[0.5,0.5]}],\"combos\":[]}',
        '横向xgb', 'horz_xgb', '横向xgb', '35537bb55a1348218d010f07d875ab24', '2021-06-01 16:50:40.473000', NULL, NULL),
       ('e5dd5ef59edf4ae2a21c8d8d4bd3b90b',
        '{\"nodes\":[{\"layoutOrder\":0,\"data\":{\"nodeType\":\"system\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"x\":166,\"y\":127,\"anchorPoints\":[[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"id\":\"start\",\"label\":\"开始\",\"singleEdge\":true,\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"DataIO\",\"jsonParams\":false,\"autoSave\":false},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":165,\"y\":208,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196070742596633\",\"singleEdge\":true,\"label\":\"选择数据集\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Intersection\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":164,\"y\":302,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196070752644906\",\"singleEdge\":true,\"label\":\"样本对齐\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Segment\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":169,\"y\":403,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196070784837294\",\"singleEdge\":true,\"label\":\"数据切割\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"VertSecureBoost\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":168,\"y\":510,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"1619607079605235\",\"singleEdge\":true,\"label\":\"纵向XGBoost\",\"type\":\"flow-node\"},{\"data\":{\"componentType\":\"Evaluation\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":173,\"y\":601,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16225416491336596\",\"singleEdge\":true,\"label\":\"评估模型\",\"type\":\"flow-node\"}],\"edges\":[{\"endPoint\":{\"x\":165,\"y\":187.5,\"anchorIndex\":0},\"sourceAnchor\":0,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":166,\"y\":147.5,\"anchorIndex\":0},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"161960708162624\",\"source\":\"start\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196070742596633\"},{\"endPoint\":{\"x\":164,\"y\":281.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":165,\"y\":228.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196070840168287\",\"source\":\"16196070742596633\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196070752644906\"},{\"endPoint\":{\"x\":169,\"y\":382.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":164,\"y\":322.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"1619607094709153\",\"source\":\"16196070752644906\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196070784837294\"},{\"endPoint\":{\"x\":168,\"y\":489.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":169,\"y\":423.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196071043665596\",\"source\":\"16196070784837294\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"1619607079605235\"},{\"endPoint\":{\"x\":173,\"y\":580.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":168,\"y\":530.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":20,\"endArrow\":true},\"id\":\"16225416542085797\",\"source\":\"1619607079605235\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16225416491336596\"}],\"combos\":[]}',
        '纵向xgb', 'vert_xgb', '纵向xgb', '35537bb55a1348218d010f07d875ab24', '2021-06-01 16:48:41.107000', NULL, NULL),
       ('ed4084575e12487d8761104f1de11c80',
        '{\"nodes\":[{\"layoutOrder\":0,\"data\":{\"nodeType\":\"system\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"x\":213,\"y\":149,\"anchorPoints\":[[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"id\":\"start\",\"label\":\"开始\",\"singleEdge\":true,\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"DataIO\",\"jsonParams\":false,\"autoSave\":false},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":213,\"y\":236,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987735668056\",\"singleEdge\":true,\"label\":\"选择数据集\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Segment\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":209,\"y\":342,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196751191832815\",\"singleEdge\":true,\"label\":\"数据切割\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"HorzLR\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":212,\"y\":492,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196751401397136\",\"singleEdge\":true,\"label\":\"横向逻辑回归\",\"type\":\"flow-node\"},{\"data\":{\"componentType\":\"Evaluation\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":211,\"y\":586,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}},\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"lineWidth\":1},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16225420030922147\",\"singleEdge\":true,\"label\":\"评估模型\",\"type\":\"flow-node\"}],\"edges\":[{\"endPoint\":{\"x\":213,\"y\":215.5,\"anchorIndex\":0},\"sourceAnchor\":0,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":213,\"y\":169.5,\"anchorIndex\":0},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"1619598777907116\",\"source\":\"start\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987735668056\"},{\"endPoint\":{\"x\":212,\"y\":471.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":209,\"y\":362.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\"},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196751423124660\",\"source\":\"16196751191832815\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196751401397136\"},{\"endPoint\":{\"x\":211,\"y\":565.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":212,\"y\":512.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":20,\"endArrow\":true},\"id\":\"16225420064161251\",\"source\":\"16196751401397136\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16225420030922147\"},{\"id\":\"16323801145191532\",\"source\":\"16195987735668056\",\"target\":\"16196751191832815\",\"sourceAnchor\":1,\"targetAnchor\":0,\"label\":\"\",\"type\":\"flow-edge\",\"style\":{\"edgeState:default\":{\"stroke\":\"#aab7c3\",\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"}},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":20,\"endArrow\":true},\"startPoint\":{\"x\":213,\"y\":256.5,\"anchorIndex\":1},\"endPoint\":{\"x\":209,\"y\":321.5,\"anchorIndex\":0},\"curveOffset\":[-20,20],\"curvePosition\":[0.5,0.5]}],\"combos\":[]}',
        '横向LR', 'horz_lr', '横向LR', '35537bb55a1348218d010f07d875ab24', '2021-06-01 16:49:30.406000', NULL, NULL),
       ('fb6f02ac8ab1472fa49bcf8067f6b552',
        '{\"nodes\":[{\"layoutOrder\":0,\"data\":{\"nodeType\":\"system\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"x\":185,\"y\":154,\"anchorPoints\":[[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"id\":\"start\",\"label\":\"开始\",\"singleEdge\":true,\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"DataIO\",\"jsonParams\":false,\"autoSave\":false},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":184,\"y\":246,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987735668056\",\"singleEdge\":true,\"label\":\"选择数据集\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Intersection\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":185,\"y\":334,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16195987757288191\",\"singleEdge\":true,\"label\":\"样本对齐\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"Segment\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":184,\"y\":433.5,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196030570887798\",\"singleEdge\":true,\"label\":\"数据切割\",\"type\":\"flow-node\"},{\"layoutOrder\":0,\"data\":{\"componentType\":\"VertLR\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":186,\"y\":509,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16196034768611638\",\"singleEdge\":true,\"label\":\"纵向逻辑回归\",\"type\":\"flow-node\"},{\"data\":{\"componentType\":\"Evaluation\",\"jsonParams\":false,\"autoSave\":true},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}},\"x\":186,\"y\":589,\"anchorPoints\":[[0.5,0],[0.5,1]],\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"id\":\"16225420809312593\",\"singleEdge\":true,\"label\":\"评估模型\",\"type\":\"flow-node\"}],\"edges\":[{\"endPoint\":{\"x\":184,\"y\":225.5,\"anchorIndex\":0},\"sourceAnchor\":0,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":185,\"y\":174.5,\"anchorIndex\":0},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"1619598777907116\",\"source\":\"start\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987735668056\"},{\"endPoint\":{\"x\":185,\"y\":313.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":184,\"y\":266.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"16195987793774742\",\"source\":\"16195987735668056\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16195987757288191\"},{\"endPoint\":{\"x\":184,\"y\":413,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":185,\"y\":354.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true,\"highlight\":{\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500},\"stroke\":\"rgb(95, 149, 255)\",\"shadowColor\":\"rgb(95, 149, 255)\",\"lineWidth\":2}},\"id\":\"16196030589406312\",\"source\":\"16195987757288191\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196030570887798\"},{\"endPoint\":{\"x\":186,\"y\":488.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":184,\"y\":454,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":10,\"endArrow\":true},\"id\":\"16196034786442552\",\"source\":\"16196030570887798\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16196034768611638\"},{\"endPoint\":{\"x\":186,\"y\":568.5,\"anchorIndex\":0},\"sourceAnchor\":1,\"targetAnchor\":0,\"curvePosition\":[0.5,0.5],\"curveOffset\":[-20,20],\"startPoint\":{\"x\":186,\"y\":529.5,\"anchorIndex\":1},\"style\":{\"edgeState:default\":{\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"stroke\":\"#aab7c3\",\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":20,\"endArrow\":true},\"id\":\"16225420841924398\",\"source\":\"16196034768611638\",\"label\":\"\",\"type\":\"cubic-edge\",\"target\":\"16225420809312593\"}],\"combos\":[]}',
        '纵向LR', 'vert_lr', '纵向LR', '35537bb55a1348218d010f07d875ab24', '2021-06-01 16:47:45.605000', NULL, NULL);

UPDATE `project_flow_template`
SET `federated_learning_type` = 'horizontal'
WHERE (`id` = 'cc2f6f2a733b48548a317a820fe12131');
UPDATE `project_flow_template`
SET `federated_learning_type` = 'horizontal'
WHERE (`id` = 'ed4084575e12487d8761104f1de11c80');
UPDATE `project_flow_template`
SET `federated_learning_type` = 'vertical'
WHERE (`id` = 'e5dd5ef59edf4ae2a21c8d8d4bd3b90b');
UPDATE `project_flow_template`
SET `federated_learning_type` = 'vertical'
WHERE (`id` = 'fb6f02ac8ab1472fa49bcf8067f6b552');

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
    `created_time`         datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `created_time`       datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
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
    `pid_success`        INT(8) NULL DEFAULT 0 COMMENT 'spark任务是否成功，1=成功',
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
    `id`                  varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32) COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`          varchar(32) COMMENT '更新人',
    `updated_time`        datetime(6) COMMENT '更新时间',
    `data_set_name`       varchar(128) DEFAULT NULL COMMENT '数据集名',
    `data_set_id`         varchar(32)  DEFAULT NULL COMMENT '数据集id',
    `total_row_count`     bigint(20) DEFAULT NULL COMMENT '总数据行数',
    `added_row_count`     bigint(20) DEFAULT NULL COMMENT '已写入数据行数',
    `progress`            int(10) DEFAULT NULL COMMENT '任务进度百分比',
    `estimate_time`       int(64) DEFAULT NULL COMMENT '预计剩余耗时',
    `repeat_id_row_count` int(64) DEFAULT NULL COMMENT '主键重复条数',
    `error_message`       text         DEFAULT NULL COMMENT '错误消息',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique_data_set_task` (`data_set_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  DEFAULT CHARSET = utf8mb4 COMMENT ='添加数据集的任务表。';

-- ----------------------------
-- Table structure for chat_last_account
-- ----------------------------
DROP TABLE IF EXISTS `chat_last_account`;
CREATE TABLE `chat_last_account`
(
    `id`                   varchar(100) NOT NULL COMMENT '全局唯一标识',
    `account_id`           varchar(100) DEFAULT NULL COMMENT '用户id',
    `member_id`            varchar(100) DEFAULT NULL COMMENT '成员id',
    `account_name`         varchar(100) DEFAULT NULL COMMENT '用户名称',
    `member_name`          varchar(100) DEFAULT NULL COMMENT '成员名称',
    `liaison_member_id`    varchar(100) DEFAULT NULL COMMENT '联系人成员id',
    `liaison_account_id`   varchar(100) DEFAULT NULL COMMENT '联系人账号id',
    `liaison_account_name` varchar(100) DEFAULT NULL COMMENT '联系人用户名称',
    `liaison_member_name`  varchar(100) DEFAULT NULL COMMENT '联系人成员名称',
    `created_time`         datetime     DEFAULT NULL COMMENT '创建时间',
    `updated_time`         datetime     DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='最近联系人表';

-- ----------------------------
-- Table structure for chat_unread_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_unread_message`;
CREATE TABLE `chat_unread_message`
(
    `id`              varchar(255) NOT NULL COMMENT '全局唯一标识',
    `created_time`    datetime     DEFAULT NULL COMMENT '创建时间',
    `updated_time`    datetime     DEFAULT NULL COMMENT '更新时间',
    `from_member_id`  varchar(100) DEFAULT NULL COMMENT '发送方成员id',
    `from_account_id` varchar(100) DEFAULT NULL COMMENT '发送方账号id',
    `to_member_id`    varchar(100) DEFAULT NULL COMMENT '接收方成员id',
    `to_account_id`   varchar(100) DEFAULT NULL COMMENT '接收方账号id',
    `num`             int(10) DEFAULT NULL COMMENT '未读消息数量',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='未读消息汇总表';

-- ----------------------------
-- Table structure for member_chat
-- ----------------------------
DROP TABLE IF EXISTS `member_chat`;
CREATE TABLE `member_chat`
(
    `id`                varchar(100) NOT NULL COMMENT '全局唯一标识',
    `from_account_id`   varchar(100) DEFAULT NULL COMMENT '发送方用户id',
    `from_member_id`    varchar(100) DEFAULT NULL COMMENT '发送方成员id',
    `to_member_id`      varchar(100) DEFAULT NULL COMMENT '接收方成员id',
    `to_account_id`     varchar(100) DEFAULT NULL COMMENT '接收方的账号id',
    `created_time`      datetime(3) DEFAULT NULL COMMENT '发送时间',
    `updated_time`      datetime     DEFAULT NULL COMMENT '更新时间',
    `content`           text COMMENT '消息id',
    `direction`         tinyint(4) DEFAULT NULL COMMENT '方向：收：0 或发送：1',
    `status`            tinyint(4) DEFAULT NULL COMMENT '状态：（0：已读、1：未读、2、发送成功、3、发送失败）',
    `message_id`        varchar(100) DEFAULT NULL COMMENT '消息编号',
    `from_account_name` varchar(100) DEFAULT NULL COMMENT '发送方的账号名称',
    `from_member_name`  varchar(100) DEFAULT NULL COMMENT '发送方成员名称',
    `to_account_name`   varchar(100) DEFAULT NULL COMMENT '收接方的账号名称',
    `to_member_name`    varchar(100) DEFAULT NULL COMMENT '接收方成员名称',
    PRIMARY KEY (`id`),
    KEY                 `index_from_account_id` (`from_account_id`),
    KEY                 `index_to_account_id` (`to_account_id`),
    KEY                 `index_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息明细表';

DROP TABLE IF EXISTS `model_oot_record`;
CREATE TABLE `model_oot_record`
(
    `id`                     varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`             varchar(32) DEFAULT NULL COMMENT '创建人',
    `created_time`           datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`             varchar(32) DEFAULT NULL COMMENT '更新人',
    `updated_time`           datetime(6) DEFAULT NULL COMMENT '更新时间',
    `flow_id`                varchar(32) NOT NULL COMMENT '流程ID',
    `oot_job_id`             varchar(32) NOT NULL COMMENT '被oot的任务ID',
    `oot_model_flow_node_id` varchar(64) NOT NULL COMMENT '被oot的模型节点id',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型打分验证记录表';

-- ----------------------------
-- oot 模板
-- ----------------------------
INSERT INTO `project_flow_template`
VALUES ('70a504c115504b01910fb766d62fe016',
        '{\"nodes\":[{\"id\":\"start\",\"label\":\"开始\",\"x\":101,\"y\":150,\"anchorPoints\":[[0.5,1]],\"singleEdge\":true,\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"data\":{\"nodeType\":\"system\"},\"type\":\"flow-node\"},{\"id\":\"16296886125199914\",\"x\":101,\"y\":260,\"anchorPoints\":[[0.5,0],[0.5,1]],\"singleEdge\":true,\"label\":\"打分验证\",\"data\":{\"componentType\":\"Oot\"},\"type\":\"flow-node\",\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}}}],\"edges\":[{\"id\":\"16296886317753166\",\"source\":\"start\",\"target\":\"16296886125199914\",\"sourceAnchor\":0,\"targetAnchor\":0,\"label\":\"\",\"type\":\"flow-edge\",\"style\":{\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2,\"shadowColor\":\"rgb(95, 149, 255)\",\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500}},\"highlight\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2,\"text-shape\":{\"fontWeight\":500}},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"edgeState:default\":{\"stroke\":\"#aab7c3\",\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":20,\"endArrow\":true},\"startPoint\":{\"x\":101,\"y\":170.5,\"anchorIndex\":0},\"endPoint\":{\"x\":101,\"y\":239.5,\"anchorIndex\":0},\"curveOffset\":[-20,20],\"curvePosition\":[0.5,0.5]}],\"combos\":[]}',
        '打分验证', 'oot', '新建流程模板不显示，只有模型列表使用', 'vertical', '35537bb55a1348218d010f07d875ab24', '2021-6-1 16:47:45', NULL,
        NULL);

-- ----------------------------
-- Table structure for global_config
-- ----------------------------
DROP TABLE IF EXISTS `global_config`;
CREATE TABLE `global_config`
(
    `id`           varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`   varchar(32) COMMENT '创建人',
    `created_time` datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`   varchar(32) COMMENT '更新人',
    `updated_time` datetime(6) COMMENT '更新时间',
    `group`        varchar(32) COMMENT '配置项所在的组',
    `name`         varchar(32) COMMENT '配置项名称',
    `value`        longtext COMMENT '配置项的值',
    `comment`      text COMMENT '配置项的解释说明',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique_group_name`(`group`,`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='全局设置';


SET
FOREIGN_KEY_CHECKS = 1;

set
global max_allowed_packet = 1024*1024*32;


-- ↓ v3.0.0 ↓ 2021年11月05日 ↓

DROP TABLE IF EXISTS `data_resource`;
CREATE TABLE `data_resource`
(
    `id`                      varchar(32)   NOT NULL COMMENT '全局唯一标识',
    `created_by`              varchar(32) COMMENT '创建人',
    `created_time`            datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`              varchar(32) COMMENT '更新人',
    `updated_time`            datetime(6) COMMENT '更新时间',
    `name`                    varchar(256)  NOT NULL COMMENT '资源名称',
    `data_resource_type`      varchar(32)   NOT NULL COMMENT '资源类型',
    `description`             varchar(3072) COMMENT '描述',
    `tags`                    varchar(128) COMMENT '标签',
    `storage_type`            varchar(32) COMMENT '存储类型',
    `storage_namespace`       varchar(1000) NOT NULL COMMENT '资源在存储中的命名空间（库名、目录路径）',
    `storage_resource_name`   varchar(1000) COMMENT '资源在存储中的名称（表名、文件名）',
    `total_data_count`        bigint(20) NOT NULL COMMENT '总数据量',
    `public_level`            varchar(32) COMMENT '资源的可见性',
    `public_member_list`      varchar(3072) COMMENT '可见成员列表 只有在列表中的联邦成员才可以看到该资源的基本信息',
    `usage_count_in_job`      int(11) NOT NULL DEFAULT '0' COMMENT '该资源在多少个job中被使用',
    `usage_count_in_flow`     int(11) NOT NULL DEFAULT '0' COMMENT '该资源在多少个flow中被使用',
    `usage_count_in_project`  int(11) NOT NULL DEFAULT '0' COMMENT '该资源在多少个project中被使用',
    `usage_count_in_member`   int(11) NOT NULL DEFAULT '0' COMMENT '该资源被多少个其他成员被使用',
    `derived_resource`        bool default false comment '是否是衍生资源',
    `derived_from`            varchar(32) COMMENT '衍生来源，枚举（原始、对齐、分箱）',
    `derived_from_flow_id`    varchar(64) COMMENT '衍生来源流程id',
    `derived_from_job_id`     varchar(64) COMMENT '衍生来源任务id',
    `derived_from_task_id`    varchar(100) COMMENT '衍生来源子任务id',
    `statistical_information` longtext COMMENT '该数据资源相关的统计信息',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据资源';

DROP TABLE IF EXISTS `data_resource_upload_task`;
CREATE TABLE `data_resource_upload_task`
(
    `id`                      varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`              varchar(32) COMMENT '创建人',
    `created_time`            datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`              varchar(32) COMMENT '更新人',
    `updated_time`            datetime(6) COMMENT '更新时间',
    `data_resource_id`        varchar(32)  DEFAULT NULL COMMENT '数据资源id',
    `data_resource_name`      varchar(128) DEFAULT NULL COMMENT '数据资源名称',
    `data_resource_type`      varchar(32) COMMENT '资源类型',
    `total_data_count`        bigint(20) DEFAULT NULL COMMENT '总数据行数',
    `completed_data_count`    bigint(20) DEFAULT 0 COMMENT '已写入数据行数',
    `progress_ratio`          int(10) DEFAULT NULL COMMENT '任务进度百分比',
    `estimate_remaining_time` bigint(20) DEFAULT NULL COMMENT '预计剩余耗时',
    `invalid_data_count`      bigint(20) DEFAULT 0 COMMENT '无效数据量（主键重复条数）',
    `error_message`           text         DEFAULT NULL COMMENT '错误消息',
    `status`                  varchar(32) NOT NULL COMMENT '状态：上传中、已完成、已失败',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique_data_set_task` (`data_resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据资源上传进度';

DROP TABLE IF EXISTS `image_data_set`;
CREATE TABLE `image_data_set`
(
    `id`              varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`      varchar(32) COMMENT '创建人',
    `created_time`    datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`      varchar(32) COMMENT '更新人',
    `updated_time`    datetime(6) COMMENT '更新时间',
    `for_job_type`    varchar(32) COMMENT '任务类型（物体检测...）',
    `label_list`      varchar(1000) COMMENT 'label 列表',
    `labeled_count`   bigint(20) NOT NULL COMMENT '已标注数量',
    `label_completed` bool COMMENT '是否已标注完毕',
    `files_size`      bigint(20) NOT NULL DEFAULT '0' COMMENT '数据集大小',

    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='图片数据集';

DROP TABLE IF EXISTS `image_data_set_sample`;
CREATE TABLE `image_data_set_sample`
(
    `id`             varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`     varchar(32) COMMENT '创建人',
    `created_time`   datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`     varchar(32) COMMENT '更新人',
    `updated_time`   datetime(6) COMMENT '更新时间',
    `data_set_id`    varchar(36) NOT NULL COMMENT '数据集id',
    `file_name`      varchar(128) COMMENT '文件名',
    `file_path`      varchar(512) COMMENT '文件路径',
    `file_size`      bigint(20) NOT NULL DEFAULT '0' COMMENT '文件大小',
    `label_list`     varchar(1000) COMMENT 'label 列表',
    `labeled`        bool        NOT NULL DEFAULT false COMMENT '是否已标注',
    `label_info`     text COMMENT 'json形式的标注信息',
    `xml_annotation` text COMMENT 'xml形式的标注信息',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique`(`data_set_id`,`file_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='图片数据集中的样本';

DROP TABLE IF EXISTS `table_data_set`;
CREATE TABLE `table_data_set`
(
    `id`                      varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`              varchar(32) COMMENT '创建人',
    `created_time`            datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`              varchar(32) COMMENT '更新人',
    `updated_time`            datetime(6) COMMENT '更新时间',
    `column_name_list`        text        NOT NULL COMMENT '数据集字段列表',
    `column_count`            int(11) NOT NULL COMMENT '数据集列数',
    `primary_key_column`      varchar(32) NOT NULL COMMENT '主键字段',
    `feature_name_list`       text COMMENT '特征列表',
    `feature_count`           int(11) NOT NULL COMMENT '特征数量',
    `contains_y`              bool        NOT NULL COMMENT '是否包含 Y 值',
    `y_name_list`             text COMMENT 'y列名称列表',
    `y_count`                 int(11) NOT NULL COMMENT 'y列的数量',
    `positive_sample_value`   varchar(32) COMMENT '正样本的值',
    `y_positive_sample_count` bigint(20) COMMENT '正例数量',
    `y_positive_sample_ratio` double(10, 4
) COMMENT '正例比例',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据集';


-- bloom_filter definition

DROP TABLE IF EXISTS `bloom_filter`;
CREATE TABLE `bloom_filter`
(
    `id`             varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`     varchar(32)  DEFAULT NULL COMMENT '创建人',
    `created_time`   datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`     varchar(32)  DEFAULT NULL COMMENT '更新人',
    `updated_time`   datetime(6) DEFAULT NULL COMMENT '更新时间',
    `rsa_e`          text COMMENT '密钥e',
    `rsa_n`          text COMMENT '密钥n',
    `rsa_d`          text COMMENT '密钥e',
    `data_source_id` varchar(32)  DEFAULT NULL COMMENT '数据源id',
    `source_path`    varchar(255) DEFAULT NULL COMMENT '数据源地址',
    `hash_function`  text COMMENT '主键hash生成方法',
    `add_method`     varchar(255) DEFAULT NULL COMMENT '布隆过滤器添加方式',
    `sql_script`     varchar(255) DEFAULT NULL COMMENT 'sql语句',
    `rsa_p`          text,
    `rsa_q`          text,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='布隆过滤器';


-- bloom_filter_column definition

DROP TABLE IF EXISTS `bloom_filter_column`;
CREATE TABLE `bloom_filter_column`
(
    `id`                 varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`         varchar(32)  DEFAULT NULL COMMENT '创建人',
    `created_time`       datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`         varchar(32)  DEFAULT NULL COMMENT '更新人',
    `updated_time`       datetime(6) DEFAULT NULL COMMENT '更新时间',
    `bloom_filter_id`    varchar(32)  NOT NULL COMMENT '数据集Id',
    `index`              int(32) NOT NULL COMMENT '序号',
    `name`               varchar(255) NOT NULL COMMENT '字段名称',
    `data_type`          varchar(32)  NOT NULL COMMENT '数据类型',
    `comment`            varchar(255) DEFAULT NULL COMMENT '注释',
    `empty_rows`         bigint(255) DEFAULT '0' COMMENT '空值数据行数',
    `value_distribution` json         DEFAULT NULL COMMENT '数值分布',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集字段';


-- bloom_filter_task definition

DROP TABLE IF EXISTS `bloom_filter_task`;
CREATE TABLE `bloom_filter_task`
(
    `id`                  varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32)  DEFAULT NULL COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`          varchar(32)  DEFAULT NULL COMMENT '更新人',
    `updated_time`        datetime(6) DEFAULT NULL COMMENT '更新时间',
    `bloom_filter_name`   varchar(128) DEFAULT NULL COMMENT '过滤器名',
    `bloom_filter_id`     varchar(32)  DEFAULT NULL COMMENT '过滤器id',
    `total_row_count`     bigint(20) DEFAULT NULL COMMENT '总数据行数',
    `added_row_count`     bigint(20) DEFAULT NULL COMMENT '已写入数据行数',
    `progress`            int(10) DEFAULT NULL COMMENT '任务进度百分比',
    `estimate_time`       int(64) DEFAULT NULL COMMENT '预计剩余耗时',
    `repeat_id_row_count` int(64) DEFAULT NULL COMMENT '主键重复条数',
    `error_message`       text,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique_data_set_task` (`bloom_filter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='添加数据集的任务表。';


-- fusion_field_info definition

DROP TABLE IF EXISTS `fusion_field_info`;
CREATE TABLE `fusion_field_info`
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
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- fusion_task definition

DROP TABLE IF EXISTS `fusion_task`;
CREATE TABLE `fusion_task`
(
    `id`                         varchar(64)  NOT NULL,
    `business_id`                varchar(64)  NOT NULL COMMENT '业务ID',
    `name`                       varchar(255) NOT NULL COMMENT '任务名称',
    `status`                     varchar(32)  NOT NULL COMMENT '任务状态',
    `error`                      text COMMENT '任务错误信息',
    `dst_member_id`              varchar(32)  NOT NULL COMMENT '合作伙伴id',
    `data_resource_id`           varchar(32)   DEFAULT NULL COMMENT '数据集id',
    `data_resource_type`         varchar(21)   DEFAULT NULL,
    `partner_data_resource_id`   varchar(32)   DEFAULT NULL COMMENT '数据集id',
    `partner_data_resource_type` varchar(21)   DEFAULT NULL,
    `row_count`                  int(11) DEFAULT NULL COMMENT '对齐数据行数',
    `psi_actuator_role`          varchar(32)   DEFAULT NULL,
    `algorithm`                  varchar(32)   DEFAULT NULL,
    `partner_row_count`          int(11) DEFAULT NULL COMMENT '处理总数',
    `fusion_count`               int(11) DEFAULT NULL COMMENT '已融合数',
    `spend`                      bigint(20) DEFAULT NULL,
    `created_by`                 varchar(32)   DEFAULT NULL,
    `updated_by`                 varchar(32)   DEFAULT NULL,
    `created_time`               datetime     NOT NULL,
    `updated_time`               datetime      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `description`                varchar(1024) DEFAULT NULL COMMENT '描述',
    `is_trace`                   tinyint(1) NOT NULL DEFAULT '0',
    `trace_column`               varchar(255)  DEFAULT NULL,
    `comment`                    text,
    `project_id`                 varchar(64)   DEFAULT NULL,
    `my_role`                    varchar(100)  DEFAULT NULL,
    `data_count`                 int(11) DEFAULT NULL,
    `processed_count`            int(11) DEFAULT NULL,
    `hash_function`              varchar(100)  DEFAULT NULL,
    `partner_hash_function`      varchar(100)  DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;


-- fusion_result_export_progress definition

DROP TABLE IF EXISTS `fusion_result_export_progress`;
CREATE TABLE `fusion_result_export_progress`
(
    `id`               varchar(64)  NOT NULL,
    `business_id`      varchar(64)  NOT NULL COMMENT '融合任务businessId',
    `table_name`       varchar(255) NOT NULL COMMENT '导出表名',
    `progress`         int(11) NOT NULL COMMENT '进度',
    `total_data_count` int(11) DEFAULT NULL COMMENT '导出总数',
    `processed_count`  int(11) DEFAULT NULL COMMENT '已导出数量',
    `status`           varchar(32) DEFAULT NULL COMMENT '状态',
    `created_by`       varchar(32) DEFAULT NULL,
    `updated_by`       varchar(32) DEFAULT NULL,
    `created_time`     datetime     NOT NULL,
    `updated_time`     datetime    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    `finish_time`      bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- verification_code definition

DROP TABLE IF EXISTS `verification_code`;
CREATE TABLE `verification_code`
(
    `id`            varchar(32)  NOT NULL COMMENT '全局唯一标识',
    `created_by`    varchar(32)  DEFAULT NULL COMMENT '创建人',
    `created_time`  datetime(6) NOT NULL COMMENT '创建时间',
    `updated_by`    varchar(32)  DEFAULT NULL COMMENT '更新人',
    `updated_time`  datetime(6) DEFAULT NULL COMMENT '更新时间',
    `mobile`        varchar(200) NOT NULL COMMENT '手机号',
    `code`          varchar(30)  NOT NULL COMMENT '验证码',
    `success`       varchar(10)  DEFAULT NULL COMMENT 'true：成功，false：失败',
    `send_channel`  varchar(10)  DEFAULT NULL COMMENT '发送渠道，sms：短信、email：邮件',
    `business_type` varchar(30)  DEFAULT NULL COMMENT '业务类型，memberRegister：成员注册、accountForgetPassword：账号忘记密码',
    `resp_content`  varchar(500) DEFAULT NULL COMMENT '响应内容',
    `biz_id`        varchar(64)  DEFAULT NULL COMMENT '业务ID',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码';