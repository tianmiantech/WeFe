-- 用户账号表

CREATE TABLE `account` (
  `id` varchar(32) NOT NULL COMMENT '全局唯一标识',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `phone_number` varchar(32) NOT NULL COMMENT '手机号',
  `password` varchar(128) NOT NULL COMMENT '密码',
  `salt` varchar(128) NOT NULL COMMENT '盐',
  `nickname` varchar(32) NOT NULL COMMENT '昵称',
  `email` varchar(128) NOT NULL COMMENT '邮箱',
  `super_admin_role` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是超级管理员 超级管理员通常是第一个创建并初始化系统的那个人',
  `admin_role` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否是管理员 管理员有更多权限，比如设置 member 是否对外可见。',
  PRIMARY KEY (`id`),
  UNIQUE KEY `index_unique_phonenumber` (`phone_number`),
  KEY `idx_create_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账号 ';

-- 全局设置表

CREATE TABLE `global_setting` (
  `id` varchar(32) NOT NULL COMMENT '全局唯一标识',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `member_id` varchar(32) NOT NULL COMMENT '联邦成员 Id 全局唯一，默认为uuid。',
  `member_name` varchar(128) NOT NULL COMMENT '联邦成员名称',
  `rsa_private_key` text NOT NULL,
  `rsa_public_key` text NOT NULL COMMENT '公钥',
  `gateway_uri` varchar(512) DEFAULT NULL COMMENT '网关通信地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局设置 全局设置，这个表永远有且只有一条数据。';


-- 成员配置信息表

CREATE TABLE `member` (
  `id` varchar(32) NOT NULL,
  `member_id` varchar(256) NOT NULL COMMENT '成员id',
  `name` varchar(64) NOT NULL,
  `api` varchar(256) DEFAULT NULL COMMENT '调用路警',
  `public_key` text NOT NULL COMMENT '公钥',
  `created_time` datetime NOT NULL,
  `updated_time` datetime DEFAULT NULL,
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成员配置信息表';


-- 模型表

CREATE TABLE `model` (
  `id` varchar(32) NOT NULL,
  `model_id` varchar(256) NOT NULL COMMENT '模型id',
  `algorithm` varchar(64) NOT NULL COMMENT '算法',
  `fl_type` varchar(64) NOT NULL COMMENT '联邦学习类型',
  `feature_source` varchar(64) NOT NULL,
  `model_param` mediumtext NOT NULL COMMENT '模型参数',
  `created_time` datetime NOT NULL,
  `updated_time` datetime DEFAULT NULL,
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `enable` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'true-在线 false-下线',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型表';

-- 参与模型成员表

CREATE TABLE `model_member` (
  `id` varchar(32) NOT NULL,
  `model_id` varchar(256) NOT NULL COMMENT '模型id',
  `member_id` varchar(256) DEFAULT NULL COMMENT '成员id',
  `role` varchar(64) DEFAULT NULL COMMENT '角色',
  `created_time` datetime NOT NULL,
  `updated_time` datetime DEFAULT NULL,
  `created_by` varchar(32) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练模型成员表';

-- 模型sql配置表

CREATE TABLE `model_sql_config` (
  `id` varchar(32) NOT NULL,
  `model_id` varchar(256) DEFAULT NULL COMMENT '模型id',
  `type` varchar(64) DEFAULT NULL COMMENT 'db类型',
  `url` varchar(255) DEFAULT NULL COMMENT '数据链接路径',
  `username` varchar(255) DEFAULT NULL COMMENT '账号',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `sql_context` text COMMENT '执行SQL',
  `created_time` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP,
  `created_by` varchar(32) DEFAULT NULL,
  `updated_by` varchar(32) DEFAULT NULL,
  `updated_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- 调用日志表

CREATE TABLE `predict_log` (
  `id` varchar(32) NOT NULL,
  `seq_no` varchar(64) NOT NULL COMMENT '流水号',
  `member_id` varchar(256) NOT NULL COMMENT '成员id',
  `model_id` varchar(256) NOT NULL COMMENT '模型id',
  `algorithm` varchar(64) DEFAULT NULL,
  `fl_type` varchar(64) DEFAULT NULL,
  `my_role` varchar(64) DEFAULT NULL,
  `created_time` timestamp NOT NULL,
  `request` text,
  `response` text COMMENT '返回结果',
  `spend` bigint(20) DEFAULT NULL,
  `result` tinyint(1) NOT NULL DEFAULT '0' COMMENT '调用结果：1成功，0失败',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调用日志表';


-- 调用统计表


CREATE TABLE `predict_statistics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `member_id` varchar(256) NOT NULL DEFAULT '' COMMENT '成员id',
  `model_id` varchar(256) NOT NULL COMMENT '模型id',
  `month` varchar(20) NOT NULL COMMENT '月份',
  `day` varchar(20) NOT NULL COMMENT '天',
  `hour` varchar(20) NOT NULL COMMENT '小时',
  `minute` varchar(20) NOT NULL COMMENT '分',
  `total` bigint(20) NOT NULL DEFAULT '0' COMMENT '调用数量',
  `success` bigint(20) NOT NULL DEFAULT '0' COMMENT '成功数量',
  `fail` bigint(20) NOT NULL DEFAULT '0' COMMENT '失败数量',
  `created_time` datetime NOT NULL COMMENT '创建时间',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_member_id_month` (`member_id`,`month`),
  KEY `idx_member_id_day` (`member_id`,`day`),
  KEY `idx_member_id_hours` (`member_id`,`hour`),
  KEY `idx_member_id_time` (`member_id`,`minute`),
  KEY `idx_model_id_month` (`model_id`,`month`),
  KEY `idx_model_id_day` (`model_id`,`day`),
  KEY `idx_model_id_hours` (`model_id`,`hour`),
  KEY `idx_model_id_time` (`model_id`,`minute`),
  KEY `idx_day` (`day`),
  KEY `idx_hours` (`hour`),
  KEY `idx_time` (`minute`)
) ENGINE=InnoDB AUTO_INCREMENT=6895578 DEFAULT CHARSET=utf8mb4 COMMENT='调用统计表';





