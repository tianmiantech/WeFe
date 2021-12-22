package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.api.feeconfig.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.FeeConfigRepository;
import com.welab.wefe.serving.service.enums.PayTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ivenn.zheng
 */
@Service
public class FeeConfigService {


    @Autowired
    private FeeConfigRepository feeConfigRepository;


    public void save(SaveApi.Input input) {

        FeeConfigMysqlModel model = feeConfigRepository.findOne("id", input.getId(), FeeConfigMysqlModel.class);

        if (null == model) {
            model = new FeeConfigMysqlModel();

        }

        model.setServiceId(input.getServiceId());
        model.setClientId(input.getClientId());
        if (null != input.getUnitPrice()) {
            model.setUnitPrice(input.getUnitPrice());
        }
        // 预留字段
        if (null != input.getPayType()) {
            model.setPayType(input.getPayType());
        }
        feeConfigRepository.save(model);
    }
}
