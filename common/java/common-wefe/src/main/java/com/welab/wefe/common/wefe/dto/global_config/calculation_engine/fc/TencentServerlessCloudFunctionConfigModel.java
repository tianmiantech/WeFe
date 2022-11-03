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
 * 腾讯云函数计算
 *
 * @author zane
 * @date 2022/06/16
 */
@ConfigModel(group = ConfigGroupConstant.TENCENT_SCF_CONFIG)
public class TencentServerlessCloudFunctionConfigModel extends AbstractConfigModel {

    /**
     * 账号ID
     */
    public String accountId;
    /**
     * 函数计算所在的区域
     */
    public String region = "ap-guangzhou";
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
     * COS 的 bucketName
     */
    public String cosBucketName;

    /**
     * 版本号，默认LATEST，可选
     */
    public String qualifier = "LATEST";
    /**
     * scf 服务地址
     * <p>
     * eg: https://service-xxxxxx-xxxxxxxxx.gz.apigw.tencentcs.com/release/invoke
     */
    public String scfServerUrl;
}
