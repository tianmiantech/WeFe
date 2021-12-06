package com.welab.wefe.board.service.api.fusion.actuator.psi;

/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.fusion.core.dto.PsiActuatorMeta;
import com.welab.wefe.fusion.core.utils.PSIUtils;
import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

import java.io.IOException;
import java.math.BigInteger;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/psi/down_bloom_filter",
        name = "down bloomfilter",
        desc = "down bloomfilter",
        login = false,
        rsaVerify = true)
public class DownBloomFilterApi extends AbstractApi<DownBloomFilterApi.Input, PsiActuatorMeta> {

    @Override
    protected ApiResult<PsiActuatorMeta> handle(Input input) throws StatusCodeWithException, IOException {

        BigInteger N = new BigInteger("146167375152084793681454802679848639178224348966309619052798488909082307110902445595724341286608959925801829756525526243684536115856528805020439965613516355067753856475629524304268915399502745195831856710907661535868988721331189916736238540712398051680091965455756603260140826492895494853907634504720747245633");
        BigInteger e = new BigInteger("65537");
        BigInteger d = new BigInteger("19889843166551599707817170915649025194796904711560632661135799992236385779254894331792265065443622756890012020212927705588884036211735720023380435682764524449631974370220019402021038164175570368177776959055309765000696946731304849785712081220896277458221633983822452333249197209907929579769680795368625751585");

        BloomFilters bf = new BloomFilters(0.001, 1000);

        for (int i = 1; i <= 1000; i++) {
            BigInteger h = PSIUtils.stringToBigInteger(String.valueOf(i));
            BigInteger z = h.modPow(d, N);

            bf.add(z);
        }

        return success(PsiActuatorMeta.of(e, N, bf));
//        ServerActuator actuator = (ServerActuator) ActuatorManager.get(input.getBusinessId());
//
//        return success(actuator.getActuatorParam());
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "businessId", require = true)
        String businessId;

        public Input(String businessId) {
            this.businessId = businessId;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }
    }
}
