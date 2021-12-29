package com.welab.wefe.fusion.core.dto;
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


import com.welab.wefe.fusion.core.utils.bf.BloomFilters;

import java.math.BigInteger;
import java.util.BitSet;

/**
 * @author hunter.zhao
 */
public class PsiActuatorMeta {

    private BigInteger e;
    private BigInteger n;

    private BloomFilters bf;

    protected BloomFilterDto bfDto;

    public BigInteger getE() {
        return e;
    }

    public void setE(BigInteger e) {
        this.e = e;
    }

    public BigInteger getN() {
        return n;
    }

    public void setN(BigInteger n) {
        this.n = n;
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

    public static PsiActuatorMeta of(BigInteger e, BigInteger n, BloomFilters bf) {
        PsiActuatorMeta psiActuatorMeta = new PsiActuatorMeta();
        psiActuatorMeta.bfDto = BloomFilterDto.ofBloomFilters(bf);
        psiActuatorMeta.e = e;
        psiActuatorMeta.n = n;
        return psiActuatorMeta;
    }

    public void setBfByDto(BloomFilterDto dto) {
        BitSet bs = BitSet.valueOf(dto.getBitSet());
        this.bf = new BloomFilters(dto.getSize(), dto.getCount(), dto.getCount(), bs);
    }
}
