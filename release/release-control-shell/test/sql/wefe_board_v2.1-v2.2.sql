-- 此文件为 v2.1 升级为 v2.2 时需要执行的 sql
-- 请大家按格式写好注释和作者


-- ----------------------------
-- 对 task 表中的 task_conf 字段进行容量扩容
-- author: Zane
-- ----------------------------
ALTER TABLE `task` MODIFY COLUMN `task_conf` longtext NOT NULL COMMENT '任务conf_json' AFTER `task_type`;


-- ----------------------------
-- 重构 data_set_task 表结构
-- author: Zane
-- ----------------------------
DROP TABLE IF EXISTS `data_set_task`;
CREATE TABLE `data_set_task`
(
    `id`                  varchar(32) NOT NULL COMMENT '全局唯一标识',
    `created_by`          varchar(32) COMMENT '创建人',
    `created_time`        datetime(6) NOT NULL COMMENT '创建时间',
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
-- 新增 data_source 表结构
-- author: Jacky
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


-- ---------------------------------------
-- 数据集表 data_set 新增正例数量和比例字段
-- author: aaron.li
-- -------------------------------------
ALTER TABLE data_set
    ADD COLUMN `y_positive_example_count` bigint(20) DEFAULT NULL COMMENT '正例数量';
ALTER TABLE data_set
    ADD COLUMN `y_positive_example_ratio` double(10,4) DEFAULT NULL COMMENT '正例比例';

alter table operator_log
    add index log_action_index (log_action);
alter table operator_log
    add index operator_phone_index (operator_phone);
alter table operator_log
    add index created_time_index (created_time);


-- ----------------------------
-- project 表加字段
-- author: Zane
-- ----------------------------
ALTER TABLE `project`
    ADD COLUMN `exited_by` varchar(32) NULL COMMENT '退出项目的操作者' AFTER `exited`,
ADD COLUMN `exited_time` datetime(6) NULL COMMENT '退出时间' AFTER `exited_by`,
ADD COLUMN `closed_by` varchar(32) NULL COMMENT '关闭者' AFTER `closed`,
ADD COLUMN `closed_time` datetime(6) NULL COMMENT '关闭时间' AFTER `closed_by`;


-- ----------------------------
-- operator_log 表加索引，优化多条件筛选下的查询性能。
-- author: Zane
-- ----------------------------
ALTER TABLE `operator_log`
    ADD INDEX `query_index`(`created_time`, `operator_phone`, `log_action`);


-- ----------------------------
-- 对 ProjectFlowMySqlModel 加 @Enumerated(EnumType.STRING)
-- author: Zane
-- ----------------------------
update project_flow
set federated_learning_type='horizontal'
where federated_learning_type = '0';
update project_flow
set federated_learning_type='vertical'
where federated_learning_type = '1';
update project_flow
set federated_learning_type='mix'
where federated_learning_type = '2';


-- ----------------------------
-- 新增模型打分的oot关系记录表
-- author: Aaron.Li
-- ----------------------------
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
-- oot模板
-- author: Aaron.Li
-- ----------------------------
INSERT INTO `project_flow_template`
VALUES ('70a504c115504b01910fb766d62fe016',
        '{\"nodes\":[{\"id\":\"start\",\"label\":\"开始\",\"x\":101,\"y\":150,\"anchorPoints\":[[0.5,1]],\"singleEdge\":true,\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#f2f9ec\",\"width\":100,\"stroke\":\"#8BC34A\"},\"labelCfg\":{\"style\":{\"fill\":\"#8BC34A\",\"fontSize\":14}},\"nodeStateStyles\":{\"nodeState:default\":{\"lineWidth\":1},\"nodeState:selected\":{\"lineWidth\":2}},\"data\":{\"nodeType\":\"system\"},\"type\":\"flow-node\"},{\"id\":\"16296886125199914\",\"x\":101,\"y\":260,\"anchorPoints\":[[0.5,0],[0.5,1]],\"singleEdge\":true,\"label\":\"打分验证\",\"data\":{\"componentType\":\"Oot\"},\"type\":\"flow-node\",\"style\":{\"highlight\":{\"lineWidth\":1,\"fill\":\"#f85564\",\"stroke\":\"#f85564\",\"labelCfg\":{\"style\":{\"fill\":\"#fff\",\"fontWeight\":\"bold\"}}},\"fill\":\"#ecf3ff\",\"width\":100,\"stroke\":\"#4483FF\",\"lineWidth\":1},\"labelCfg\":{\"style\":{\"fill\":\"#4483FF\"}}}],\"edges\":[{\"id\":\"16296886317753166\",\"source\":\"start\",\"target\":\"16296886125199914\",\"sourceAnchor\":0,\"targetAnchor\":0,\"label\":\"\",\"type\":\"flow-edge\",\"style\":{\"active\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":1},\"selected\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2,\"shadowColor\":\"rgb(95, 149, 255)\",\"shadowBlur\":10,\"text-shape\":{\"fontWeight\":500}},\"highlight\":{\"stroke\":\"rgb(95, 149, 255)\",\"lineWidth\":2,\"text-shape\":{\"fontWeight\":500}},\"inactive\":{\"stroke\":\"rgb(234, 234, 234)\",\"lineWidth\":1},\"disable\":{\"stroke\":\"rgb(245, 245, 245)\",\"lineWidth\":1},\"edgeState:default\":{\"stroke\":\"#aab7c3\",\"endArrow\":{\"path\":\"M 0,0 L 8,4 L 7,0 L 8,-4 Z\",\"fill\":\"#aab7c3\",\"stroke\":\"#aab7c3\"},\"lineWidth\":1},\"edgeState:selected\":{\"stroke\":\"#1890FF\"},\"edgeState:hover\":{\"stroke\":\"#1890FF\"},\"stroke\":\"#aab7c3\",\"lineAppendWidth\":20,\"endArrow\":true},\"startPoint\":{\"x\":101,\"y\":170.5,\"anchorIndex\":0},\"endPoint\":{\"x\":101,\"y\":239.5,\"anchorIndex\":0},\"curveOffset\":[-20,20],\"curvePosition\":[0.5,0.5]}],\"combos\":[]}',
        '打分验证', 'oot', '新建流程模板不显示，只有模型列表使用', 'vertical', '35537bb55a1348218d010f07d875ab24', '2021-6-1 16:47:45', NULL,
        NULL);


-- ----------------------------
-- 移除两个表： global_setting、system_config
-- 使用新的 global_config 表替代两个旧表
-- author: Zane
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
  DEFAULT CHARSET = utf8mb4 COMMENT ='全局设置 全局设置，这个表永远有且只有一条数据。';

insert into global_config(`id`, `group`, `name`, `comment`, `value`)
values (replace(uuid(), '-', ''), 'member_info', 'member_id', '联邦成员 Id 全局唯一，默认为uuid。',
        (select member_id from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','member_name','联邦成员名称',(select member_name from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','member_email','联邦成员邮箱',(select member_email from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','member_mobile','联邦成员电话',(select member_mobile from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','member_gateway_uri','联邦成员网关访问地址',(select gateway_uri from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','member_allow_public_data_set','是否允许对外公开数据集基础信息',(select member_allow_public_data_set from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','member_logo','成员头像',(select member_logo from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','member_hidden','隐身状态',(select member_hidden from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','rsa_private_key','私钥',(select rsa_private_key from global_setting limit 1)),
(replace(uuid(),'-',''),'member_info','rsa_public_key','公钥',(select rsa_public_key from global_setting limit 1)),

(replace(uuid(),'-',''),'alert_config','email_alert_on_job_error','Job 执行失败时进行邮件通知','false'),

(replace(uuid(),'-',''),'wefe_board','account_need_audit_when_register','新注册的账号是否需要管理员审核','true'),

(replace(uuid(),'-',''),'mail_server','mail_host','邮件服务器地址',(select mail_host from system_config limit 1)),
(replace(uuid(),'-',''),'mail_server','mail_port','邮件服务器端口',(select mail_port from system_config limit 1)),
(replace(uuid(),'-',''),'mail_server','mail_username','邮件用户名',(select mail_username from system_config limit 1)),
(replace(uuid(),'-',''),'mail_server','mail_password','邮件密码',(select mail_password from system_config limit 1)),

(replace(uuid(),'-',''),'wefe_gateway','ip_white_list','关网IP白名单',(select gateway_ip_white_list from system_config limit 1));
