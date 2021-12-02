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

package com.welab.wefe.board.service.api.data_resource.bloomfilter;


import com.welab.wefe.board.service.service.data_resource.bloomfilter.BloomfilterService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jacky.jiang
 */
@Api(path = "bloomfilter/delete", name = "delete bloomfilter")
public class BloomfilterDeleteApi extends AbstractNoneOutputApi<BloomfilterDeleteApi.Input> {

    @Autowired
    private BloomfilterService bloomfilterService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        bloomfilterService.delete(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "数据集 Id", require = true)
        private String id;

        //region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }


        //endregion
    }
}
