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
package com.welab.wefe.board.service.api.project.dataset;

import com.welab.wefe.board.service.dto.vo.FeatureOutput;
import com.welab.wefe.board.service.service.DataSetColumnService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 * @date 2022/11/9
 */
@Api(path = "project/table_data_set/feature/list", name = "获取项目中数据集的特征列表", desc = "这里返回的特征列表会包含特征的数据类型，这个信息在union中不存在，所以需要额外获取。", allowAccessWithSign = true)
public class GetFeatures extends AbstractApi<GetFeatures.Input, GetFeatures.Output> {

    @Autowired
    private DataSetColumnService dataSetColumnService;

    @Override
    protected ApiResult<GetFeatures.Output> handle(GetFeatures.Input input) throws Exception {
        List<FeatureOutput> list = dataSetColumnService.listProjectDataSetFeatures(input.memberId,input.projectId, input.dataSetId);
        return success(new Output(list));
    }

    public static class Output {
        public List<FeatureOutput> list;

        public Output() {
        }

        public Output(List<FeatureOutput> list) {
            this.list = list;
        }
    }

    public class Input extends AbstractApiInput {
        @Check(require = true)
        public String projectId;
        @Check(require = true)
        public String memberId;
        @Check(require = true)
        public String dataSetId;
    }
}
