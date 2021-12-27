package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.database.serving.entity.FeeDetailMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.FeeDetailRepository;
import com.welab.wefe.serving.service.dto.FeeDetailInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class FeeDetailService {

    @Autowired
    private FeeDetailRepository feeDetailRepository;

    public void save(FeeDetailMysqlModel input) {

        FeeDetailMysqlModel model = feeDetailRepository.findOne("id", input.getId(), FeeDetailMysqlModel.class);
        if (null == model) {
            model = new FeeDetailMysqlModel();
        }

        model.setServiceId(input.getServiceId());
        model.setClientId(input.getClientId());
        model.setTotalFee(input.getTotalFee());
        model.setTotalRequestTimes(input.getTotalRequestTimes());
        model.setCreatedTime(new Date());

        feeDetailRepository.save(model);
    }


}
