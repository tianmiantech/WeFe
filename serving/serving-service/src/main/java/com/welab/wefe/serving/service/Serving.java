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

package com.welab.wefe.serving.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.common.web.service.CaptchaService;
import com.welab.wefe.serving.sdk.manager.ModelProcessorManager;
import com.welab.wefe.serving.service.database.serving.entity.MemberMySqlModel;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandle;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.MemberService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author hunter.zhao
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {Launcher.class, Serving.class})
public class Serving {

    public static void main(String[] args) {
        Launcher
                .instance()
                .apiPackageClass(Serving.class)
                // Login status check method
                .checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null)
                .apiPermissionPolicy((api, annotation, params) -> {

                    if (!annotation.rsaVerify()) {
                        return;
                    }

                    switch (annotation.domain()) {
                        case Member:
                            rsaVerifyMember(params);
                            break;
                        case Board:
                            rsaVerifyBoard(params);
                            break;
                        default:
                            throw new RuntimeException("Unexpected enumeration value");
                    }
                })
                .launch(Serving.class, args);

        //Initialize model processor
        ModelProcessorManager.init();

        //Initialize feature processor
        CodeFeatureDataHandle.init();

        //Initialize verification code memory
        CaptchaService.init();
    }

    /**
     * rsa Signature check
     * <p>
     * Federal member
     * </p>
     */
    private static void rsaVerifyMember(JSONObject params) throws Exception {


        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        /**
         * Find signature information
         */
        MemberService memberService = Launcher.CONTEXT.getBean(MemberService.class);
        MemberMySqlModel member = memberService.findOne(signedApiInput.getMemberId());

        if (member == null) {
            throw new StatusCodeWithException("Invalid member_id：" + signedApiInput.getMemberId(), StatusCode.PARAMETER_VALUE_INVALID);
        }


        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(), RSAUtil.getPublicKey(member.getPublicKey()), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
        //params.put("memberId", signedApiInput.getMemberId());
    }

    /**
     * rsa Signature check
     * <p>
     * board
     * </p>
     */
    private static void rsaVerifyBoard(JSONObject params) throws Exception {
        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        if (!CacheObjects.getMemberId().equals(signedApiInput.getMemberId())) {
            throw new StatusCodeWithException("Invalid member_id：" + signedApiInput.getMemberId(), StatusCode.PARAMETER_VALUE_INVALID);
        }


        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(), RSAUtil.getPublicKey(CacheObjects.getRsaPublicKey()), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
        //params.put("memberId", signedApiInput.getMemberId());
    }
}
