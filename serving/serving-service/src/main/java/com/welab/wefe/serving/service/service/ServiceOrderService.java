/*
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.serving.service.service;

import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.serviceorder.DownloadApi;
import com.welab.wefe.serving.service.api.serviceorder.QueryListApi;
import com.welab.wefe.serving.service.api.serviceorder.SaveApi;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.ServiceOrderMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ServiceOrderRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.dto.ServiceOrderInput;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.enums.ServiceResultEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;
import de.siegmar.fastcsv.writer.QuoteStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ivenn.zheng
 * @date 2022/4/27
 */
@Service
public class ServiceOrderService {

    @Autowired
    private Config config;

    private static final String filePrefix = "service_order/";

    @Autowired
    ServiceOrderRepository serviceOrderRepository;

    public void save(SaveApi.Input input) {

        ServiceOrderMysqlModel model = serviceOrderRepository.findOne("id", input.getId(), ServiceOrderMysqlModel.class);
        if (null == model) {
            model = new ServiceOrderMysqlModel();
            input.setId(model.getId());
        }
        model = ModelMapper.map(input, ServiceOrderMysqlModel.class);
        model.setUpdatedBy(input.getUpdatedBy());
        model.setUpdatedTime(new Date(System.currentTimeMillis()));

        serviceOrderRepository.save(model);
    }

    public PagingOutput<QueryListApi.Output> queryList(QueryListApi.Input input) {

        Specification<ServiceOrderMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .contains("serviceName", input.getServiceName())
                .equal("serviceType", input.getServiceType())
                .equal("orderType", input.getOrderType())
                .equal("status", input.getStatus())
                .contains("requestPartnerName", input.getRequestPartnerName())
                .contains("responsePartnerName", input.getResponsePartnerName())
                .betweenAndDate("createdTime", input.getStartTime() == null ? null : input.getStartTime().getTime(), input.getEndTime() == null ? null : input.getEndTime().getTime())
                .build(ServiceOrderMysqlModel.class);

        PagingOutput<ServiceOrderMysqlModel> models = serviceOrderRepository.paging(where, input);
        List<QueryListApi.Output> list = new ArrayList<>();
        models.getList().forEach(x -> {
            QueryListApi.Output output = ModelMapper.map(x, QueryListApi.Output.class);
            list.add(output);
        });
        return PagingOutput.of(list.size(), list);

    }

    /**
     * 根据参数获取列表, 且不查询进行中的订单
     *
     * @param input
     * @return
     */
    public List<ServiceOrderMysqlModel> getByParams(ServiceOrderInput input) {
        Specification<ServiceOrderMysqlModel> where = Where.create()
                .equal("serviceId", input.getServiceId())
                .contains("serviceName", input.getServiceName())
                .equal("requestPartnerId", input.getRequestPartnerId())
                .contains("requestPartnerName", input.getRequestPartnerName())
                .equal("responsePartnerId", input.getResponsePartnerId())
                .contains("responsePartnerName", input.getResponsePartnerName())
                .notEqual("status", input.getStatus())
                .equal("orderType", input.getOrderType())
                .betweenAndDate("createdTime", input.getCreatedStartTime() == null ? null : input.getCreatedStartTime().getTime(), input.getCreatedEndTime() == null ? null : input.getCreatedEndTime().getTime())
                .betweenAndDate("updatedTime", input.getUpdatedStartTime() == null ? null : input.getUpdatedStartTime().getTime(), input.getUpdatedEndTime() == null ? null : input.getUpdatedEndTime().getTime())
                .build(ServiceOrderMysqlModel.class);

        return serviceOrderRepository.findAll(where);

    }


    public File downloadFile(DownloadApi.Input input) {
        String fileName = DateUtil.getCurrentDate() + "_result.csv";
        Specification<ServiceOrderMysqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .equal("serviceId", input.getServiceId())
                .contains("serviceName", input.getServiceName())
                .equal("serviceType", input.getServiceType())
                .equal("orderType", input.getOrderType())
                .equal("status", input.getStatus())
                .contains("requestPartnerName", input.getRequestPartnerName())
                .contains("responsePartnerName", input.getResponsePartnerName())
                .betweenAndDate("createdTime", input.getStartTime().getTime(), input.getEndTime().getTime())
                .orderBy("createdTime", OrderBy.desc)
                .build(ServiceOrderMysqlModel.class);

        List<ServiceOrderMysqlModel> all = serviceOrderRepository.findAll(where);
        try {
            return writeCSV(all, fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File writeCSV(List<ServiceOrderMysqlModel> dataList, String fileName) throws IOException {
        final StringWriter sw = new StringWriter();
        CsvWriter csvWriter = CsvWriter.builder()
                .fieldSeparator(',')
                .quoteStrategy(QuoteStrategy.EMPTY)
                .lineDelimiter(LineDelimiter.LF)
                .build(sw);

        csvWriter.writeRow("订单Id", "服务Id", "服务名称", "服务类型", "是否为己方生成的订单", "订单状态",
                "调用方Id", "调用方名称", "响应方Id", "响应方名称", "创建时间");

        for (ServiceOrderMysqlModel model : dataList) {
            csvWriter.writeRow(
                    model.getId(),
                    model.getServiceId(),
                    model.getServiceName(),
                    model.getServiceType(),
                    model.getOrderType().toString(),
                    model.getStatus(),
                    model.getRequestPartnerId(),
                    model.getRequestPartnerName(),
                    model.getResponsePartnerId(),
                    model.getResponsePartnerName(),
                    DateUtil.toString(model.getCreatedTime(), DateUtil.YYYY_MM_DD_HH_MM_SS2)
            );
        }

        File csvFile = new File(config.getFileBasePath() + filePrefix + fileName);
        if (!csvFile.exists()) {
            File file = new File(csvFile.getParent());
            if (!file.exists()) {
                file.mkdirs();
            }
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8));
        bw.write('\ufeff');
        bw.write(sw.toString());
        bw.flush();
        bw.close();

        return csvFile;
    }

}
