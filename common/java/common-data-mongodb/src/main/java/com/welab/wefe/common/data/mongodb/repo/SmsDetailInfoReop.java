package com.welab.wefe.common.data.mongodb.repo;

import com.welab.wefe.common.data.mongodb.entity.sms.SmsDetailInfo;
import org.springframework.stereotype.Repository;

/**
 * @author aaron.li
 * @date 2021/10/22 11:20
 **/
@Repository
public class SmsDetailInfoReop extends AbstractMongoRepo {

    public void save(SmsDetailInfo smsDetailInfo) {
        mongoTemplate.save(smsDetailInfo);
    }
}
