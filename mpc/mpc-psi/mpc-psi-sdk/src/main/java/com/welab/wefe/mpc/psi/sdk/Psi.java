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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.psi.sdk.model.ConfuseData;
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
    protected ConfuseData confuseData = new ConfuseData();

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
        List<String> psiResult = readPsiResult(config.getRequestId());
        logger.info("psiResult size = " + psiResult.size());
        ConfuseData confuseData = getConfuseData();
        List<String> clientIds = new ArrayList<>();
        if (confuseData != null && !confuseData.isEmpty()) {
            clientIds.addAll(confuseData.getData());
            logger.info("confuseData size = " + confuseData.size());
        }
        clientIds.addAll(psiResult);
        Collections.shuffle(clientIds);
        request.setClientIds(clientIds);
        logger.info("clientIds size = " + clientIds.size());
        PrivateSetIntersectionService privateSetIntersectionService = new PrivateSetIntersectionService();
        logger.info("psi result request = " + request);
        QueryPrivateSetIntersectionResponse response = privateSetIntersectionService.handle(config, request);
        if (response.getCode() != 0) {
            logger.info("psi result response = " + response);
            throw new Exception(response.getMessage());
        }
        // 根据psiResult 过滤掉 混淆数据结果，如果confuseData不为空的话
        Set<String> fieldResult = filterConfuseData(response.getFieldResults(), psiResult, confuseData);
        saveFieldResult(fieldResult, config.getRequestId());
        config.setNeedReturnFields(false);
        return response.getFieldResults();
    }

    private Set<String> filterConfuseData(List<String> responseFieldResults, List<String> psiResults,
            ConfuseData confuseData) {
        if (confuseData == null || confuseData.isEmpty()) {
            return new HashSet<>(responseFieldResults);
        }
        Set<String> set = new HashSet<>();
        if (confuseData.isJson()) {
            Map<String, String> data1 = new HashMap<>();
            List<String> fieldNames = confuseData.getMixFieldNames();
            for (String psiResult : psiResults) {
                JSONObject json = JSONObject.parseObject(psiResult);
                StringBuilder sb = new StringBuilder();
                for (String fieldName : fieldNames) {
                    sb.append(json.getString(fieldName));
                }
                data1.put(sb.toString(), psiResult);
            }
            for (String responseFieldResult : responseFieldResults) {
                JSONObject json = JSONObject.parseObject(responseFieldResult);
                StringBuilder sb = new StringBuilder();
                for (String fieldName : fieldNames) {
                    sb.append(json.getString(fieldName));
                }
                if (data1.containsKey(sb.toString())) {
                    set.add(responseFieldResult);
                }
            }
            return set;
        } else {
            for (String responseFieldResult : responseFieldResults) {
                JSONObject json = JSONObject.parseObject(responseFieldResult);
                String value = json.getString(confuseData.getSingleFieldName());
                if (psiResults.contains(value)) {
                    set.add(responseFieldResult);
                }
            }
            return set;
        }
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

    public void saveFieldResult(Set<String> data, String requestId) {
        FileUtil.appendLines(data, Paths.get(SAVE_RESULT_DIR, requestId + "_field_result").toFile(),
                Charset.forName("utf-8").toString());
        saveInCsv(data, requestId + "_field_result" + ".csv");
    }

    public void savePsiResult(Set<String> jsonSet, String requestId) {
        FileUtil.appendLines(jsonSet, Paths.get(SAVE_RESULT_DIR, requestId).toFile(),
                Charset.forName("utf-8").toString());
        saveInCsv(jsonSet, requestId + ".csv");
    }

    private void saveInCsv(Collection<String> set, String fileName) {
        if (set == null || set.isEmpty()) {
            logger.info("save in csv, set is empty");
            return;
        }
        try {
            Iterator<String> it = set.iterator();
            String first = it.next();
            List<String> contents = new ArrayList<>();
            if (JSONObject.isValidObject(first)) {
                Map<String, Object> firstMap = JSONObject.parseObject(first).getInnerMap();
                Set<String> keys = firstMap.keySet();
                contents.add(StringUtils.join(keys, ","));
                for (String json : set) {
                    Collection<Object> values = JSONObject.parseObject(json).getInnerMap().values();
                    contents.add(StringUtils.join(values, ","));
                }
                FileUtil.appendLines(contents, Paths.get(SAVE_RESULT_DIR, fileName).toFile(),
                        Charset.forName("utf-8").toString());
            } else {
                FileUtil.appendLines(set, Paths.get(SAVE_RESULT_DIR, fileName).toFile(),
                        Charset.forName("utf-8").toString());
            }
        } catch (Exception e) {
            logger.info("save in csv error", e);
        }
    }

    public List<String> readPsiResult(String requestId) {
        List<String> result = FileUtil.readLines(Paths.get(SAVE_RESULT_DIR, requestId).toFile(),
                Charset.forName("utf-8").toString());
        return new ArrayList<>(new HashSet<>(result));
    }

    public ConfuseData getConfuseData() {
        return confuseData;
    }

    /**
     * 设置混淆数据 格式与{{requestId}}文件一致
     */
    public void setConfuseData(ConfuseData confuseData) {
        this.confuseData = confuseData;
    }

    public Map<String, String> getClientDatasetMap() {
        return clientDatasetMap;
    }

    public void setClientDatasetMap(Map<String, String> clientDatasetMap) {
        this.clientDatasetMap = clientDatasetMap;
    }
}
