-- 服务表
DROP TABLE IF EXISTS service;
CREATE TABLE `service` (
  `id` varchar(32) NOT NULL COMMENT '全局唯一标识',
  `created_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `updated_time` datetime DEFAULT NULL COMMENT '更新时间',
  `name` varchar(32) NOT NULL COMMENT '服务名',
  `url` varchar(128) NOT NULL COMMENT '服务地址',
  `service_type` tinyint(2) NOT NULL COMMENT '服务类型  1匿踪查询，2交集查询，3安全聚合',
  `query_params` text COMMENT '查询参数配置',
  `data_source` text COMMENT 'SQL配置',
  `status` tinyint(2) DEFAULT '0' COMMENT '是否在线 1在线，0离线',
  PRIMARY KEY (`id`),
  UNIQUE KEY `url_unique` (`url`),
  KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务';

-- 数据源
DROP TABLE IF EXISTS data_source;
CREATE TABLE `data_source` (
   `database_type` varchar(255) DEFAULT NULL,
   `host` varchar(255) DEFAULT NULL,
   `port` int(255) DEFAULT NULL,
   `database_name` varchar(255) DEFAULT NULL,
   `user_name` varchar(255) DEFAULT NULL,
   `password` varchar(255) DEFAULT NULL,
   `created_by` varchar(255) DEFAULT NULL,
   `id` varchar(32) NOT NULL,
   `created_time` datetime DEFAULT NULL,
   `updated_time` datetime DEFAULT NULL,
   `name` varchar(255) DEFAULT NULL,
   `updated_by` varchar(255) DEFAULT '',
   PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

-- 客户表
DROP TABLE IF EXISTS client;
CREATE TABLE client(
                       id VARCHAR(32) NOT NULL   COMMENT '客户id' ,
                       name VARCHAR(255) NOT NULL   COMMENT '客户名称' ,
                       created_by varchar(32) DEFAULT NULL COMMENT '创建人',
                       created_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       updated_by varchar(32) DEFAULT NULL COMMENT '更新人',
                       updated_time datetime DEFAULT NULL COMMENT '更新时间',
                       email VARCHAR(255)    COMMENT '邮箱' ,
                       ip_add VARCHAR(255) NOT NULL   COMMENT 'ip地址' ,
                       pub_key VARCHAR(255) NOT NULL   COMMENT '公钥' ,
                       remark VARCHAR(255)    COMMENT '备注' ,
                       status INT NOT NULL  DEFAULT 1 COMMENT '客户状态;1正常、0删除' ,
                       PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '客户基本信息表';


-- 客户-服务表
DROP TABLE IF EXISTS client_service;
CREATE TABLE client_service(
                               id VARCHAR(32) NOT NULL   COMMENT '客户服务id' ,
                               service_id VARCHAR(32) NOT NULL   COMMENT '服务id' ,
                               client_id VARCHAR(32) NOT NULL   COMMENT '客户id' ,
                               created_by varchar(32) DEFAULT NULL COMMENT '创建人',
                               created_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               updated_by varchar(32) DEFAULT NULL COMMENT '更新人',
                               updated_time datetime DEFAULT NULL COMMENT '更新时间',
                               status TINYINT(1) NOT NULL   COMMENT '是否启用' ,
                               PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT = '客户服务表';
CREATE UNIQUE INDEX service_client_index ON client_service(id,service_id,client_id);

-- 计费规则配置表
DROP TABLE IF EXISTS fee_config;
CREATE TABLE fee_config(
                           id VARCHAR(32) NOT NULL   COMMENT '' ,
                           service_id VARCHAR(32)  COMMENT '服务id' ,
                           client_id VARCHAR(32)  COMMENT '客户id' ,
                           created_by varchar(32) DEFAULT NULL COMMENT '创建人',
                           created_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           updated_by varchar(32) DEFAULT NULL COMMENT '更新人',
                           updated_time datetime DEFAULT NULL COMMENT '更新时间',
                           unit_price double (10,6) NOT NULL   COMMENT '调用单价' ,
                           pay_type TINYINT(1) NOT NULL   COMMENT '付费类型: 1 预付费、0 后付费' ,
                           PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '计费配置';

-- API 调用统计表
DROP TABLE IF EXISTS api_request_record;
CREATE TABLE api_request_record(
                                   id VARCHAR(32) NOT NULL   COMMENT '租户号' ,
                                   service_id VARCHAR(32) NOT NULL   COMMENT '服务id' ,
                                   client_id VARCHAR(32) NOT NULL   COMMENT '客户id' ,
                                   ip_add VARCHAR(255) NOT NULL   COMMENT '请求ip地址' ,
                                   spend BIGINT NOT NULL   COMMENT '耗时' ,
                                   request_result INT NOT NULL   COMMENT '请求结果' ,
                                   created_by varchar(32) DEFAULT NULL COMMENT '创建人',
                                   created_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   updated_by varchar(32) DEFAULT NULL COMMENT '更新人',
                                   updated_time datetime DEFAULT NULL COMMENT '更新时间',
                                   PRIMARY KEY (id)
)  COMMENT = 'API 调用记录';
CREATE UNIQUE INDEX service_client_index ON api_request_record(service_id,client_id,id);

-- 计费详情表
DROP TABLE IF EXISTS fee_detail;
CREATE TABLE fee_detail(
                           id VARCHAR(32) NOT NULL   COMMENT '' ,
                           service_id VARCHAR(32) NOT NULL   COMMENT '服务id' ,
                           client_id VARCHAR(32) NOT NULL   COMMENT '客户id' ,
                           total_fee DECIMAL(24,6)    COMMENT '总费用' ,
                           total_request_times INT NOT NULL  DEFAULT 0 COMMENT '总调用次数' ,
                           created_by varchar(32) DEFAULT NULL COMMENT '创建人',
                           created_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           updated_by varchar(32) DEFAULT NULL COMMENT '更新人',
                           updated_time datetime DEFAULT NULL COMMENT '更新时间',
                           PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '结算详情表';
CREATE UNIQUE INDEX fee_detail_index ON fee_detail(id,service_id,client_id);
