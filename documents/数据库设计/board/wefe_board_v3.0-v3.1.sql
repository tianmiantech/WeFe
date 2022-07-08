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
where `level` in (0, 1, 2, 3, 4, 5);