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

/**
 * @author hunter.zhao
 */
public class BloomFilterDto {
    private int count;

    private int size;

    private byte[] bitSet;


    public static BloomFilterDto ofBloomFilters(BloomFilters bf) {
        BloomFilterDto dto = new BloomFilterDto();
        dto.count = bf.count();
        dto.size = bf.size();
        dto.bitSet = bf.getBitSet().toByteArray();
        return dto;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getBitSet() {
        return bitSet;
    }

    public void setBitSet(byte[] bitSet) {
        this.bitSet = bitSet;
    }
}
