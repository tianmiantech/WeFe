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

package com.welab.wefe.serving.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.serving.sdk.manager.ModelProcessorManager;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.MemberMySqlModel;
import com.welab.wefe.serving.service.database.entity.PartnerMysqlModel;
import com.welab.wefe.serving.service.database.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.database.repository.ServiceRepository;
import com.welab.wefe.serving.service.feature.CodeFeatureDataHandler;
import com.welab.wefe.serving.service.operation.ServingApiLogger;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ClientServiceService;
import com.welab.wefe.serving.service.service.MemberService;
import com.welab.wefe.serving.service.service.PartnerService;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hunter.zhao
 */
@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {Launcher.class, ServingService.class})
public class ServingService implements ApplicationContextAware {

    public static void main(String[] args) {
        Launcher
                .instance()
                .apiPackageClass(ServingService.class)
                .apiLogger(new ServingApiLogger())
                // Login status check method
//                .checkSessionTokenFunction((api, annotation, token) -> CurrentAccount.get() != null)
//                .apiPermissionPolicy((request, annotation, params) -> {
//
//                    if (!annotation.rsaVerify()) {
//                        return;
//                    }
//
//                    switch (annotation.domain()) {
//                        case Member:
//                            rsaVerifyMember(params);
//                            break;
//                        case Board:
//                            rsaVerifyBoard(params);
//                            break;
//                        case Customer:
//                            rsaVerifyCustomer(request, params);
//                            break;
//                        default:
//                            throw new RuntimeException("Unexpected enumeration value");
//                    }
//                })
                .launch(ServingService.class, args);

        //Initialize model processor
        ModelProcessorManager.init();

        //Initialize feature processor
        CodeFeatureDataHandler.init();
    }

    /**
     * rsa Signature check
     * <p>
     * customer
     * </p>
     */
    private static void rsaVerifyCustomer(HttpServletRequest request, JSONObject params) throws Exception {
        String uri = request.getRequestURI();
        String serviceUrl = uri.substring(uri.lastIndexOf("api/") + 4);
        ServiceRepository serviceRepository = Launcher.CONTEXT.getBean(ServiceRepository.class);
        ServiceMySqlModel service = serviceRepository.findOne("url", serviceUrl, ServiceMySqlModel.class);
        if (service == null) {
            throw new StatusCodeWithException("Invalid request：", StatusCode.PARAMETER_VALUE_INVALID);
        }
        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);
        /**
         * Find signature information
         */
        PartnerService partnerService = Launcher.CONTEXT.getBean(PartnerService.class);
        PartnerMysqlModel partnerMysqlModel = partnerService.queryByCode(signedApiInput.getCustomerId());

        if (partnerMysqlModel == null) {
            throw new StatusCodeWithException("Invalid customer_id：" + signedApiInput.getCustomerId(),
                    StatusCode.PARAMETER_VALUE_INVALID);
        }

        ClientServiceService clientServiceService = Launcher.CONTEXT.getBean(ClientServiceService.class);

        ClientServiceMysqlModel clientServiceMysqlModel = clientServiceService.queryByIdAndServiceId(partnerMysqlModel.getId(), service.getId());
        if (clientServiceMysqlModel == null) {
            throw new StatusCodeWithException("Invalid request", StatusCode.PARAMETER_VALUE_INVALID);
        }
        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(),
                RSAUtil.getPublicKey(clientServiceMysqlModel.getPublicKey()), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }
        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
        // params.putAll(JSONObject.parseObject(RSAUtil.decryptByPublicKey(signedApiInput.getData(), clientMysqlModel.getPubKey())));
        params.put("customer_id", signedApiInput.getCustomerId());
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Launcher.CONTEXT = applicationContext;
    }
}
