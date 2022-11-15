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
package com.welab.wefe.fusion.core.actuator.psi;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;

import java.util.List;

/**
 * @author hunter.zhao
 */
public interface PsiClientInterface {

    void init() throws Exception;

    /**
     * 下载布隆过滤器参数
     *
     * @return
     * @throws StatusCodeWithException
     */
    PsiActuatorMeta downloadActuatorMeta() throws StatusCodeWithException;

    /**
     * RSA-PSI 数据转换
     *
     * @param bsList
     * @return
     * @throws StatusCodeWithException
     */
    byte[][] dataTransform(byte[][] bsList) throws StatusCodeWithException;

    /**
     * Send alignment data to the server
     */
    void sendFusionDataToServer(List<JObject> rs);

    /**
     * Alignment data into the library implementation method
     *
     * @param fruit
     */
    void dump(List<JObject> fruit);

    /**
     * 主键hash方法
     *
     * @param value
     * @return
     */
    String hashValue(JObject value);

    /**
     * 通知server执行器关闭任务
     */
    void notifyServerClose();

    /**
     * 桶数
     *
     * @return
     */
    int bucketSize();

    /**
     * Paging data fetching
     *
     * @param index
     * @return
     */
    List<JObject> getBucketByIndex(int index);

    /**
     * 判断server是否准备好
     *
     * @param businessId
     * @return
     */
    boolean isServerReady(String businessId);
}
