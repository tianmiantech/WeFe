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

package com.welab.wefe.gateway.init;

import com.welab.wefe.gateway.cache.MemberCache;

/**
 * Load its own member and all member information into the cache
 *
 * @author aaron.li
 **/
public class LoadMemberToCache {

    public static void load() {
        // Load own member information
        MemberCache.getInstance().refreshSelfMemberCacheUntilComplete();
        // Load all member information
        MemberCache.getInstance().refreshTotalMemberCacheUntilComplete();
    }
}
