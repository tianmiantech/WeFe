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

package com.welab.wefe.data.fusion.service.task;

import java.util.concurrent.CountDownLatch;

import com.welab.wefe.data.fusion.service.actuator.rsapsi.PsiServerActuator;
import com.welab.wefe.data.fusion.service.manager.ActuatorManager;
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilterUtils;
import com.welab.wefe.data.fusion.service.utils.bf.BloomFilters;

/**
 * @author hunter.zhao
 */
public class PsiServerTask extends AbstractPsiTask<PsiServerActuator> {
    private String src;
    private CountDownLatch latch;
    
    public PsiServerTask(String businessId, String src, PsiServerActuator psiServer, CountDownLatch latch) {
        super(businessId, psiServer);

        this.src = src;
        this.latch = latch;
    }


    BloomFilters findBloomFilters(String src) {
        BloomFilters bf = ActuatorManager.getBloomFilters(src);
        if(bf == null) {
            return BloomFilterUtils.readFrom(src);            
        }
        return bf;
    }

    @Override
    protected void preprocess() {
        LOG.info("preprocess start, read bf from " + src);
        long start = System.currentTimeMillis();
        BloomFilters bf = findBloomFilters(src);
        ActuatorManager.setBloomFilters(src, bf);
        actuator.fillBloomFilters(bf);
        LOG.info("preprocess end, duration = " + (System.currentTimeMillis() - start));
    }
    
    @Override
    protected void postprocess() {
        LOG.info("postprocess start");
        latch.countDown();
        LOG.info("postprocess end");
    }
}
