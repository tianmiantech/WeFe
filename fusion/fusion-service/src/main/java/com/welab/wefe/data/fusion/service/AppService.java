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

package com.welab.wefe.data.fusion.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.data.fusion.service.database.entity.PartnerMySqlModel;
import com.welab.wefe.data.fusion.service.service.PartnerService;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author hunter.zhao
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(
        nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {AppService.class, Launcher.class}
)
public class AppService implements ApplicationContextAware {

    public static void main(String[] args) {
        Launcher
                .instance()
                .apiPackageClass(AppService.class)
//                .checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null)
                .apiPermissionPolicy((api, annotation, params) -> {
                    if (annotation.rsaVerify()) {
                        rsaVerify(params);
                    }
                })
                .launch(AppService.class, args);
//        Launcher.instance().afterApiExecuteFunction(Launcher.CONTEXT.getBean(OperationLogAfterApiExecute.class));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Launcher.CONTEXT = applicationContext;
    }


    /**
     * Rsa Signature Check
     */
    private static void rsaVerify(JSONObject params) throws Exception {

        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        /**
         * Searching for signature Information
         */
        PartnerService partnerService = Launcher.CONTEXT.getBean(PartnerService.class);
        PartnerMySqlModel partner = partnerService.findByPartnerId(params.getString("partner_id"));

        if (partner == null) {
            throw new StatusCodeWithException("invalid partner_id：" + params.getString("partner_id"), StatusCode.PARAMETER_VALUE_INVALID);
        }


        boolean verified = RSAUtil.verify(params.getString("data").getBytes(), RSAUtil.getPublicKey(partner.getRsaPublicKey()), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.putAll(params.getJSONObject("data"));
    }
}
