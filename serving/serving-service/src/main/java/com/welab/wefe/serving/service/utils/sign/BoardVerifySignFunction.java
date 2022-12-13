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
package com.welab.wefe.serving.service.utils.sign;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.serving.service.service.CacheObjects;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hunter.zhao
 * @date 2022/6/27
 */
public class BoardVerifySignFunction extends AbstractVerifySignFunction {

    @Override
    protected void rsaVerify(HttpServletRequest request, JSONObject params) throws Exception {


        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        if (!CacheObjects.getMemberId().equals(signedApiInput.getMemberId())) {
            throw new StatusCodeWithException("board校验失败：" + signedApiInput.getMemberId(), StatusCode.PARAMETER_VALUE_INVALID);
        }

        //boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(), RSAUtil.getPublicKey(CacheObjects.getRsaPublicKey()), signedApiInput.getSign());
        boolean verified = com.welab.wefe.common.util.SignUtil.verify(signedApiInput.getData().getBytes(), CacheObjects.getRsaPublicKey(), signedApiInput.getSign(), CacheObjects.getSecretKeyType());
        if (!verified) {
            throw new StatusCodeWithException("错误的签名", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
    }

}
