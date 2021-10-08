/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.api.dataset;

import com.welab.wefe.board.service.database.repository.DataSetRepository;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.TreeMap;

/**
 * @author Zane
 */
@Api(path = "data_set/tags", name = "all of the data set tags")
public class TagListApi extends AbstractApi<TagListApi.Input, TreeMap<String, Long>> {

    @Autowired
    DataSetRepository repo;

    @Override
    protected ApiResult<TreeMap<String, Long>> handle(Input input) throws StatusCodeWithException {
        TreeMap<String, Long> map = (TreeMap<String, Long>) CacheObjects.getDataSetTags().clone();

        // filter
        if (StringUtil.isNotEmpty(input.tag)) {
            for (Object tag : map.keySet().toArray()) {
                if (!String.valueOf(tag).toLowerCase().contains(input.tag)) {
                    map.remove(tag);
                }
            }
        }

        return success(map);
    }

    public static class Input extends AbstractApiInput {
        private String tag;

        //region getter/setter

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }


        //endregion
    }
}
