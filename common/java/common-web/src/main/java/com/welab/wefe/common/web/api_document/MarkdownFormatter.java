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

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api_document.model.ApiItem;
import com.welab.wefe.common.web.api_document.model.ApiParam;
import com.welab.wefe.common.web.api_document.model.ApiParamField;

/**
 * @author zane
 * @date 2021/10/27
 */
public class MarkdownFormatter extends AbstractApiDocumentFormatter {

    StringBuilder str = new StringBuilder(1024);

    @Override
    public String contentType() {
        return "text/markdown";
    }

    @Override
    protected void formatApiItem(ApiItem item) {
        getApiInfo(item);
    }

    @Override
    protected void formatGroupItem(String name) {

    }

    @Override
    protected String getOutput() {
        return str.toString();
    }

    private void getApiInfo(ApiItem item) {
        Api api = item.annotation;

        String title = StringUtil.trim(api.path(), '/') + "(" + api.name() + ")";
        str.append("## " + title + System.lineSeparator());

        if (StringUtil.isNotEmpty(api.desc())) {
            str.append("API 简介：" + api.desc() + System.lineSeparator() + "<br>" + System.lineSeparator());
        }

        buildApiParamsJObject("Input", item.input);
        str.append(System.lineSeparator());
        buildApiParamsJObject("Output", item.output);

        str
                .append(System.lineSeparator())
                .append("<br>")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
    }


    /**
     * Generate API parameter documentation
     */
    private void buildApiParamsJObject(String title, ApiParam param) {
        if (param == null) {
            return;
        }

        str.append("**" + title + ":**<br>").append(System.lineSeparator());
        str.append("|name|type|comment|require|" + System.lineSeparator());
        str.append("|---|---|---|---|" + System.lineSeparator());

        for (ApiParamField field : param.fields) {
            str.append("|" + field.name
                    + "|" + field.typeName
                    + "|" + field.comment
                    + "|" + (field.require == null ? "" : String.valueOf(field.require))
                    + "|" + System.lineSeparator()
            );
        }
    }

}
