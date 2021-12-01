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

package com.welab.wefe.board.service.api.data_source.table_data_set;

import com.welab.wefe.board.service.dto.vo.data_resource.TableDataSetUpdateInputModel;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(path = "data_set/update", name = "update data set info")
public class UpdateApi extends AbstractNoneOutputApi<TableDataSetUpdateInputModel> {

    @Autowired
    private TableDataSetService tableDataSetService;

    @Override
    protected ApiResult<?> handler(TableDataSetUpdateInputModel input) throws StatusCodeWithException {
        tableDataSetService.update(input);

        return success();
    }

}
