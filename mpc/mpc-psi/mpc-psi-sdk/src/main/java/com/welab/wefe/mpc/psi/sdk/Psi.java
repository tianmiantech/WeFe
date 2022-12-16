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

package com.welab.wefe.mpc.psi.sdk;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.service.PrivateSetIntersectionService;

import cn.hutool.core.io.FileUtil;

public abstract class Psi {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String ECDH_PSI = "ECDH_PSI";
    public static final String DH_PSI = "DH_PSI";
    public static final String PSI_RESULT = "PSI_RESULT";

    public static final int DEFAULT_CURRENT_BATCH = 0; // 第一页
    public static final int DEFAULT_BATCH_SIZE = -1; // 默认不用配置，以服务端为准

    public static final String SAVE_RESULT_DIR = System.getProperty("user.dir"); // 当前目录

    protected Map<String, String> clientDatasetMap = new LinkedHashMap<>();

    /**
     * 查询本文id集与服务器id集的集合操作
     *
     * @param config    服务器的连接信息
     * @param clientIds 本方id集
     * @return 返回一个json列表
     * @throws Exception
     * 
     */
    public List<String> query(CommunicationConfig config, List<String> clientIds) throws Exception {
        return query(config, clientIds, DEFAULT_CURRENT_BATCH);
    }

    public List<String> query(CommunicationConfig config, List<String> clientIds, int currentBatch) throws Exception {
        return query(config, clientIds, currentBatch, DEFAULT_BATCH_SIZE);
    }

    public abstract List<String> query(CommunicationConfig config, List<String> clientIds, int currentBatch,
            int batchSize) throws Exception;

    public List<String> returnFields(CommunicationConfig config) throws Exception {
        if (!config.isNeedReturnFields()) {
            return new ArrayList<>();
        }
        QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
        request.setRequestId(config.getRequestId());
        request.setType(Psi.PSI_RESULT);
        request.setClientIds(readPsiResult(config.getRequestId()));
        PrivateSetIntersectionService privateSetIntersectionService = new PrivateSetIntersectionService();
        logger.info("psi result request = " + request);
        QueryPrivateSetIntersectionResponse response = privateSetIntersectionService.handle(config, request);
        if (response.getCode() != 0) {
            logger.info("psi result response = " + response);
            throw new Exception(response.getMessage());
        }
        saveFieldResult(response.getFieldResults(), config.getRequestId());
        return response.getFieldResults();
    }

    public int[] readLastCurrentBatchAndSize(String requestId) throws Exception {
        String content = FileUtil.readString(Paths.get(SAVE_RESULT_DIR, requestId + "_currentBatch").toFile(),
                Charset.forName("utf-8").toString());
        if (StringUtils.isBlank(content)) {
            throw new Exception(requestId + "_currentBatch" + " is empty");
        } else {
            try {
                String[] arr = content.split("###");
                return new int[] { Integer.valueOf(arr[0]), Integer.valueOf(arr[1]) };
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception(requestId + "_currentBatch" + " content error : " + content);
            }
        }
    }

    public void saveLastCurrentBatchAndSize(String requestId, int currentBatch, int batchSize) {
        FileUtil.writeString(currentBatch + "###" + batchSize,
                Paths.get(SAVE_RESULT_DIR, requestId + "_currentBatch").toFile(), Charset.forName("utf-8").toString());
    }

    public void saveFieldResult(List<String> data, String requestId) {
        FileUtil.appendLines(data, Paths.get(SAVE_RESULT_DIR, requestId + "_field_result").toFile(),
                Charset.forName("utf-8").toString());
    }

    public void savePsiResult(Set<String> jsonSet, String requestId) {
        FileUtil.appendLines(jsonSet, Paths.get(SAVE_RESULT_DIR, requestId).toFile(),
                Charset.forName("utf-8").toString());
    }

    public List<String> readPsiResult(String requestId) {
        return FileUtil.readLines(Paths.get(SAVE_RESULT_DIR, requestId).toFile(), Charset.forName("utf-8").toString());
    }

    public Map<String, String> getClientDatasetMap() {
        return clientDatasetMap;
    }

    public void setClientDatasetMap(Map<String, String> clientDatasetMap) {
        this.clientDatasetMap = clientDatasetMap;
    }
}
