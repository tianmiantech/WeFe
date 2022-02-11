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
package com.welab.wefe.common.web.api_document;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api_document.model.ApiItem;
import com.welab.wefe.common.web.api_document.model.ApiParam;
import com.welab.wefe.common.web.api_document.model.ApiParamField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zane
 * @date 2021/10/27
 */
public class JsonFormatter extends AbstractApiDocumentFormatter {

    List<JObject> formattedList = new ArrayList<>();

    @Override
    public String contentType() {
        return "application/json";
    }

    @Override
    protected void formatApiItem(ApiItem item) {
        formattedList.add(getApiInfo(item));
    }

    @Override
    protected void formatGroupItem(String name) {

    }

    @Override
    protected Object getOutput() {
        return formattedList;
    }

    private JObject getApiInfo(ApiItem item) {
        Api api = item.annotation;
        JObject json = JObject.create()
                .append("path", item.path);

        if (StringUtil.isNotEmpty(api.name())) {
            json.append("name", api.name());
        }
        if (StringUtil.isNotEmpty(api.desc())) {
            json.append("desc", api.desc());
        }

        json.append("input", buildApiParamsJObject(item.input));
        json.append("output", buildApiParamsJObject(item.output));

        return json;
    }


    /**
     * Generate API parameter documentation
     */
    private List<JObject> buildApiParamsJObject(ApiParam param) {
        if (param == null) {
            return null;
        }


        List<JObject> list = new ArrayList<>();

        for (ApiParamField field : param.fields) {
            JObject output = JObject.create();
            output.put("name", field.name);
            output.put("type", field.typeName);
            output.put("comment", field.comment);
            output.put("require", field.require);
            list.add(output);
        }
        return list;
    }


}
