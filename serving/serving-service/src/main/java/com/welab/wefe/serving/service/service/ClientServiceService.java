package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.api.clientservice.QueryApi;
import com.welab.wefe.serving.service.api.clientservice.QueryListApi;
import com.welab.wefe.serving.service.api.clientservice.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceOutputModel;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceQueryRepository;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.database.serving.repository.FeeConfigRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * @author ivenn.zheng
 */
@Service
public class ClientServiceService {


    @Autowired
    private ClientServiceRepository clientServiceRepository;

    @Autowired
    private ClientServiceQueryRepository clientServiceQueryRepository;

    @Autowired
    private FeeConfigRepository feeConfigRepository;

    public void save(SaveApi.Input input) {

        ClientServiceMysqlModel model = clientServiceRepository.findOne("id", input.getId(), ClientServiceMysqlModel.class);

        if (null == model) {
            model = new ClientServiceMysqlModel();
        }

        if (null != input.getClientId()) {
            model.setClientId(input.getClientId());

        }
        if (null != input.getServiceId()) {
            model.setServiceId(input.getServiceId());

        }
        model.setUpdatedTime(new Date());
        if (null != input.getStatus()) {
            model.setStatus(input.getStatus());
        }

        if (null != input.getFeeConfigId()) {
            FeeConfigMysqlModel feeConfigMysqlModel = feeConfigRepository.findOne("id", input.getFeeConfigId(), FeeConfigMysqlModel.class);
            // 暂不设置付费类型
            // feeConfigMysqlModel.setPayType();
            feeConfigMysqlModel.setUnitPrice(null != input.getUnitPrice() ? input.getUnitPrice() : feeConfigMysqlModel.getUnitPrice());
            FeeConfigMysqlModel feeConfigModel = feeConfigRepository.save(feeConfigMysqlModel);
            model.setFeeConfigId(feeConfigModel.getId());
        }

        clientServiceRepository.save(model);

    }


    public PagingOutput<ClientServiceOutputModel> queryList(QueryListApi.Input input) {
        List<ClientServiceOutputModel> list = clientServiceQueryRepository.queryClientServiceList(input.getServiceName(), input.getClientName(), input.getStatus());
        return PagingOutput.of(list.size(), list);
    }

    public ClientServiceOutputModel queryOne(QueryApi.Input input) {
        return clientServiceQueryRepository.queryOne(input.getId());

    }


}
