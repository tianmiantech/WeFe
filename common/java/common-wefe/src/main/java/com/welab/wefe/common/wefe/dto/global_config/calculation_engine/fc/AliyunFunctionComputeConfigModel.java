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
package com.welab.wefe.common.wefe.dto.global_config.calculation_engine.fc;

import com.welab.wefe.common.fieldvalidate.secret.MaskStrategy;
import com.welab.wefe.common.fieldvalidate.secret.Secret;
import com.welab.wefe.common.wefe.dto.global_config.base.AbstractConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.base.ConfigGroupConstant;
import com.welab.wefe.common.wefe.dto.global_config.base.ConfigModel;

/**
 * @author zane
 * @date 2021/10/29
 */
@ConfigModel(group = ConfigGroupConstant.ALIYUN_FC_CONFIG)
public class AliyunFunctionComputeConfigModel extends AbstractConfigModel {
    /**
     * 账号类型：api/admin
     */
    public String accountType = "admin";
    /**
     * 账号ID
     */
    public String accountId;
    /**
     * 函数计算所在的区域
     */
    public String region = "cn-shenzhen";
    /**
     * AccessKeyId
     */
    public String accessKeyId;
    /**
     * AccessKeySecret
     */
    @Secret(maskStrategy = MaskStrategy.PASSWORD)
    public String accessKeySecret;
    /**
     * OSS的bucketName
     */
    public String ossBucketName;

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
     * 版本号，默认LATEST，可选
     */
    public String qualifier = "LATEST";
}
