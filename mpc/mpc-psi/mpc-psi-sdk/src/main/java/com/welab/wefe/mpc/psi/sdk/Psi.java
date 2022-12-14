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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.welab.wefe.mpc.config.CommunicationConfig;

import cn.hutool.core.io.FileUtil;

public abstract class Psi {

    public static final String ECDH_PSI = "ECDH_PSI";
    public static final String DH_PSI = "DH_PSI";

    public static final int DEFAULT_CURRENT_BATCH = 0; // 第一页
    public static final int DEFAULT_BATCH_SIZE = -1; // 默认不用配置，以服务端为准

    public static final String SAVE_RESULT_DIR = System.getProperty("user.dir"); // 当前目录

    public abstract List<String> query(CommunicationConfig config, List<String> clientIds) throws Exception;

    public abstract List<String> query(CommunicationConfig config, List<String> clientIds, int currentBatch)
            throws Exception;

    public abstract List<String> query(CommunicationConfig config, List<String> clientIds, int currentBatch,
            int batchSize) throws Exception;

    public int[] readLastCurrentBatchAndSize(String requestId) throws Exception {
        String content = FileUtil.readString(Paths.get(SAVE_RESULT_DIR, requestId + "_currentBatch").toFile(),
                Charset.forName("utf-8").toString());
        if (StringUtils.isBlank(content)) {
            throw new Exception(requestId + "_currentBatch" + " is empty");
        } else {
            try {
                String[] arr = content.split("-");
                return new int[] { Integer.valueOf(arr[0]), Integer.valueOf(arr[1]) };
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception(requestId + "_currentBatch" + " content error : " + content);
            }
        }
    }
}
