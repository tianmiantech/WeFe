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

package com.welab.wefe.common.data.mongodb.util;

import org.springframework.data.mongodb.core.query.Update;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yuxin.zhang
 **/
public class UpdateBuilder {

    private Map<String, Object> queryMap = new HashMap<>();

    public UpdateBuilder append(String key, Object value) {
        queryMap.put(key, value);
        return this;
    }

    public Update build() {
        Update update = new Update();

        queryMap.forEach(update::set);

        return update;
    }


}
