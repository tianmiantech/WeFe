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

package com.welab.wefe.union.service;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.contract.data.Member;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.union.service.cache.MemberActivityCache;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jervis
 **/
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class})
@EnableScheduling
@ComponentScan(
        basePackages = {"com.welab.wefe.common.data.mongodb"},
        nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {
                Launcher.class,
                UnionService.class
        }
)
public class UnionService implements ApplicationContextAware {


    public static ApplicationContext CONTEXT = null;


    public static void main(String[] args) {

        Launcher.instance()
                .apiPackageClass(UnionService.class)
                .apiPermissionPolicy((api, annotation, params) -> {
                    if (annotation.rsaVerify()) {
                        rsaVerify(params);
                    }
                })
                .flowLimitByIpFunctionFunction((httpServletRequest, api, params) -> new FlowLimitByIpService(httpServletRequest, api, params).check())
                .flowLimitByMobileFunctionFunction((httpServletRequest, api, params) -> new FlowLimitByMobileService(httpServletRequest, api, params).check())
                .launch(UnionService.class, args);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CONTEXT = applicationContext;
    }


    /**
     * rsa Signature verify
     */
    private static void rsaVerify(JSONObject params) throws Exception {
        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);
        MemberMongoReop memberMongoReop = CONTEXT.getBean(MemberMongoReop.class);
        Member member = memberMongoReop.findMemberId(signedApiInput.getMemberId());
        if (member == null) {
            throw new StatusCodeWithException("Invalid member_id: " + signedApiInput.getMemberId(), StatusCode.INVALID_MEMBER);
        }

        if ("1".equals(member.getFreezed())) {
            throw new StatusCodeWithException("Member has been freezed member_id: " + signedApiInput.getMemberId(), StatusCode.INVALID_MEMBER);
        }

        // Due to performance issues, put it in the cache and then update it asynchronously
        member.setLastActivityTime(String.valueOf(System.currentTimeMillis()));
        MemberActivityCache.getInstance().add(member);

        String publicKey = member.getPublicKey();

        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes("UTF-8"), RSAUtil.getPublicKey(publicKey), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
        params.put("cur_member_id", signedApiInput.getMemberId());
    }

}
