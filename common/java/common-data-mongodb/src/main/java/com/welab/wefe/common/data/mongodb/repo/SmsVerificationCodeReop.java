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
