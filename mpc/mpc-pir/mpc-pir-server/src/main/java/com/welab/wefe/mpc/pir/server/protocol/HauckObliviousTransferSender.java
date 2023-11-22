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

package com.welab.wefe.mpc.pir.server.protocol;

import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupElement;
import com.welab.wefe.mpc.pir.protocol.ot.ObliviousTransfer;
import com.welab.wefe.mpc.pir.protocol.ot.ObliviousTransferKey;
import com.welab.wefe.mpc.pir.protocol.ot.hauck.HauckObliviousTransfer;
import com.welab.wefe.mpc.pir.protocol.ot.hauck.HauckTarget;
import com.welab.wefe.mpc.pir.server.cache.HauckTargetCache;
import com.welab.wefe.mpc.pir.server.trasfer.PrivateInformationRetrievalTransferVariable;
import com.welab.wefe.mpc.pir.server.trasfer.impl.CacheTransferVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author eval
 */
public class HauckObliviousTransferSender extends HauckObliviousTransfer implements ObliviousTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(HauckObliviousTransferSender.class);

    HauckTarget mHauckTarget = null;

    PrivateInformationRetrievalTransferVariable mTransferVariable = new CacheTransferVariable();

    public HauckObliviousTransferSender(String uuid) {
        super(uuid);
    }

    @Override
    public List<ObliviousTransferKey> keyDerivation(int targetNum) {
        LOG.info("uuid:{} keyDerivation start", uuid);
        HauckTarget hauckTarget = mHauckTarget;
        if (mHauckTarget == null) {
            hauckTarget = getHauckTarget();
        }
        BigInteger y = hauckTarget.y;
        GroupElement s = hauckTarget.s;
        GroupElement t = hauckTarget.t;
        int attemptCount = 0;
        while (true) {
            if (mHauckTarget == null) {
                mTransferVariable.processHauckRandom(uuid, attemptCount, Conversion.groupElementToString(s));
            }
            LOG.info("uuid:{} get s_legal", uuid);
            boolean legal = mTransferVariable.processHauckRandomLegal(uuid, attemptCount);
            if (legal) {
                break;
            } else {
                hauckTarget = getHauckTarget();
                y = hauckTarget.y;
                s = hauckTarget.s;
                t = hauckTarget.t;
                attemptCount += 1;
            }
        }
        LOG.info("uuid:{} keyDerivation get r", uuid);
        GroupElement r = getGroupElement(mTransferVariable.processClientRandom(uuid));
        LOG.info("uuid:{} keyDerivation init mac", uuid);
        initMac(s, r);
        LOG.info("uuid:{} keyDerivation calc yt an yr", uuid);
        final BigInteger paramY = y;
        final GroupElement paramT = t;
        CompletableFuture<GroupElement> calcYT = CompletableFuture.supplyAsync(() -> arithmetic.mul(paramY, paramT));
        CompletableFuture<GroupElement> calcYR = CompletableFuture.supplyAsync(() -> arithmetic.mul(paramY, r));
        CompletableFuture.allOf(calcYR, calcYT).join();
        GroupElement yt = null;
        GroupElement yr = null;
        try {
            yt = calcYT.get();
            yr = calcYR.get();
        } catch (Exception e) {
            LOG.info(uuid + e.getMessage(), e);
        }
        LOG.info("uuid:{} keyDerivation calc key list", uuid);
        List<ObliviousTransferKey> keyList = new ArrayList<>(targetNum);
        for (int i = 0; i < targetNum; i++) {
            GroupElement iyt = arithmetic.mul(BigInteger.valueOf(i), yt);
            GroupElement diff = arithmetic.sub(yr, iyt);
            byte[] key = macTecElement(diff);
            keyList.add(new ObliviousTransferKey(i, key));
        }
        LOG.info("uuid:{} keyDerivation finish", uuid);
        return keyList;
    }

    /**
     * 先从缓存里面取HauckTarget，若没有再生成
     *
     * @return
     */
    @Override
    public HauckTarget getHauckTarget() {
        HauckTarget hauckTarget = HauckTargetCache.getInstance().get();
        if (hauckTarget == null) {
            hauckTarget = generateHauckTarget();
        }
        mHauckTarget = hauckTarget;
        return hauckTarget;
    }
}
