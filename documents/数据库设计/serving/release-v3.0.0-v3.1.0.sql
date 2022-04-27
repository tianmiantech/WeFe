-- 服务调用日志表
DROP TABLE IF EXISTS service_call_log;
CREATE TABLE service_call_log
(
    id                    VARCHAR(32)  NOT NULL COMMENT '序号id',
    order_id              VARCHAR(255) NOT NULL COMMENT '订单号',
    call_by_me            BOOL         NOT NULL DEFAULT 0 COMMENT '是否是我方发起的请求',
    request_partner_id    VARCHAR(255) NOT NULL COMMENT '请求方id',
    request_partner_name  VARCHAR(32)  NOT NULL COMMENT '请求方名称',
    response_partner_id   VARCHAR(255) NOT NULL COMMENT '响应方id',
    response_partner_name VARCHAR(32)  NOT NULL COMMENT '响应方名称',
    service_id            VARCHAR(255) NOT NULL COMMENT '服务id',
    service_name          VARCHAR(32)  NOT NULL COMMENT '服务名称',
    service_type          VARCHAR(32)  NOT NULL COMMENT '服务类型',
    request_id            VARCHAR(255) NOT NULL COMMENT '请求id',
    response_id           VARCHAR(255) NOT NULL COMMENT '相应id',
    request_data          TEXT         NOT NULL COMMENT '请求内容',
    response_data         TEXT         NOT NULL COMMENT '响应内容',
    response_code         INT          NOT NULL DEFAULT 0 COMMENT '响应码',
    response_status       VARCHAR(255) NOT NULL COMMENT '响应状态',
    request_ip            VARCHAR(255) NOT NULL COMMENT '请求ip',
    created_by            VARCHAR(32) COMMENT '创建人',
    created_time          DATETIME COMMENT '创建时间',
    updated_by            VARCHAR(32) COMMENT '更新人',
    updated_time          DATETIME COMMENT '更新时间',
    spend_time            INT COMMENT '响应时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '服务调用日志表';
CREATE INDEX service_order_index ON service_call_log (service_id, order_id);
CREATE INDEX req_resp_index ON service_call_log (request_id, response_id);
CREATE INDEX req_resp_partner ON service_call_log (request_partner_id, response_partner_id);

-- 订单表
DROP TABLE IF EXISTS service_order;
CREATE TABLE service_order
(
    id                    VARCHAR(32) NOT NULL COMMENT '订单号',
    service_id            VARCHAR(32) COMMENT '服务id',
    service_name          VARCHAR(32) COMMENT '服务名称',
    service_type          VARCHAR(32) COMMENT '服务类型',
    order_type            BOOL        NOT NULL DEFAULT 1 COMMENT '是否为己方生成的订单;1 是, 0否',
    status                VARCHAR(32) NOT NULL COMMENT '订单状态;成功、失败、进行中',
    request_partner_id    VARCHAR(32) NOT NULL COMMENT '请求方id',
    request_partner_name  VARCHAR(32) COMMENT '请求方名称',
    response_partner_id   VARCHAR(32) NOT NULL COMMENT '响应方id',
    response_partner_name VARCHAR(32) COMMENT '响应方名称',
    created_by            VARCHAR(32) COMMENT '创建人',
    created_time          DATETIME COMMENT '创建时间',
    updated_by            VARCHAR(32) COMMENT '更新人',
    updated_time          DATETIME COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '订单表';
CREATE INDEX service_req_resp_index ON service_order (service_id, request_partner_id, response_partner_id);

-- 订单统计表
DROP TABLE IF EXISTS order_statistics;
CREATE TABLE order_statistics
(
    id                    VARCHAR(32) NOT NULL COMMENT '序号',
    call_times            INT COMMENT '调用次数',
    success_times         INT COMMENT '成功次数',
    failed_times          INT COMMENT '失败次数',
    minute                VARCHAR(255) COMMENT '每分钟统计',
    hour                  VARCHAR(255) COMMENT '每小时统计',
    day                   VARCHAR(255) COMMENT '每天统计',
    month                 VARCHAR(255) COMMENT '每月统计',
    service_id            VARCHAR(32) NOT NULL COMMENT '服务id',
    service_name          VARCHAR(32) NOT NULL COMMENT '服务名称',
    request_partner_id    VARCHAR(32) NOT NULL COMMENT '请求方id',
    request_partner_name  VARCHAR(32) NOT NULL COMMENT '请求方名称',
    response_partner_id   VARCHAR(32) NOT NULL COMMENT '响应方id',
    response_partner_name VARCHAR(32) NOT NULL COMMENT '响应方名称',
    created_by            VARCHAR(32) COMMENT '创建人',
    created_time          DATETIME COMMENT '创建时间',
    updated_by            VARCHAR(32) COMMENT '更新人',
    updated_time          DATETIME COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT = '订单统计表';
CREATE INDEX service_req_resp_index ON order_statistics (service_id, request_partner_id, response_partner_id);
CREATE INDEX minute_index ON order_statistics (minute);
CREATE INDEX hour_index ON order_statistics (hour);
CREATE INDEX day_index ON order_statistics (day);
CREATE INDEX month_index ON order_statistics (month);
