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

package com.welab.wefe.serving.sdk.algorithm;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.PredictResult;
import com.welab.wefe.serving.sdk.model.BaseModel;
import com.welab.wefe.serving.sdk.model.PredictModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.lang.Math.exp;

/**
 * @author Zane
 */
public abstract class AbstractAlgorithm<T, R> {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected T modelParam;

    /**
     * A single prediction
     *
     * @param predictParams
     * @return predict result
     * @throws StatusCodeWithException
     */
    protected abstract R handle(PredictParams predictParams, List<JObject> federatedResult) throws StatusCodeWithException;

    public PredictResult execute(BaseModel model, PredictParams predictParams, List<JObject> federatedResult) throws StatusCodeWithException {

        // Convert the parameter list stored in the database into a well-defined entity object
        modelParam = (T) JSON.parseObject(model.params).toJavaObject(ClassUtils.getGenericClass(getClass(), 0));

        R value = handle(predictParams, federatedResult);

        return new PredictResult(model.getAlgorithm(), model.getFlType(), model.getMyRole(), value);
    }
}
