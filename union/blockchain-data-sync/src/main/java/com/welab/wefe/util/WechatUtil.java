/**
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

package com.welab.wefe.util;

import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wechat util
 *
 * @author aaron.li
 **/
public class WechatUtil {
    private static final Logger LOG = LoggerFactory.getLogger(WechatUtil.class);

    /**
     * Send message to WeChat
     */
    public static void send(String url, String message) {
        try {
            if (StringUtil.isEmpty(url) || StringUtil.isEmpty(message)) {
                return;
            }
            JObject body = JObject.create()
                    .append("msgtype", "text")
                    .append("text", JObject.create().append("content", message));

            HttpResponse response = HttpRequest.create(url)
                    .appendParameters(body)
                    .setRetryCount(3)
                    .postJson();
            LOG.info(response.getBodyAsString());
        } catch (Exception e) {
            LOG.error("Exception sending message to enterprise wechat：", e);
        }
    }
}
