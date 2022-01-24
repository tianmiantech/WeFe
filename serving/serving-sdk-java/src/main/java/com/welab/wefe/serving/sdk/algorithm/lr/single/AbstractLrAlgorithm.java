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

package com.welab.wefe.serving.sdk.algorithm.lr.single;

import com.welab.wefe.serving.sdk.algorithm.AbstractAlgorithm;
import com.welab.wefe.serving.sdk.algorithm.lr.LrAlgorithmHelper;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.lr.BaseLrModel;

/**
 * @author hunter.zhao
 */
public abstract class AbstractLrAlgorithm<T extends BaseLrModel, R> extends AbstractAlgorithm<T, R> {
    public PredictModel compute(PredictParams predictParams) {
        return LrAlgorithmHelper.compute(
                modelParam.getModelParam(),
                predictParams.getUserId(),
                predictParams.getFeatureData()
        );
    }
}
