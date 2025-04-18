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

package com.welab.wefe.serving.sdk.algorithm.lr.single;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.lr.BaseLrModel;
import com.welab.wefe.serving.sdk.model.lr.LrPredictResultModel;

import java.util.List;

/**
 * Transverse federal
 *
 * @author hunter.zhao
 */
public class LrHorzPromoterAlgorithm extends AbstractLrAlgorithm<BaseLrModel, PredictModel> {

    @Override
    protected PredictModel handle(PredictParams predictParams, List<JObject> federatedResult) {
        LrPredictResultModel predictModel = localCompute(predictParams);

        if (StringUtil.isNotEmpty(predictModel.getError())) {
            return predictModel;
        }

        return normalize(predictModel);
    }
}
