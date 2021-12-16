package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.api.requeststatistics.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ivenn.zheng
 * desc: 用于统计接口调用信息
 */
@Service
public class RequestStatisticsService {

    @Autowired
    private ClientRepository clientRepository;



    public PagingOutput<QueryListApi.Output> queryList(QueryListApi.Input input) {
        String clientName = input.getClientName();
        ClientMysqlModel client = clientRepository.findOne("clientName", clientName, ClientMysqlModel.class);
        if (null != client) {

        }
        return null;
    }


}
