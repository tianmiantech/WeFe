package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.service.api.feeconfig.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.FeeConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author ivenn.zheng
 */
@Service
public class FeeConfigService {


    @Autowired
    private FeeConfigRepository feeConfigRepository;


    public FeeConfigMysqlModel save(SaveApi.Input input) {

        FeeConfigMysqlModel model = feeConfigRepository.findOne("id", input.getId(), FeeConfigMysqlModel.class);

        if (null == model) {
            model = new FeeConfigMysqlModel();

        }

        if (null != input.getUnitPrice()) {
            model.setUnitPrice(input.getUnitPrice());
        }
        // 预留字段
        if (null != input.getPayType()) {
            model.setPayType(input.getPayType());
        }
        return feeConfigRepository.save(model);
    }


    public FeeConfigMysqlModel queryOne(String serviceId, String clientId) {

        if (StringUtil.isNotEmpty(serviceId) && StringUtil.isNotEmpty(clientId)) {

            Specification<FeeConfigMysqlModel> where = Where
                    .create()
                    .equal("serviceId", serviceId)
                    .equal("clientId", clientId)
                    .build(FeeConfigMysqlModel.class);


            Optional<FeeConfigMysqlModel> one = feeConfigRepository.findOne(where);
            return one.get();
        } else {

        }
        return null;

    }
}
