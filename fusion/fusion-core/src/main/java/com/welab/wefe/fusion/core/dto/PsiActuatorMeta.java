package com.welab.wefe.fusion.core.dto;
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


import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

import java.math.BigInteger;

/**
 * @author hunter.zhao
 */
public class PsiActuatorMeta {

    private BigInteger e;
    private BigInteger N;

    private BloomFilters bf;

    protected BloomFilterDto bfDto;

    public BigInteger getE() {
        return e;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }

    public BigInteger getN() {
        return N;
    }

    public void setN(BigInteger n) {
        N = n;
    }

    public BloomFilterDto getBfDto() {
        return bfDto;
    }

    public void setBfDto(BloomFilterDto bfDto) {
        this.bfDto = bfDto;
    }

    public BloomFilters getBf() {
        return bf;
    }

    public void setBf(BloomFilters bf) {
        this.bf = bf;
    }

    public static PsiActuatorMeta of(BigInteger e, BigInteger N, BloomFilters bf) {
        PsiActuatorMeta psiActuatorMeta = new PsiActuatorMeta();
        psiActuatorMeta.bfDto = BloomFilterDto.ofBloomFilters(bf);
        psiActuatorMeta.e = e;
        psiActuatorMeta.N = N;
        return psiActuatorMeta;
    }
}
