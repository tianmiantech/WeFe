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

package com.welab.wefe.common.data.mongodb.repo;

import com.welab.wefe.common.data.mongodb.constant.SmsBusinessType;
import com.welab.wefe.common.data.mongodb.entity.sms.SmsVerificationCode;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author aaron.li
 * @date 2021/10/22 09:38
 **/
@Repository
public class SmsVerificationCodeReop extends AbstractMongoRepo {

    public void saveOrUpdate(SmsVerificationCode smsVerificationCode) {
        SmsVerificationCode dbResult = find(smsVerificationCode.getMobile(), smsVerificationCode.getBusinessType());
        if (null != dbResult) {
            smsVerificationCode.setId(dbResult.getId());
            smsVerificationCode.setCode(smsVerificationCode.getCode());
            smsVerificationCode.setUpdateTime(System.currentTimeMillis());
        }
        mongoTemplate.save(smsVerificationCode);
    }

    public SmsVerificationCode find(String mobile, SmsBusinessType smsBusinessType) {
        Query query = new QueryBuilder().append("mobile", mobile).append("businessType",smsBusinessType).sort("updateTime", false).build();
        List<SmsVerificationCode> list = mongoTemplate.find(query, SmsVerificationCode.class);
        return list.isEmpty() ? null : list.get(0);
    }
}
