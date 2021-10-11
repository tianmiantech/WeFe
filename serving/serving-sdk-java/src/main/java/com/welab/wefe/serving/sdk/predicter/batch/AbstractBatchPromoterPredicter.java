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

package com.welab.wefe.serving.sdk.predicter.batch;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.serving.sdk.dto.FederatedParams;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.sdk.dto.ProviderParams;

import java.util.List;
import java.util.Random;

/**
 * @author hunter.zhao
 */
public abstract class AbstractBatchPromoterPredicter extends AbstractBatchPredicter {

    public String seqNo;
    public String memberId;

    public AbstractBatchPromoterPredicter(String modelId,
                                          PredictParams predictParams,
                                          JSONObject params,
                                          List<ProviderParams> providers,
                                          String memberId) {
        this.modelId = modelId;
        this.predictParams = predictParams;
        this.params = params;

        this.seqNo = DateUtil.getCurrentTimeStr(DateUtil.Y4_M2_D2_H2_M2_S2) + new Random().nextInt(100000000);
        this.memberId = memberId;
        this.federatedParams = FederatedParams.of(seqNo, modelId, memberId, providers);
    }
}
