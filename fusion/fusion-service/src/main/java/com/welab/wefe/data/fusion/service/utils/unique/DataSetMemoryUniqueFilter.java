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

package com.welab.wefe.data.fusion.service.utils.unique;

import java.util.HashSet;
import java.util.Set;

/**
 * Memory implementation filter
 *
 * @author zane.luo
 */
public class DataSetMemoryUniqueFilter extends AbstractDataSetUniqueFilter {
    private Set<CharSequence> keys = new HashSet<>();

    @Override
    public ContainResult contains(String item) {

        boolean contain = keys.contains(item);

        if (contain) {
            return ContainResult.In;
        } else {
            keys.add(item);
            return ContainResult.NotIn;
        }

    }
}
