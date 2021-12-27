package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.serving.service.database.serving.entity.ApiRequestRecordMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ApiRequestRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

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

    public List<ApiRequestRecordMysqlModel> getList(Date startTime, Date endTime) {

        Specification<ApiRequestRecordMysqlModel> where = Where
                .create()
                .betweenAndDate("createdTime", startTime.getTime(), endTime.getTime())
                .build(ApiRequestRecordMysqlModel.class);

        return apiRequestRecordRepository.findAll(where);
    }


}
