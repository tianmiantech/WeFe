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

package com.welab.wefe.service;

import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.exception.BusinessException;
import com.welab.wefe.util.WechatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.util.Date;

/**
 * @author aaron.li
 * @date 2021/12/31 11:12
 **/
public class BaseService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Value("${wechat.bot-url}")
    private String wechatUrl;

    /**
     * Send error warning
     */
    public void sendErrorMsg(int groupId, long blockNumber, Exception e) {
        String errorMsg;
        if (e instanceof BusinessException) {
            errorMsg = "Warning!!!, Business exception! Business exception! Business exception! Important things are to be repeated for 3 times! Sync group id: " + groupId + ", block number: " + blockNumber + ", data exception: " + e.getMessage();
        } else {
            errorMsg = "Sync group id: " + groupId + ", block number " + blockNumber + ", data exception: " + e.getMessage();
        }
        errorMsg += "\n\n" + DateUtil.toStringYYYY_MM_DD_HH_MM_SS2(new Date());
        WechatUtil.send(wechatUrl, errorMsg);
    }
}
