/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.util.unique;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

/**
 * Bloom filter
 *
 * @author zane.luo
 */
public class DataSetBloomUniqueFilter extends AbstractDataSetUniqueFilter {

    private final BloomFilter<CharSequence> bloomFilter;

    public DataSetBloomUniqueFilter(long width) {
        width *= 2;
        int minWidth = 100_000_000;
        width = width < minWidth ? minWidth : width;
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), width, 0.01);
    }

    @Override
    public ContainResult contains(String item) {

        boolean mightContain = bloomFilter.mightContain(item);
        bloomFilter.put(item);
        if (mightContain) {
            return ContainResult.MaybeIn;
        } else {
            return ContainResult.NotIn;
        }

    }
}
