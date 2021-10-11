/**
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

package com.welab.wefe.serving.sdk.predicter;

import com.welab.wefe.common.exception.StatusCodeWithException;

import java.util.Map;

/**
 * @author hunter.zhao
 */
public interface BatchPredicter extends Predicter {

    /**
     * Batch feature acquisition
     * <p>
     * The generated format must be ：
     * {"15911111111":{"x0":"0.12231","x1":"2.056412"},"15922222222":{"x0":"0.12231","x1":"2.056412"},...}
     * </>
     * @return featureDataMap
     * @throws StatusCodeWithException
     */
    Map<String, Map<String, Object>> batchFillFeatureData() throws StatusCodeWithException;
}
