-- 此文件为 v2.3 升级为 v3.0 时需要执行的 sql
-- 请大家按格式写好注释和作者

-- ----------------------------
-- 添加融合主键展示字段
-- ----------------------------
ALTER TABLE bloom_filter ADD hash_function varchar(255) NULL;

-- ----------------------------
-- 修改合作伙伴成员字段
ALTER TABLE partner CHANGE partner_id member_id  varchar(64)  NOT NULL COMMENT '合作方id';
ALTER TABLE partner CHANGE `name` member_name  varchar(255)  NOT NULL COMMENT '合作方名称';

-- 修改任务相关字段
ALTER TABLE task CHANGE partner_id partner_member_id  varchar(32)  NOT NULL COMMENT '合作成员id';
ALTER TABLE task ADD `my_role` varchar(255) DEFAULT NULL;
ALTER TABLE task ADD `processed_count` int(10) DEFAULT NULL;

-- -------------------------------------
-- 修改相关表手机号字段长度
-- author: aaron.li
-- -------------------------------------
ALTER TABLE account MODIFY COLUMN phone_number VARCHAR(200);



