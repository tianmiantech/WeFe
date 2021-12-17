
/*
 * *
 *  * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.welab.wefe.mpc.pir.sdk;

import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;
import com.welab.wefe.mpc.pir.sdk.confuse.GenerateConfuse;
import com.welab.wefe.mpc.pir.sdk.query.PrivateInformationRetrievalClient;
import com.welab.wefe.mpc.pir.sdk.trasfer.impl.HttpTransferVariable;
import com.welab.wefe.mpc.pir.sdk.trasfer.PrivateInformationRetrievalTransferVariable;

import java.util.List;

/**
 * @Author eval
 * @Date 2021/12/15
 **/
public class PrivateInformationRetrievalQuery {
    PrivateInformationRetrievalConfig mConfig;

    public PrivateInformationRetrievalQuery(String apiName, String serverUrl, Integer targetIndex, List<Object> primaryKeys, int confuseCount, String commercialId, String signPrivateKey, GenerateConfuse generateConfuse, boolean needSign, boolean needGenerateConfuse) {
        mConfig = new PrivateInformationRetrievalConfig(apiName, serverUrl, targetIndex, primaryKeys, confuseCount, commercialId, signPrivateKey, generateConfuse, needSign, needGenerateConfuse);
    }

    public String query() throws Exception {
        PrivateInformationRetrievalTransferVariable transferVariable = new HttpTransferVariable(mConfig);
        return new PrivateInformationRetrievalClient(transferVariable, mConfig).query();
    }
}
