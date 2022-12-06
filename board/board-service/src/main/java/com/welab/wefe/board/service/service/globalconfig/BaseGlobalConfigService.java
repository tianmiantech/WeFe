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

package com.welab.wefe.board.service.service.globalconfig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.welab.wefe.board.service.database.entity.GlobalConfigMysqlModel;
import com.welab.wefe.board.service.database.repository.GlobalConfigRepository;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.secret.Secret;
import com.welab.wefe.common.fieldvalidate.secret.SecretUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.TempRsaCache;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.wefe.dto.global_config.base.AbstractConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.base.ConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.base.GlobalConfigInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zane
 */
public class BaseGlobalConfigService extends AbstractService {


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
    public void put(AbstractConfigModel model) throws StatusCodeWithException {
        /**
         * 1. The names stored in the database are unified as underscores
         * 2. Since fastjson discards fields with a value of null by default,
         *    it should be set to be preserved during serialization here.
         */
        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        String json_string = JSON.toJSONString(model, config, SerializerFeature.WriteMapNullValue);

        ConfigModel annotation = model.getClass().getAnnotation(ConfigModel.class);

        JSONObject json = JSON.parseObject(json_string);
        for (String name : json.keySet()) {
            String value = json.getString(name);
            // value 为 null 时说明前端未指定，需要跳过。
            if (value == null) {
                continue;
            }
            put(annotation.group(), name, value, null);
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
            one.setCreatedBy(CurrentAccountUtil.get().getId());
        } else {
            if (one.getValue() != null && value == null) {
                StatusCode.SQL_ERROR.throwException("不能使用 null 覆盖非空值");
            }

            // If there is no need to update, jump out
            if (Objects.equals(one.getValue(), value)) {
                if (comment != null && Objects.equals(one.getComment(), comment)) {
                    return;
                }
            }
        }

        one.setValue(value);
        one.setUpdatedBy(CurrentAccountUtil.get().getId());

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

    public <T extends AbstractConfigModel> T getModel(String group) {
        Class<T> clazz = (Class<T>) AbstractConfigModel.getModelClass(group);
        if (clazz == null) {
            throw new RuntimeException("未找到对应的 ConfigModel：" + group);
        }
        return getModel(clazz);
    }

    /**
     * Get the entity corresponding to the specified group
     */
    public <T extends AbstractConfigModel> T getModel(Class<T> clazz) {
        ConfigModel annotation = clazz.getAnnotation(ConfigModel.class);
        List<GlobalConfigMysqlModel> list = list(annotation.group());
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

    /**
     * 将 map 还原为 AbstractConfigModel
     * <p>
     * 这一步会对 @Secret 字段进行解密
     */
    public AbstractConfigModel toModel(String group, Map<String, String> map) throws Exception {
        Class<? extends AbstractConfigModel> clazz = AbstractConfigModel.getModelClass(group);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            Secret secret = SecretUtil.getAnnotation(clazz, entry.getKey());
            if (secret != null && StringUtil.isNotEmpty(entry.getValue())) {
                String decrypt = TempRsaCache.decrypt(entry.getValue());
                entry.setValue(decrypt);
            }
        }

        return JObject.create(map).toJavaObject(clazz);
    }
}
