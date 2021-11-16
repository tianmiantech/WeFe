-- 此文件为 v2.3 升级为 v3.0 时需要执行的 sql
-- 请大家按格式写好注释和作者


-- ---------------------------------------
-- v3.0 加表
-- author: zane.luo
-- -------------------------------------
DROP TABLE IF EXISTS `image_data_set`;
CREATE TABLE `image_data_set`
(
    `id`                     varchar(32)   NOT NULL COMMENT '全局唯一标识',
    `created_by`             varchar(32) COMMENT '创建人',
    `created_time`           datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`             varchar(32) COMMENT '更新人',
    `updated_time`           datetime(6) COMMENT '更新时间',
    `name`                   varchar(128)  NOT NULL COMMENT '数据集名称',
    `tags`                   varchar(128) COMMENT '标签',
    `description`            varchar(3072) COMMENT '描述',
    `storage_type`           varchar(32) COMMENT '存储类型',
    `namespace`              varchar(1000) NOT NULL COMMENT '命名空间',
    `for_job_type`           varchar(32) COMMENT '任务类型（物体检测...）',
    `label_list`             varchar(1000) COMMENT 'label 列表',
    `sample_count`           bigint(20) NOT NULL COMMENT '样本数量',
    `labeled_count`          bigint(20) NOT NULL COMMENT '已标注数量',
    `label_completed`        bool COMMENT '是否已标注完毕',
    `files_size`             bigint(20) NOT NULL DEFAULT '0' COMMENT '数据集大小',
    `public_level`           varchar(32) COMMENT '数据集的可见性',
    `public_member_list`     varchar(3072) COMMENT '可见成员列表 只有在列表中的联邦成员才可以看到该数据集的基本信息',
    `usage_count_in_job`     int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    `usage_count_in_flow`    int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    `usage_count_in_project` int(11) NOT NULL DEFAULT '0' COMMENT '使用次数',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据集';


DROP TABLE IF EXISTS `image_data_set_sample`;
CREATE TABLE `image_data_set_sample`
(
    `id`             varchar(32)   NOT NULL COMMENT '全局唯一标识',
    `created_by`     varchar(32) COMMENT '创建人',
    `created_time`   datetime(6) NOT NULL default CURRENT_TIMESTAMP (6) COMMENT '创建时间',
    `updated_by`     varchar(32) COMMENT '更新人',
    `updated_time`   datetime(6) COMMENT '更新时间',
    `data_set_id`    varchar(36)   NOT NULL COMMENT '数据集id',
    `file_name`      varchar(128) COMMENT '文件名',
    `file_path`      varchar(512) COMMENT '文件路径',
    `file_size`      bigint(20) NOT NULL DEFAULT '0' COMMENT '文件大小',
    `label_list`     varchar(1000) NOT NULL COMMENT 'label 列表',
    `labeled`        bool COMMENT '是否已标注',
    `label_info`     text          NOT NULL COMMENT 'json形式的标注信息',
    `xml_annotation` text          NOT NULL COMMENT 'xml形式的标注信息',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `index_unique`(`data_set_id`,`file_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='图片数据集中的样本';


-- ---------------------------------------
-- project 表增加字段
-- author: zane.luo
-- -------------------------------------
ALTER TABLE `project`
    ADD COLUMN `project_type` varchar(36) NOT NULL DEFAULT 'MachineLearning' COMMENT '项目类型' AFTER `flow_status_statistics`;