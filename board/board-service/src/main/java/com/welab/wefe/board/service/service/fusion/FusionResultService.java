/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.service.fusion;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.welab.wefe.board.service.api.project.fusion.result.ResultExportApi;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.dto.fusion.FusionResultExportProgress;
import com.welab.wefe.board.service.fusion.manager.ExportManager;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.jdbc.JdbcClient;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;

/**
 * @author hunter.zhao
 */
@Service
public class FusionResultService extends AbstractService {

    @Autowired
    FusionTaskService fusionTaskService;

    @Autowired
    FusionResultStorageService fusionResultStorageService;

    public String export(ResultExportApi.Input input) throws Exception {

        FusionTaskMySqlModel taskMySqlModel = fusionTaskService.findByBusinessId(input.getBusinessId());
        if (taskMySqlModel == null) {
            StatusCode.DATA_NOT_FOUND.throwException();
        }

        //table header
        DataItemModel headerModel = fusionResultStorageService.getByKey(
                Constant.DBName.WEFE_DATA,
                fusionResultStorageService.createRawDataSetTableName(input.getBusinessId()) + ".meta",
                "header"
        );
        List<String> columns = StringUtil.splitWithoutEmptyItem(headerModel.getV().toString().replace("\"", ""), ",");
        LOG.info("begin getList from ck");
        long start = System.currentTimeMillis();
        List<DataItemModel> allList = fusionResultStorageService.getList(fusionResultStorageService.createRawDataSetTableName(input.getBusinessId()));
        LOG.info("end getList from ck, duration = " + (System.currentTimeMillis() - start));
        JdbcClient client = JdbcClient.create(
                input.getDatabaseType(),
                input.getHost(),
                input.getPort(),
                input.getUserName(),
                input.getPassword(),
                input.getDatabaseName()
        );

        String tableName = "fusion_result_" + input.getBusinessId() + "_" + DateUtil.toString(new Date(), DateUtil.Y4_M2_D2_H2_M2_S2);
        create(columns, client, tableName);

        FusionResultExportProgress progress = new FusionResultExportProgress(input.getBusinessId(), tableName, allList.size());
        ExportManager.set(input.getBusinessId(), progress);
        int partitionSize = 500000;
        int taskNum = Math.max(allList.size() / partitionSize, 1);
        List<List<DataItemModel>> lists = partitionList(allList, taskNum);
        ExecutorService executorService1 = Executors.newFixedThreadPool(5);
        for (int i = 0; i < lists.size(); i++) {
            List<DataItemModel> subList = lists.get(i);
            final int finalI = i;
            executorService1.submit(() -> {
                try {
                    LOG.info("begin writerBatch index = " + finalI + ", partition size = " + subList.size());
                    writerBatch(columns, subList, client, tableName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
//        todo:（Winter）这里建议使用 client.saveBatch() 进行批量写入。
//        allList.forEach(x -> {
//            CommonThreadPool.run(
//                    () -> {
//                        try {
//                            writer(columns, x, client, tableName);
//                            progress.increment();
//                        } catch (Exception e) {
//                            progress.setStatus(ExportStatus.failure);
//                            e.printStackTrace();
//                            return;
//                        }
//                    }
//            );
//        });
        return tableName;
    }

    private void create(List<String> headers, JdbcClient client, String tableName) throws StatusCodeWithException {
        String sql = String.format("CREATE TABLE %s (", tableName);
        StringBuilder s = new StringBuilder(sql);
        for (String row : headers) {
            s.append(row).append(" VARCHAR(32) NOT NULL,");
        }

        if (s.length() > 0) {
            s.deleteCharAt(s.length() - 1).append(")");
        }
        try {
            client.execute(s.toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new StatusCodeWithException(StatusCode.SQL_ERROR, "create table error:" + e.getMessage());
        }
    }


    private void writer(List<String> headers, DataItemModel model, JdbcClient client, String tableName) throws Exception {
        StringBuilder sql = new StringBuilder().append("INSERT INTO  " + tableName + "(");
        headers.forEach(
                x -> {
                    sql.append(x + ",");
                }
        );
        sql.deleteCharAt(sql.length() - 1).append(") value (");

        List<String> list = Lists.newArrayList();
        list.add(String.valueOf(model.getK()));
        list.addAll(StringUtil.splitWithoutEmptyItem(String.valueOf(model.getV()), ","));
        list.forEach(x -> sql.append(x + ","));

        sql.deleteCharAt(sql.length() - 1).append(")");

        client.execute(sql.toString());
    }

    private void writerBatch(List<String> headers, List<DataItemModel> models, JdbcClient client, String tableName)
            throws Exception {
        StringBuilder sql = new StringBuilder().append("INSERT INTO  " + tableName + "(");
        headers.forEach(x -> {
            sql.append(x + ",");
        });
        sql.deleteCharAt(sql.length() - 1).append(") value (");
        headers.forEach(x -> {
            sql.append("?,");
        });
        sql.deleteCharAt(sql.length() - 1).append(")");
        LOG.info("writerBatch sql = " + sql.toString());

        client.saveBatch(sql.toString(), models, (s) -> {
            List<String> list = Lists.newArrayList();
            list.add(String.valueOf(s.getK()));
            list.addAll(StringUtil.splitWithoutEmptyItem(String.valueOf(s.getV()), ","));
            return list.toArray();
        });
    }
    
    /**
     * 分片
     */
    public static <T> List<List<T>> partitionList(List<T> list, int numPartitions) {
        if (list == null) {
            throw new NullPointerException("The set must not be null");
        }
        List<List<T>> partitions = new ArrayList<>(numPartitions);
        for (int i = 0; i < numPartitions; i++)
            partitions.add(i, new ArrayList<>());

        int size = list.size();
        int partitionSize = (int) Math.ceil((double) size / numPartitions);
        if (numPartitions <= 0)
            throw new IllegalArgumentException("'numPartitions' must be greater than 0");

        Iterator<T> iterator = list.iterator();
        int partitionToWrite = 0;
        int cont = 0;
        while (iterator.hasNext()) {
            partitions.get(partitionToWrite).add(iterator.next());
            cont++;
            if (cont >= partitionSize) {
                partitionToWrite++;
                cont = 0;
            }
        }
        return partitions;
    }
}
