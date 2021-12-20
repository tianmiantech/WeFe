/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.web.dto;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Zane
 */
public class AbstractApiInput extends AbstractCheckModel {
    /**
     * 原始的全部接口请求参数
     */
    @JSONField(serialize = false)
    public JSONObject rawRequestParams;

    @Check(name = "Request way")
    @JSONField(serialize = false)
    public String method;

    @Check(name = "The request object")
    @JSONField(serialize = false)
    public HttpServletRequest request;

    /**
     * If the request comes from the Gateway, the caller's information will be here.
     */
    @JSONField(serialize = false)
    public GatewayMemberInfo callerMemberInfo;

    /**
     * Whether the request is from the Gateway
     */
    public boolean fromGateway() {
        return callerMemberInfo != null && callerMemberInfo.getMemberId() != null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
