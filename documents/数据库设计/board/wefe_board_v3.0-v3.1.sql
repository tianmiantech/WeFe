-- 此文件为 v3.0 升级为 v3.1 时需要执行的 sql
-- 请大家按格式写好注释和作者


-- -------------------------------------
-- project、flow 增加置顶功能
-- author: zane.luo
-- -------------------------------------
ALTER TABLE `project`
    ADD COLUMN `top` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶';
ALTER TABLE `project`
    ADD COLUMN `sort_num` int NOT NULL DEFAULT 0 COMMENT '排序序号';

ALTER TABLE `project_flow`
    ADD COLUMN `top` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶';
ALTER TABLE `project_flow`
    ADD COLUMN `sort_num` int NOT NULL DEFAULT 0 COMMENT '排序序号';


-- -------------------------------------
-- account 增加字段功能
-- author: zane.luo
-- -------------------------------------
ALTER TABLE `account`
    ADD COLUMN `ui_config` text NULL COMMENT 'UI 相关配置信息';



alter table `fusion_task`
    add column `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已删除';

-- -------------------------------------
-- job 表加字段
-- author: zane.luo
-- -------------------------------------
ALTER TABLE `job`
    ADD COLUMN `job_config` text NULL COMMENT '配置信息';

-- -------------------------------------
-- table_data_set 表加字段
-- author: zane.luo
-- -------------------------------------
ALTER TABLE `table_data_set`
    ADD COLUMN `label_distribution` text NULL COMMENT 'label 分布情况';

-- -------------------------------------
-- global_config 表字段扩容
-- author: zane.luo
-- -------------------------------------
ALTER TABLE `global_config`
    MODIFY COLUMN `name` varchar (128) COMMENT '配置项名称',
    MODIFY COLUMN `group` varchar (128) COMMENT '配置项名称';

-- -------------------------------------
-- message 表加字段
-- author: zane.luo
-- -------------------------------------
ALTER TABLE `message`
    ADD COLUMN `event` varchar(32) NOT NULL DEFAULT 'OnGatewayError' COMMENT '消息关联的事件';
ALTER TABLE `message`
    ADD COLUMN `todo` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是待办事项';
ALTER TABLE `message`
    ADD COLUMN `todo_complete` tinyint(1) NOT NULL DEFAULT '0' COMMENT '待办事项是否已处理';
ALTER TABLE `message`
    ADD COLUMN `todo_related_id1` varchar(128) COMMENT '待办事项关联对象Id1';
ALTER TABLE `message`
    ADD COLUMN `todo_related_id2` varchar(128) COMMENT '待办事项关联对象Id2';
-- 将历史数据中的枚举改为字符串
update `message`
set `level`='error'
where `level` in ('0', '1', '2', '3', '4', '5');

CREATE TABLE `cert_info` (
  `id` varchar(32) NOT NULL COMMENT '全局唯一标识',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `member_id` varchar(32) DEFAULT NULL COMMENT '成员ID',
  `subject_pub_key` text COMMENT '申请人公钥内容',
  `subject_org` varchar(256) DEFAULT NULL COMMENT '申请人组织名称',
  `subject_cn` varchar(256) DEFAULT NULL COMMENT '申请人常用名称',
  `serial_number` varchar(256) DEFAULT NULL COMMENT '证书序列号',
  `cert_content` text COMMENT '证书pem内容',
  `csr_id` varchar(32) DEFAULT NULL COMMENT '证书请求ID',
  `status` varchar(32) DEFAULT NULL COMMENT '证书状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书';

CREATE TABLE `cert_key_info` (
  `id` varchar(32) NOT NULL COMMENT '全局唯一标识',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `key_pem` text COMMENT '私钥pem内容',
  `member_id` varchar(32) DEFAULT NULL COMMENT '成员ID',
  `key_alg` varchar(32) DEFAULT NULL COMMENT '密钥算法',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='密钥';

CREATE TABLE `cert_request_info` (
  `id` varchar(32) NOT NULL COMMENT '全局唯一标识',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `member_id` varchar(32) DEFAULT NULL COMMENT '成员ID',
  `subject_key_id` varchar(32) DEFAULT NULL COMMENT '申请人私钥ID',
  `subject_org` varchar(256) DEFAULT NULL COMMENT '申请人组织名称',
  `subject_cn` varchar(256) DEFAULT NULL COMMENT '申请人常用名称',
  `cert_request_content` text COMMENT '证书请求内容',
  `issue` tinyint(2) DEFAULT NULL COMMENT '是否签发',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书请求';

ALTER TABLE message_queue DROP COLUMN `action`;
ALTER TABLE message_queue DROP COLUMN channel;

-- 删除手机号唯一索引
ALTER TABLE `account` DROP INDEX `index_unique_phonenumber`;