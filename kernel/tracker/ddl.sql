-- wefe_board.tracking_metric definition

CREATE TABLE `tracking_metric`
(
    `id`               varchar(200) NOT NULL COMMENT '全局唯一标识',
    `job_id`           varchar(200) NOT NULL COMMENT '任务 Id',
    `task_id`          varchar(200) NOT NULL COMMENT '子任务 Id',
    `component_name`   varchar(200) NOT NULL COMMENT '组件名称',
    `role`             varchar(200) NOT NULL COMMENT '角色',
    `member_id`        varchar(200) NOT NULL COMMENT '成员id',
    `metric_namespace` varchar(200) NOT NULL COMMENT 'metric命名空间',
    `metric_name`      varchar(200) NOT NULL COMMENT 'metric名称',
    `metric_type`      varchar(200) NOT NULL COMMENT 'metric类型',
    `curve_name`       varchar(200) NOT NULL COMMENT '曲线名称',
    `abscissa_name`    varchar(200) NOT NULL COMMENT '横坐标名称',
    `ordinate_name`    varchar(200) NOT NULL COMMENT '纵坐标名称',
    `pair_type`        varchar(200) NOT NULL,
    `key`              varchar(200) NOT NULL COMMENT 'key',
    `value`            varchar(1024)         DEFAULT NULL COMMENT '值',
    `created_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`     datetime              DEFAULT NULL COMMENT '更新时间',
    `created_by`       varchar(100)          DEFAULT NULL COMMENT '创建人',
    `updated_by`       varchar(100)          DEFAULT NULL COMMENT '修改人',
    PRIMARY KEY (`id`),
    KEY                `tracking_metric_job_id_IDX` (`job_id`) USING BTREE,
    KEY                `tracking_metric_role_IDX` (`role`) USING BTREE,
    KEY                `tracking_metric_component_name_IDX` (`component_name`) USING BTREE,
    KEY                `tracking_metric_metric_namespace_IDX` (`metric_namespace`) USING BTREE,
    KEY                `tracking_metric_metric_name_IDX` (`metric_name`) USING BTREE,
    KEY                `tracking_metric_key_IDX` (`key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='跟踪指标'

CREATE TABLE `data_output_info`
(
    `id`                  varchar(200) NOT NULL COMMENT '全局唯一标识',
    `job_id`              varchar(200) NOT NULL COMMENT '任务 Id',
    `task_id`             varchar(200) NOT NULL COMMENT '子任务 Id',
    `component_name`      varchar(200) NOT NULL COMMENT '组件名称',
    `role`                varchar(200) NOT NULL COMMENT '角色',
    `member_id`           varchar(200) NOT NULL COMMENT '成员id',
    `data_name`           varchar(100)          DEFAULT '' COMMENT '数据名称',
    `table_namespace`     varchar(200) NOT NULL COMMENT '表空间',
    `table_name`          varchar(200) NOT NULL COMMENT '表名',
    `table_create_count`  int(11) NOT NULL COMMENT '创建时数量',
    `table_current_count` int(11) DEFAULT NULL COMMENT '当前数量',
    `partition`           int(11) DEFAULT NULL COMMENT '分区数',
    `member_model_id`     varchar(100)          DEFAULT NULL COMMENT '模型id',
    `model_version`       varchar(100)          DEFAULT NULL COMMENT '模型版本',
    `desc`                varchar(200)          DEFAULT NULL COMMENT '描述',
    `created_by`          varchar(100)          DEFAULT NULL COMMENT '创建人',
    `updated_by`          varchar(100)          DEFAULT NULL COMMENT '修改人',
    `created_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`        datetime              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据信息'

CREATE TABLE `output_model`
(
    `id`                  varchar(200) NOT NULL COMMENT '全局唯一标识',
    `job_id`              varchar(200) NOT NULL COMMENT 'job Id',
    `task_id`             varchar(200) NOT NULL COMMENT '任务 Id',
    `component_name`      varchar(200) NOT NULL COMMENT '组件名称',
    `role`                varchar(200) NOT NULL COMMENT '角色',
    `member_id`           varchar(200) NOT NULL COMMENT '成员id',
    `member_model_id`     varchar(1000)         DEFAULT NULL COMMENT '模型id',
    `model_version`       varchar(100)          DEFAULT NULL COMMENT '模型版本',
    `component_model_key` varchar(100)          DEFAULT NULL COMMENT '模型key',
    `model_meta`          text COMMENT '模型信息',
    `model_param`         mediumtext COMMENT '模型参数',
    `created_by`          varchar(100)          DEFAULT NULL COMMENT '创建人',
    `updated_by`          varchar(100)          DEFAULT NULL COMMENT '修改人',
    `created_time`        datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`        datetime              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='输出模型'

CREATE TABLE `cur_best_model`
(
    `id`             varchar(200) NOT NULL COMMENT '全局唯一标识',
    `job_id`         varchar(200) NOT NULL COMMENT 'job Id',
    `task_id`        varchar(200) NOT NULL COMMENT '任务 Id',
    `component_name` varchar(200) NOT NULL COMMENT '组件名称',
    `role`           varchar(200) NOT NULL COMMENT '角色',
    `member_id`      varchar(200) NOT NULL COMMENT '成员id',
    `model_meta`     text COMMENT '模型信息',
    `model_param`    mediumtext COMMENT '模型参数',
    `iteration`      int(11) DEFAULT '0' COMMENT '当前迭代索引',
    `created_by`     varchar(100)          DEFAULT NULL COMMENT '创建人',
    `updated_by`     varchar(100)          DEFAULT NULL COMMENT '修改人',
    `created_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`   datetime              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='当前最优模型';

CREATE TABLE `provider_model_params`
(
    `id`                   varchar(200) NOT NULL COMMENT '全局唯一标识',
    `job_id`               varchar(200) NOT NULL COMMENT 'job Id',
    `task_id`              varchar(200) NOT NULL COMMENT '任务 Id',
    `component_name`       varchar(200) NOT NULL COMMENT '组件名称',
    `role`                 varchar(200) NOT NULL COMMENT '角色',
    `member_id`            varchar(200) NOT NULL COMMENT '成员id',
    `provider_member_id`   varchar(200) NOT NULL COMMENT '数据方成员id',
    `provider_model_param` mediumtext COMMENT '模型参数',
    `created_by`           varchar(100)          DEFAULT NULL COMMENT '创建人',
    `updated_by`           varchar(100)          DEFAULT NULL COMMENT '修改人',
    `created_time`         datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time`         datetime              DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据方模型参数';



--------    修改记录  ------------

-- 2020.08.26 添加数据名称字段
-- alter table data_output_info add `data_name` varchar(100) DEFAULT '' COMMENT '数据名称' after member_id;

