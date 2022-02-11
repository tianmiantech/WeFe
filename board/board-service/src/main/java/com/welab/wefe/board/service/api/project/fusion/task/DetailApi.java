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

package com.welab.wefe.board.service.api.project.fusion.task;

import com.welab.wefe.board.service.dto.fusion.FusionTaskOutput;
import com.welab.wefe.board.service.service.fusion.FusionTaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.fusion.core.utils.CryptoUtils;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/task/detail", name = "任务列表", desc = "任务列表", login = false)
public class DetailApi extends AbstractApi<DetailApi.Input, FusionTaskOutput> {
    @Autowired
    FusionTaskService fusionTaskService;

    @Override
    protected ApiResult<FusionTaskOutput> handle(Input input) throws StatusCodeWithException {
        return success(fusionTaskService.detail(input.id));
    }

    public static void main(String[] args) {
        AsymmetricCipherKeyPair keyPair = CryptoUtils.generateKeys(1024);

        RSAKeyParameters pk = (RSAKeyParameters) keyPair.getPublic();
        RSAKeyParameters sk = (RSAPrivateCrtKeyParameters) keyPair.getPrivate();
        BigInteger e = pk.getExponent();
        BigInteger N = pk.getModulus();
        BigInteger d = sk.getExponent();
        BigInteger p = ((RSAPrivateCrtKeyParameters) sk).getP();
        BigInteger q = ((RSAPrivateCrtKeyParameters) sk).getQ();


        long s1 = System.currentTimeMillis();
        BigInteger tq = p.modInverse(q);
        BigInteger tp = q.modInverse(p);
        BigInteger cp = tp.multiply(q);
        BigInteger cq = tq.multiply(p);



      for(int i=1; i <= 200000;i++) {
          BigInteger x = BigInteger.valueOf(4328423048302L * i);

          BigInteger rp = x.modPow(d.remainder(p.subtract(BigInteger.valueOf(1))), p);
          BigInteger rq = x.modPow(d.remainder(q.subtract(BigInteger.valueOf(1))), q);
          BigInteger r = (rp.multiply(cp).add(rq.multiply(cq))).remainder(N);
      }
        long s2 = System.currentTimeMillis();
        System.out.println(s2-s1 + "ms");
        long s3 = System.currentTimeMillis();
        for(int i=1; i <= 200000;i++) {
            BigInteger x = BigInteger.valueOf(4328423048302L * i);
            BigInteger r1 = x.modPow(d, N);
        }
        long s4 = System.currentTimeMillis();
        System.out.println(s4-s3 + "ms");
//        System.out.println(r1.equals(r));

    }

    public static class Input extends AbstractApiInput {
        @Check(name = "指定操作的taskId", require = true)
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
