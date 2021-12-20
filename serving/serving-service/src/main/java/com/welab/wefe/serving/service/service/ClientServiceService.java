package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.api.clientservice.QueryApi;
import com.welab.wefe.serving.service.api.clientservice.QueryListApi;
import com.welab.wefe.serving.service.api.clientservice.SaveApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceOutputModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceQueryRepository;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
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

    public void save(SaveApi.Input input) {

        ClientServiceMysqlModel model = clientServiceRepository.findOne("id", input.getId(), ClientServiceMysqlModel.class);

        if (null == model) {
            model = new ClientServiceMysqlModel();
        }

        model.setServiceId(input.getServiceId());
        model.setClientId(input.getClientId());
        model.setUpdatedTime(new Date());

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
