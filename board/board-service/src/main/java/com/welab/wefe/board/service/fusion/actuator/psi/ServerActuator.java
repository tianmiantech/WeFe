package com.welab.wefe.board.service.fusion.actuator.psi;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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


import com.alibaba.fastjson.JSON;
import com.welab.wefe.board.service.fusion.actuator.PsiDumpHelper;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.fusion.core.actuator.psi.AbstractPsiServerActuator;
import com.welab.wefe.fusion.core.enums.FusionTaskStatus;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

/**
 * @author hunter.zhao
 */
public class ServerActuator extends AbstractPsiServerActuator {
    public ServerActuator(String businessId, BloomFilters bloomFilters, BigInteger n, BigInteger e, BigInteger d, BigInteger p, BigInteger q, Long dataCount) {
        super(businessId, bloomFilters, n, e, d, p, q, dataCount);
    }

    @Override
    public void dump(List<JObject> fruit) {
        LOG.info("fruit insert ready..., fruit size = " + fruit.size());

        Set<String> headers = new HashSet<>();
        if (fruit.isEmpty()) {
            return;
        }

        for (String header : fruit.get(0).keySet()) {
            headers.add(header);
        }

        try {
            PsiDumpHelper.dump(businessId, headers, fruit);
        } catch (Exception ex) {
            LOG.error(ex.getClass().getSimpleName() + " " + ex.getMessage(), ex);
        }

        LOG.info("fruit insert end...");
    }

    @Override
    public void close() throws Exception {

        //update task status
        FusionTaskService fusionTaskService = Launcher.CONTEXT.getBean(FusionTaskService.class);
        switch (status) {
            case success:
                fusionTaskService.updateByBusinessId(
                        businessId,
                        FusionTaskStatus.Success,
                        dataCount,
                        fusionCount.longValue(),
                        processedCount.longValue(),
                        getSpend()
                );
                break;
            case falsify:
            case running:
                fusionTaskService.updateErrorByBusinessId(
                        businessId,
                        FusionTaskStatus.Interrupt,
                        dataCount,
                        fusionCount.longValue(),
                        processedCount.longValue(),
                        getSpend(),
                        error
                );
                break;
            default:
                fusionTaskService.updateErrorByBusinessId(
                        businessId,
                        FusionTaskStatus.Failure,
                        dataCount,
                        fusionCount.longValue(),
                        processedCount.longValue(),
                        getSpend(),
                        error
                );
                break;
        }
    }
}
