package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.api.requeststatistics.QueryListApi;
import com.welab.wefe.serving.service.database.serving.entity.RequestStatisticsMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.RequestStatisticsRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


/**
 * @author ivenn.zheng
 * desc: 用于统计接口调用信息
 */
@Service
public class RequestStatisticsService {

    @Autowired
    private RequestStatisticsRepository requestStatisticsRepository;

    /**
     * query request statistics list
     * @param input
     * @return
     */
    public PagingOutput<RequestStatisticsMysqlModel> queryList(QueryListApi.Input input) {
        List<RequestStatisticsMysqlModel> list = requestStatisticsRepository.groupByServiceIdAndClientId(input.getServiceId(),
                input.getClientId(), input.getStartTime(), input.getEndTime());
        return PagingOutput.of(list.size(), list);
    }


}
