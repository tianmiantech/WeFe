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