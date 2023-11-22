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

package com.welab.wefe.serving.service.api.logger;

import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.DateTypeEnum;

import java.util.EnumSet;

/**
 * @author hunter.zhao
 */
@Api(path = "log/date_type", name = "Date type", login = false)
public class DateTypeEnumApi extends AbstractApi<DateTypeEnumApi.Input, EnumSet<DateTypeEnum>> {
    @Override
    protected ApiResult<EnumSet<DateTypeEnum>> handle(DateTypeEnumApi.Input input) {
        EnumSet<DateTypeEnum> type = EnumSet.allOf(DateTypeEnum.class);
        return success(type);
    }

    public static class Input extends AbstractApiInput {


        //region getter/setter


        //endregion
    }

}
