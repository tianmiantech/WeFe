-- 此文件为 v2.2 升级为 v2.3 时需要执行的 sql
-- 请大家按格式写好注释和作者


-- ---------------------------------------
-- 流程表增加字段
-- author: zane.luo
-- -------------------------------------
ALTER TABLE project_flow
    ADD COLUMN `creator_member_id` varchar(36) COMMENT '创建此流程的成员的ID';