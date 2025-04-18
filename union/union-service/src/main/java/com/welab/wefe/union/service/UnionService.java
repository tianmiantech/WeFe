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

package com.welab.wefe.union.service;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.SM2Util;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.config.ApiBeanNameGenerator;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.common.wefe.checkpoint.CheckpointManager;
import com.welab.wefe.union.service.cache.MemberActivityCache;
import com.welab.wefe.union.service.dto.common.SM2SignedApiInput;
import com.welab.wefe.union.service.operation.UnionApiLogger;
import com.welab.wefe.union.service.service.contract.MemberContractService;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.charset.StandardCharsets;

/**
 * @author Jervis
 **/
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class,
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class,
        TransactionAutoConfiguration.class
})
@EnableScheduling
@ComponentScan(
        basePackages = {"com.welab.wefe.common.data.mongodb"},
        nameGenerator = ApiBeanNameGenerator.class,
        basePackageClasses = {
                Launcher.class,
                UnionService.class,
                CheckpointManager.class
        }
)
public class UnionService implements ApplicationContextAware {


    public static ApplicationContext CONTEXT = null;


    public static void main(String[] args) {
        Launcher.instance()
                .apiLogger(new UnionApiLogger())
                .apiPackageClass(UnionService.class)
                .apiPermissionPolicy((api, annotation, params) -> {
                    if (annotation.allowAccessWithSign()) {
                        rsaVerify(params);
                    }
                    if (annotation.sm2Verify()) {
                        sm2Verify(params);
                    }
                })
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
        signedApiInput.setSign(signedApiInput.getSign().replaceAll(" ", "+"));
        MemberMongoReop memberMongoReop = CONTEXT.getBean(MemberMongoReop.class);
        Member member = memberMongoReop.findMemberId(signedApiInput.getMemberId());
        if (member == null) {
            throw new StatusCodeWithException(StatusCode.INVALID_MEMBER, "成员不存在");
        }


        if ("1".equals(member.getFreezed())) {
            throw new StatusCodeWithException(StatusCode.INVALID_MEMBER, "该成员已被冻结，请联系管理员");
        }

        // 更新成员活跃时间
        if (Long.parseLong(member.getLastActivityTime()) < DateUtil.currentDateMillis()) {
            member.setLastActivityTime(String.valueOf(System.currentTimeMillis()));
            MemberActivityCache memberActivityCache = MemberActivityCache.getInstance();
            if (!memberActivityCache.isActivePeriod(member)) {
                MemberContractService memberContractService = UnionService.CONTEXT.getBean(MemberContractService.class);
                memberContractService.updateLastActivityTimeById(member.getMemberId(), member.getLastActivityTime());
                memberActivityCache.add(member);
            }
        }

        String publicKey = member.getPublicKey();
        SecretKeyType secretKeyType = getSecretKeyType(member);
        boolean verified = SignUtil.verify(signedApiInput.getData().getBytes(StandardCharsets.UTF_8.toString()), publicKey, signedApiInput.getSign(), secretKeyType);
        if (!verified) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "错误的签名");
        }

        params.put("cur_member_id", signedApiInput.getMemberId());
        params.remove("member_id");
        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
    }


    /**
     * SM2 Signature verify
     */
    private static void sm2Verify(JSONObject params) throws Exception {
        SM2SignedApiInput signedApiInput = params.toJavaObject(SM2SignedApiInput.class);
        UnionNodeMongoRepo unionNodeMongoRepo = CONTEXT.getBean(UnionNodeMongoRepo.class);
        UnionNode unionNode = unionNodeMongoRepo.findByBlockchainNodeId(signedApiInput.getCurrentBlockchainNodeId());
        if (unionNode == null) {
            throw new StatusCodeWithException(StatusCode.INVALID_MEMBER, "UnionNode not registered blockchainNodeId: " + signedApiInput.getCurrentBlockchainNodeId());
        }

        if ("0".equals(unionNode.getEnable())) {
            throw new StatusCodeWithException(StatusCode.INVALID_MEMBER, "UnionNode has been disabled nodeId: " + unionNode.getNodeId());
        }


        String publicKey = unionNode.getPublicKey();

        boolean verified = SM2Util.verify(signedApiInput.getData().getBytes("UTF-8"), SM2Util.getPublicKey(publicKey), signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "错误的签名");
        }
        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
        params.put("cur_blockchain_id", signedApiInput.getCurrentBlockchainNodeId());
    }


    private static SecretKeyType getSecretKeyType(Member member) {
        MemberExtJSON extJson = member.getExtJson();
        if (null == extJson || null == extJson.getSecretKeyType()) {
            return SecretKeyType.rsa;
        }
        return extJson.getSecretKeyType();
    }
}
