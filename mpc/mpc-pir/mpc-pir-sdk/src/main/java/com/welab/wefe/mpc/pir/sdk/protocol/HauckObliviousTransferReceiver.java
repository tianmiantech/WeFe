
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

package com.welab.wefe.mpc.pir.sdk.protocol;

import cn.hutool.core.util.StrUtil;
import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupArithmetic;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupElement;
import com.welab.wefe.mpc.pir.protocol.ot.ObliviousTransfer;
import com.welab.wefe.mpc.pir.protocol.ot.ObliviousTransferKey;
import com.welab.wefe.mpc.pir.protocol.ot.hauck.HauckObliviousTransfer;
import com.welab.wefe.mpc.pir.request.QueryRandomLegalRequest;
import com.welab.wefe.mpc.pir.request.QueryRandomLegalResponse;
import com.welab.wefe.mpc.pir.request.QueryRandomRequest;
import com.welab.wefe.mpc.pir.sdk.crypt.CryptUtil;
import com.welab.wefe.mpc.pir.sdk.trasfer.PrivateInformationRetrievalTransferVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author eval
 */
public class HauckObliviousTransferReceiver extends HauckObliviousTransfer implements ObliviousTransfer {
    private static final Logger LOG = LoggerFactory.getLogger(HauckObliviousTransferReceiver.class);

    private PrivateInformationRetrievalTransferVariable mTransferVariable;

    String firstS;

    public HauckObliviousTransferReceiver(String uuid) {
        super(uuid);
    }

    public HauckObliviousTransferReceiver(String uuid, String s, PrivateInformationRetrievalTransferVariable transferVariable) {
        super(uuid);
        firstS = s;
        mTransferVariable = transferVariable;
    }

    @Override
    public List<ObliviousTransferKey> keyDerivation(int target) {
        LOG.info("uuid:{} keyDerivation start", uuid);
        BigInteger x = genRandomScalar();
        LOG.info("uuid:{} keyDerivation generator random x", uuid);

        int attemptCount = 0;
        GroupElement s = null;
        while (true) {
            if (!StrUtil.isEmpty(firstS)) {
                s = getGroupElement(firstS);
            } else {
                QueryRandomRequest request = new QueryRandomRequest();
                request.setUuid(uuid);
                request.setAttemptCount(attemptCount);
                s = getGroupElement(mTransferVariable.queryRandom(request).getS());
            }
            LOG.info("uuid:{} keyDerivation check s", uuid);
            boolean sLegal = checkSLegal(s);
            if (sLegal) {
                break;
            } else {
                attemptCount += 1;
                QueryRandomLegalRequest request = new QueryRandomLegalRequest();
                request.setUuid(uuid);
                request.setsLegal(false);
                request.setAttemptCount(attemptCount);
                mTransferVariable.queryRandomLegal(request);
            }
        }

        LOG.info("uuid:{} keyDerivation send r and xs", uuid);
        final GroupElement paramS = s;
        CompletableFuture<GroupElement> calcR = CompletableFuture.supplyAsync(() -> calcR(arithmetic, target, x, paramS));
        CompletableFuture<GroupElement> calcXS = CompletableFuture.supplyAsync(() -> arithmetic.mul(x, paramS));
        CompletableFuture.allOf(calcR, calcXS).join();

        GroupElement r = null;
        GroupElement xs = null;
        try {
            r = calcR.get();
            xs = calcXS.get();
        } catch (InterruptedException e) {
            LOG.error("CompletableFuture Interrupted", e);
        } catch (ExecutionException e) {
            LOG.error("CompletableFuture error", e);
        }

        final GroupElement sendRValue = r;
        final int paramAttemptCount = attemptCount;
        CompletableFuture<QueryRandomLegalResponse> cfResults = CompletableFuture.supplyAsync(
                () -> sendR(sendRValue, paramAttemptCount));

        LOG.info("uuid:{} keyDerivation init mac", uuid);
        initMac(s, r);
        byte[] targetKey = macTecElement(xs);
        ObliviousTransferKey key = new ObliviousTransferKey(target, targetKey);

        LOG.info("uuid:{} keyDerivation finish", uuid);
        cfResults.join();
        QueryRandomLegalResponse response = null;
        try {
            response = cfResults.get();
        } catch (ExecutionException e) {
            LOG.error("CompletableFuture Interrupted", e);
        } catch (InterruptedException e) {
            LOG.error("CompletableFuture Interrupted", e);
        }
        if (response.getResults() != null && !response.getResults().isEmpty()) {
            String result = CryptUtil.decrypt(response.getResults().get(target), targetKey);
            key.setResult(result);
        }

        List<ObliviousTransferKey> keys = new ArrayList<>(1);
        keys.add(key);
        return keys;
    }

    private QueryRandomLegalResponse sendR(GroupElement r, int attemptCount) {
        QueryRandomLegalRequest request = new QueryRandomLegalRequest();
        request.setUuid(uuid);
        request.setsLegal(true);
        request.setAttemptCount(attemptCount);
        request.setR(Conversion.groupElementToString(r));
        QueryRandomLegalResponse response = mTransferVariable.queryRandomLegal(request);
        LOG.info("uuid:{} keyDerivation send r", uuid);
        return response;
    }

    private GroupElement calcR(GroupArithmetic arithmetic, int target, BigInteger x, GroupElement s) {
        LOG.info("uuid:{} keyDerivation calc r", uuid);
        GroupElement t = hashTecElement(s);
        BigInteger c = BigInteger.valueOf(target);
        GroupElement ct = arithmetic.mul(c, t);
        GroupElement xg = arithmetic.mul(x, arithmetic.getGenerator());
        GroupElement r = arithmetic.add(ct, xg);
        LOG.info("uuid:{} keyDerivation calc r finish", uuid);
        return r;
    }

    boolean checkSLegal(GroupElement s) {
        return arithmetic.isInGroup(s);
    }

}
