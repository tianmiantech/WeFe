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

package com.welab.wefe.serving.service.service.globalconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.serving.service.database.serving.entity.GlobalConfigMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.GlobalConfigRepository;
import com.welab.wefe.serving.service.dto.GlobalConfigInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Objects;

/**
 * @author zane
 */
public class BaseGlobalConfigService{

    public static class Group {
        public static String IDENTITY_INFO = "identity_info";
        public static String WEFE_UNION = "wefe_union";
    }


    @Autowired
    protected GlobalConfigRepository globalConfigRepository;

    /**
     * Add or update multiple records
     */
    protected void put(List<GlobalConfigInput> list) throws StatusCodeWithException {
        for (GlobalConfigInput item : list) {
            put(item.group, item.name, item.value, null);
        }
    }

    /**
     * Add or update an object (multiple records)
     */
    protected void put(String group, Object obj) throws StatusCodeWithException {
        /**
         * 1. The names stored in the database are unified as underscores
         * 2. Since fastjson discards fields with a value of null by default,
         *    it should be set to be preserved during serialization here.
         */
        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        String json_string = JSON.toJSONString(obj, config, SerializerFeature.WriteMapNullValue);

        JSONObject json = JSON.parseObject(json_string);
        for (String name : json.keySet()) {
            put(group, name, json.getString(name), null);
        }
    }

    /**
     * Add or update a record
     */
    protected synchronized void put(String group, String name, String value, String comment) throws StatusCodeWithException {
        GlobalConfigMysqlModel one = findOne(group, name);
        if (one == null) {
            one = new GlobalConfigMysqlModel();
            one.setGroup(group);
            one.setName(name);
            one.setCreatedBy(CurrentAccount.id());
        } else {
            if (one.getValue() != null && value == null) {
                StatusCode.SQL_ERROR.throwException("不能试用 null 覆盖非控值");
            }

            // If there is no need to update, jump out
            if (Objects.equals(one.getValue(), value)) {
                if (comment != null && Objects.equals(one.getComment(), comment)) {
                    return;
                }
            }
        }

        one.setValue(value);
        one.setUpdatedBy(CurrentAccount.id());

        if (comment != null) {
            one.setComment(comment);
        }

        globalConfigRepository.save(one);
    }

    public GlobalConfigMysqlModel findOne(String group, String name) {
        Specification<GlobalConfigMysqlModel> where = Where
                .create()
                .equal("group", group)
                .equal("name", name)
                .build(GlobalConfigMysqlModel.class);

        return globalConfigRepository.findOne(where).orElse(null);
    }

    /**
     * Query list according to group
     */
    public List<GlobalConfigMysqlModel> list(String group) {
        return globalConfigRepository.findByGroup(group);
    }

    /**
     * Get the entity corresponding to the specified group
     */
    protected <T> T getModel(String group, Class<T> clazz) {
        List<GlobalConfigMysqlModel> list = list(group);
        return toModel(list, clazz);
    }

    /**
     * Turn the list of configuration items into entities
     */
    private <T> T toModel(List<GlobalConfigMysqlModel> list, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        JSONObject json = new JSONObject();
        for (GlobalConfigMysqlModel item : list) {
            json.put(item.getName(), item.getValue());
        }
        return json.toJavaObject(clazz);
    }
}
