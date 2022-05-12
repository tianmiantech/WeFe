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
    

-- https://www.tapd.cn/53885119/prong/stories/view/1153885119001085243
CREATE TABLE `partner` (
  `id` varchar(32) NOT NULL,
  `partner_id` varchar(256) NOT NULL COMMENT '合作者id',
  `name` varchar(64) DEFAULT NULL COMMENT '合作者名称',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `serving_base_url` varchar(255) DEFAULT NULL COMMENT 'Serving服务地址',
  `code` varchar(255) DEFAULT '' COMMENT '客户 code',
  `remark` text COMMENT '备注',
  `is_union_member` tinyint(1) NOT NULL COMMENT '是否是联邦成员',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '合作者状态;1正常、0删除',
  `created_time` datetime NOT NULL,
  `updated_time` datetime DEFAULT NULL,
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `partner_id` (`partner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- https://www.tapd.cn/53885119/prong/stories/view/1153885119001085582    
alter table `client_service` add column `public_key` text COMMENT '调用者公钥' after `client_name`
alter table `client_service` add column `type` tinyint(1) COMMENT '服务类型 0开通，1激活 ' after `client_name`

-- https://www.tapd.cn/53885119/prong/stories/view/1153885119001085636
INSERT INTO `global_config` (`id`, `created_by`, `created_time`, `updated_by`, `updated_time`, `group`, `name`, `value`, `comment`)
VALUES
    ('52b587eecde211ec8b2c00163e0a7897', NULL, '2022-05-07 16:47:27.556824', '06198105b8c647289177cf057a15bdbb', NULL, 'identity_info', 'serving_base_url', 'http://localhost:8080/serving-service-01/', '地址');
