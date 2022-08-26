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
package com.welab.wefe.serving.service.service_processor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.service.service.DataSourceService;

/**
 * @author hunter.zhao
 */
public abstract class AbstractServiceProcessor<T> {

    protected List<JSONObject> calllogs = new ArrayList<>();
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected final DataSourceService dataSourceService = Launcher.getBean(DataSourceService.class);

    public abstract JObject process(JObject data, T input) throws Exception;

    public List<JSONObject> calllogs() {
        return calllogs;
    }

    public void addCalllog(JSONObject request, JSONObject response) {
        JSONObject calllog = new JSONObject();
        calllog.put("request", request);
        calllog.put("response", response);
        calllogs.add(calllog);
    }
}
