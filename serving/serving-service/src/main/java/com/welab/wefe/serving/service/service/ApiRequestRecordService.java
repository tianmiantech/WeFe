/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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
import com.welab.wefe.serving.service.api.apirequestrecord.QueryListApi;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.ApiRequestRecordMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ApiRequestRecordRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
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
import java.util.*;

/**
 * @author ivenn.zheng
 */
@Service
public class ApiRequestRecordService {

    @Autowired
    private ApiRequestRecordRepository apiRequestRecordRepository;

    @Autowired
    private Config config;

    private static final String filePrefix = "api_request_records/";


    public void save(String serviceId, String serviceName, Integer serviceType, String clientName,
                     String clientId, Long spend, String ipAdd, Integer requestResult) {

        ApiRequestRecordMysqlModel model = new ApiRequestRecordMysqlModel();
        model.setServiceId(serviceId);
        model.setClientId(clientId);
        model.setServiceName(serviceName);
        model.setClientName(clientName);
        model.setServiceType(serviceType);
        model.setRequestResult(requestResult);
        model.setSpend(spend);
        model.setIpAdd(ipAdd);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        calendar.setTime(new Date());
        model.setCreatedTime(calendar.getTime());
        apiRequestRecordRepository.save(model);
    }

    public List<ApiRequestRecordMysqlModel> getList(Date startTime, Date endTime) {

        Specification<ApiRequestRecordMysqlModel> where = Where
                .create()
                .betweenAndDate("createdTime", startTime.getTime(), endTime.getTime())
                .build(ApiRequestRecordMysqlModel.class);

        return apiRequestRecordRepository.findAll(where);
    }

    public List<ApiRequestRecordMysqlModel> getList(String serviceId, String clientId, Date startTime, Date endTime) {

        Specification<ApiRequestRecordMysqlModel> where = Where
                .create()
                .equal("serviceId", serviceId)
                .equal("clientId", clientId)
                .betweenAndDate("createdTime", startTime.getTime(), endTime.getTime())
                .build(ApiRequestRecordMysqlModel.class);

        return apiRequestRecordRepository.findAll(where);
    }

    public PagingOutput<QueryListApi.Output> getListById(QueryListApi.Input input) {
        Specification<ApiRequestRecordMysqlModel> where = Where
                .create()
                .equal("serviceId", input.getServiceId())
                .equal("clientId", input.getClientId())
                .betweenAndDate("createdTime", input.getStartTime(), input.getEndTime())
                .orderBy("createdTime", OrderBy.desc)
                .build(ApiRequestRecordMysqlModel.class);

        PagingOutput<ApiRequestRecordMysqlModel> page = apiRequestRecordRepository.paging(where, input);

        List<QueryListApi.Output> list = new ArrayList<>();
        page.getList()
                .forEach(x -> {
                    QueryListApi.Output output = ModelMapper.map(x, QueryListApi.Output.class);
                    output.setServiceType(ServiceTypeEnum.getValue(x.getServiceType()));
                    output.setRequestResult(ServiceResultEnum.getValueByCode(x.getRequestResult()));
                    list.add(output);
                });

        return PagingOutput.of(
                page.getTotal(),
                list
        );

//        return apiRequestRecordRepository.paging(where, input);

    }

//    public File downloadFile(DownloadApi.Input input) {
//        String fileName = DateUtil.getCurrentDate() + "_result.csv";
//        Specification<ApiRequestRecordMysqlModel> where = Where
//                .create()
//                .equal("serviceId", input.getServiceId())
//                .equal("clientId", input.getClientId())
//                .betweenAndDate("createdTime", input.getStartTime(), input.getEndTime())
//                .orderBy("createdTime", OrderBy.desc)
//                .build(ApiRequestRecordMysqlModel.class);
//
//        List<ApiRequestRecordMysqlModel> all = apiRequestRecordRepository.findAll(where);
//        try {
//            return writeCSV(all, fileName);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public File writeCSV(List<ApiRequestRecordMysqlModel> dataList, String fileName) throws IOException {
        final StringWriter sw = new StringWriter();
        CsvWriter csvWriter = CsvWriter.builder()
                .fieldSeparator(',')
                .quoteStrategy(QuoteStrategy.EMPTY)
                .lineDelimiter(LineDelimiter.LF)
                .build(sw);

        csvWriter.writeRow("Id", "服务Id", "服务名称", "服务类型", "客户Id", "客户名称",
                "调用IP", "请求结果", "请求时间");

        for (ApiRequestRecordMysqlModel model : dataList) {
            csvWriter.writeRow(
                    model.getId(),
                    model.getServiceId(),
                    model.getServiceName(),
                    ServiceTypeEnum.getValue(model.getServiceType()),
                    model.getClientId(),
                    model.getClientName(),
                    model.getIpAdd(),
                    ServiceResultEnum.getValueByCode(model.getRequestResult()),
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
