ALTER TABLE `partner`
    MODIFY COLUMN `interface_name` varchar (1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求接口名称' AFTER `log_interface`;
