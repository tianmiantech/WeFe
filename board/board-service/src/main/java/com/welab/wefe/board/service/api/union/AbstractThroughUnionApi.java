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

package com.welab.wefe.board.service.api.union;


import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.sdk.union.UnionService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiInput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 继承此类的接口不声明具体的入参和响应参数类型，尽数透传到 union，board 中不再做任何逻辑。
 *
 * @author Zane
 */
public abstract class AbstractThroughUnionApi extends AbstractApi<NoneApiInput, Object> {

    @Autowired
    private UnionService unionService;

    protected abstract String api();

    @Override
    protected ApiResult<Object> handle(NoneApiInput input) throws StatusCodeWithException {
        JSONObject response = unionService.request(api(), input.rawRequestParams);
        return super.unionApiResultToBoardApiResult(response);
    }

}
