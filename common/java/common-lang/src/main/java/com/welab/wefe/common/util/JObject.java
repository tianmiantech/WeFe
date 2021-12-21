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

package com.welab.wefe.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
public class JObject extends JSONObject implements Serializable {


    public JObject() {
        super(true);
    }

    public JObject(Map<String, Object> map) {
        super(map);
    }


    public String getString(String key, String defaultValue) {
        String ret = super.getString(key);
        return ret == null ? defaultValue : ret;
    }

    /**
     * The value is ignored.
     */
    public String getStringIgnoreCase(String key) {
        Object value = getObjectIgnoreCase(key);
        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }

    /**
     * The value is ignored.
     */
    public Object getObjectIgnoreCase(String key) {
        Object value = super.get(key);
        if (value != null) {
            return value;
        }

        // Underline to hump
        value = super.get(StringUtil.underLineCaseToCamelCase(key));
        if (value != null) {
            return value;
        }

        // The hump is underlined
        value = super.get(StringUtil.camelCaseToUnderLineCase(key));
        return value;
    }

    public JObject append(String key, Object value) {
        return (JObject) this.fluentPut(key, value);
    }

    public JObject getJObject(String key) {
        Object obj = super.get(key);
        if (obj == null) {
            return null;
        }
        return create(obj);
    }

    /**
     * @return Formatted JSON
     */
    public String toPrettyString() {
        return JSON.toJSONString(this, true);
    }

    public static JObject create() {
        return new JObject();
    }

    /**
     * Create a JObject with a pair of initial data
     */
    public static JObject create(String key, Object value) {
        JObject jObject = new JObject();
        jObject.put(key, value);
        return jObject;
    }

    public static JObject create(String jsonText) {
        if (StringUtils.isEmpty(jsonText)) {
            return new JObject();
        }
        return JSON.parseObject(jsonText, JObject.class);
    }

    public static JObject create(Object object) {
        if(null == object) {
            return new JObject();
        }
        return create(JSON.toJSONString(object));
    }

    public String toStringWithNull() {
        SerializerFeature[] serializerFeatureArray = {
                // Output a value whose value is null
                SerializerFeature.WriteMapNullValue
        };
        return JSON.toJSONString(this, serializerFeatureArray);
    }


    @Override
    public JObject put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    //region start: get value by JSONPath

    public String getStringByPath(String jsonPath) {
        return getStringByPath(jsonPath, null);
    }

    public String getStringByPath(String jsonPath, String defaultValue) {
        Object value = getObjectByPath(jsonPath);
        return value == null ? defaultValue : value.toString();
    }

    public Integer getIntegerByPath(String jsonPath) {
        return getIntegerByPath(jsonPath, null);
    }

    public Integer getIntegerByPath(String jsonPath, Integer defaultValue) {
        Object value = getObjectByPath(jsonPath);
        return value == null ? defaultValue : TypeUtils.castToInt(value);
    }

    public Long getLongByPath(String jsonPath) {
        return getLongByPath(jsonPath, null);
    }

    public Long getLongByPath(String jsonPath, Long defaultValue) {
        Object value = getObjectByPath(jsonPath);
        return value == null ? defaultValue : TypeUtils.castToLong(value);
    }

    public Double getDoubleByPath(String jsonPath) {
        return getDoubleByPath(jsonPath, null);
    }

    public Double getDoubleByPath(String jsonPath, Double defaultValue) {
        Object value = getObjectByPath(jsonPath);
        return value == null ? defaultValue : TypeUtils.castToDouble(value);
    }

    public Object getObjectByPath(String jsonPath) {
        if (!JSONPath.contains(this, jsonPath)) {
            return null;
        }
        Object value = JSONPath.eval(this, jsonPath);
        return value;
    }

    public List<JObject> getJSONList(String jsonPath) {
        Object obj = getObjectByPath(jsonPath);

        if (obj != null && obj instanceof JSONArray) {
            return ((JSONArray) obj).stream()
                    .map(item -> create(item.toString()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    //region end: get value by JSONPath

    public void renameKey(String srcKey, String desKey) {

        if (containsKey(desKey)) {
            return;
        }

        Object value = get(srcKey);
        remove(srcKey);
        append(desKey, value);
    }

}

