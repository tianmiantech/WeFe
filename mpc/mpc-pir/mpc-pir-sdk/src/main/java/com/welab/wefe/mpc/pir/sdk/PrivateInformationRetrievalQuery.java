
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

package com.welab.wefe.mpc.pir.sdk;

import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;
import com.welab.wefe.mpc.pir.sdk.query.PrivateInformationRetrievalClient;
import com.welab.wefe.mpc.pir.sdk.trasfer.impl.HttpTransferVariable;
import com.welab.wefe.mpc.pir.sdk.trasfer.PrivateInformationRetrievalTransferVariable;

/**
 * @Author eval
 * @Date 2021/12/15
 **/
public class PrivateInformationRetrievalQuery {

    /**
     * 匿踪查询
     * @param config 匿踪查询算法参数
     * @param communicationConfig 匿踪查询与服务器通信配置
     * @return 匿踪查询结果
     * @throws Exception
     */
    public String query(PrivateInformationRetrievalConfig config, CommunicationConfig communicationConfig) throws Exception {
        PrivateInformationRetrievalTransferVariable transferVariable = new HttpTransferVariable(communicationConfig);
        return query(config, transferVariable);
    }

    public String query(PrivateInformationRetrievalConfig config, PrivateInformationRetrievalTransferVariable transferVariable) throws Exception {
        return new PrivateInformationRetrievalClient(transferVariable, config).query();
    }
}
