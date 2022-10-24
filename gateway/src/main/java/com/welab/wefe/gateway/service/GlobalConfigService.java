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

package com.welab.wefe.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.wefe.dto.global_config.base.AbstractConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.base.ConfigModel;
import com.welab.wefe.gateway.entity.GlobalConfigEntity;
import com.welab.wefe.gateway.repository.GlobalConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlobalConfigService {
    @Autowired
    private GlobalConfigRepository globalConfigRepository;


    /**
     * Get the entity corresponding to the specified group
     */
    public <T extends AbstractConfigModel> T getModel(Class<T> clazz) {
        ConfigModel annotation = clazz.getAnnotation(ConfigModel.class);
        List<GlobalConfigEntity> list = list(annotation.group());
        return toModel(list, clazz);
    }

    /**
     * Query list according to group
     */
    public List<GlobalConfigEntity> list(String group) {
        return globalConfigRepository.findByGroup(group);
    }

    /**
     * Turn the list of configuration items into entities
     */
    private <T> T toModel(List<GlobalConfigEntity> list, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        JSONObject json = new JSONObject();
        for (GlobalConfigEntity item : list) {
            json.put(item.getName(), item.getValue());
        }
        return json.toJavaObject(clazz);
    }
}
