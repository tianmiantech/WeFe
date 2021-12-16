package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.api.feedetail.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.FeeDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeeDetailService {

    @Autowired
    private FeeDetailRepository feeDetailRepository;


    public void save(SaveApi.Input input) {

        FeeDetailMysqlModel model = feeDetailRepository.findOne("id", input.getId(), FeeDetailMysqlModel.class);

        if (null == model) {
            model = new FeeDetailMysqlModel();
        }

        model.setServiceId(input.getServiceId());
        model.setClientId(input.getClientId());
        model.setTotalFee(input.getTotalFee());
        model.setFeeConfigId(input.getFeeConfigId());
        model.setApiCallRecordId(input.getApiCallRecordId());

        feeDetailRepository.save(model);
    }


}
