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
package com.welab.wefe.board.service.dto.globalconfig;

import com.welab.wefe.board.service.dto.globalconfig.base.AbstractConfigModel;
import com.welab.wefe.board.service.dto.globalconfig.base.ConfigGroupConstant;
import com.welab.wefe.board.service.dto.globalconfig.base.ConfigModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

/**
 * 阿里云短信通道
 *
 * @author zane
 * @date 2022/5/27
 */
@ConfigModel(group = ConfigGroupConstant.ALIYUN_SMS_CHANNEL)
public class AliyunSmsChannelConfigModel extends AbstractConfigModel {
    @Check(require = true)
    public String accessKeyId;

    @Check(require = true)
    public String accessKeySecret;

    @Check(name = "短信签名", require = true)
    public String signName;

    @Check(name = "找回密码短信模板码", require = true)
    public String retrievePasswordTemplateCode;

}
