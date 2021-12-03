/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.web.api_document;

import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.api_document.model.ApiItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author zane
 * @date 2021/12/3
 */
public abstract class AbstractApiDocumentFormatter {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final List<ApiItem> LIST = new ArrayList<>();

    static {
        ReflectionsUtil
                .getClassesWithAnnotation(Launcher.API_PACKAGE_PATH, Api.class)
                .stream()
                .filter(x -> !Modifier.isAbstract(x.getModifiers()))
                .map(ApiItem::new)
                .sorted(Comparator.comparing(x -> x.path))
                .forEach(LIST::add);
    }

    public abstract String contentType();

    protected abstract void formatApiItem(ApiItem item);

    protected abstract void formatGroupItem(String name);

    protected abstract Object getOutput();

    public Object format() {


        String group = null;
        for (ApiItem item : LIST) {
            if (!item.group().equals(group)) {
                group = item.group();
                formatGroupItem(group);
            }

            formatApiItem(item);
        }

        return getOutput();
    }
}
