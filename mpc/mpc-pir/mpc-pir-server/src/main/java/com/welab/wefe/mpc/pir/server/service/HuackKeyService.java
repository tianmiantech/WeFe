
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

package com.welab.wefe.mpc.pir.server.service;

import cn.hutool.core.thread.ThreadUtil;
import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.pir.protocol.ot.hauck.HauckTarget;
import com.welab.wefe.mpc.pir.request.QueryKeysRequest;
import com.welab.wefe.mpc.pir.request.QueryKeysResponse;
import com.welab.wefe.mpc.pir.server.event.PrivateInformationRetrievalEvent;
import com.welab.wefe.mpc.pir.server.flow.PrivateInformationRetrievalServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * @Author eval
 * @Date 2021/12/14
 **/
public class HuackKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(HuackKeyService.class);

    public QueryKeysResponse handle(QueryKeysRequest request) throws Exception {
        long start = System.currentTimeMillis();
        if (request.getIds() == null || request.getIds().isEmpty()) {
            throw new IllegalArgumentException("ids is empty");
        }
        QueryKeysResponse response = new QueryKeysResponse();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        response.setUuid(uuid);
        response.setAttemptCount(0);
        PrivateInformationRetrievalServer privateInformationRetrievalServer = new PrivateInformationRetrievalServer();
        privateInformationRetrievalServer.setUuid(uuid);
        HauckTarget hauckTarget = privateInformationRetrievalServer.mObliviousTransfer.getHauckTarget();
        response.setS(Conversion.groupElementToString(hauckTarget.s));
        PrivateInformationRetrievalEvent event = new PrivateInformationRetrievalEvent(uuid, request.getIds(), privateInformationRetrievalServer);
        ThreadUtil.execute(() -> event.getPrivateInformationRetrieval().process(event.getKeys(), request.getMethod()));
        LOG.info("uuid:{} keys cost:{}", uuid, (System.currentTimeMillis() - start));
        return response;
    }
}
