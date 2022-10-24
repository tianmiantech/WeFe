/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.data.storage.service.fc.tencent;

import org.springframework.util.Assert;

/**
 * @author jacky.jiang
 * @date 2022/8/19
 */
public class TencentCosConfig {
    public String accessKeyId;
    public String accessKeySecret;
    public String bucketName;
    public String region;

    public TencentCosConfig(
            String accessKeyId,
            String accessKeySecret,
            String bucketName,
            String region
    ) {
        Assert.notNull(accessKeyId, "accessKeyId == null");
        Assert.notNull(accessKeySecret, "accessKeySecret == null");
        Assert.notNull(bucketName, "bucketName == null");
        Assert.notNull(region, "region == null");

        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.bucketName = bucketName;
        this.region = region;
    }
}
