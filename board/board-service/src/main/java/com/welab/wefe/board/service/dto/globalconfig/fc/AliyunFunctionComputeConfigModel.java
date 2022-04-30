/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.dto.globalconfig.fc;

/**
 * @author zane
 * @date 2021/10/29
 */
public class AliyunFunctionComputeConfigModel extends FunctionComputeConfigModel {

    /**
     * 函数计算所在的区域
     */
    public String region = "cn-shenzhen";
    /**
     * 版本号，默认LATEST
     */
    public String qualifier = "LATEST";
    /**
     * 服务名称
     */
    public String serviceName;

    /**
     * 账号类型：api/admin
     */
    public String accountType = "api";
    /**
     * 账号ID
     */
    public String accountId;
    /**
     * AccessKeyId
     */
    public String accessKeyId;
    /**
     * AccessKeySecret
     */
    public String accessKeySecret;
    /**
     * 函数请求的Endpoint
     */
    public String endPoint;
    /**
     * 专有网络vpc id，可选
     */
    public String vpcId;
    /**
     * 交换机v switch id，可选
     */
    public String vSwitchIds;
    /**
     * 安全组secure group id，可选
     */
    public String securityGroupId;


    /**
     * OSS的bucketName
     */
    public String ossBucketName;
    /**
     * OSS的外网Endpoint
     */
    public String ossEndpoint;
    /**
     * OSS的内网Endpoint
     */
    public String ossInternalEndpoint;

    /**
     * OSS临时授权的外网Endpoint
     */
    public String cloudStoreTempAuthEndPoint;
    /**
     * OSS临时授权的内网endpoint
     */
    public String cloudStoreTempAuthInternalEndPoint;
    /**
     * OSS临时授权的角色ARN
     */
    public String cloudStoreTempAuthRoleArn;
    /**
     * OSS临时授权的Session名
     */
    public String cloudStoreTempAuthRoleSessionName = "tianmian";
    /**
     * OSS临时授权的有效时间，单位：分钟。默认3600
     */
    public String cloudStoreTempAuthDurationSeconds = "3600";
}
