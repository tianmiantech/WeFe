package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.database.serving.entity.ApiRequestRecordMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ApiRequestRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * @author ivenn.zheng
 */
@Service
public class ApiRequestRecordService {

    @Autowired
    private ApiRequestRecordRepository apiRequestRecordRepository;


    public void save(String serviceId, String clientId, Long spend, String ipAdd, Integer requestResult) {

        ApiRequestRecordMysqlModel model = new ApiRequestRecordMysqlModel();
        model.setServiceId(serviceId);
        model.setClientId(clientId);
        model.setRequestResult(requestResult);
        model.setSpend(spend);
        model.setIpAdd(ipAdd);

        apiRequestRecordRepository.save(model);
    }









}
