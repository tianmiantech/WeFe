/**
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

package com.welab.wefe.board.service.api.component;

import com.welab.wefe.board.service.dto.entity.component.ComponentOutputModel;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 **/
@Api(path = "component/list", name = "list components")
public class ListApi extends AbstractApi<ListApi.Input, List<ComponentOutputModel>> {

    @Override
    protected ApiResult<List<ComponentOutputModel>> handle(Input input) throws StatusCodeWithException {

        List<ComponentOutputModel> list = Arrays.stream(ComponentType.values())
                .filter(x -> input.getFederatedLearningType() == null || x.getFederatedLearningTypes() == null
                        || x.getFederatedLearningTypes().contains(input.federatedLearningType))
                // Exclude the relevant components of the validation data set, which have not been developed yet.
                .filter(x -> !x.name().contains("ValidationDataSetLoader")
                        && !"MixStatistic".equalsIgnoreCase(x.name()))
                .map(x -> new ComponentOutputModel(x.name(), x.getLabel(), x.getDesc())).collect(Collectors.toList());

        return success(list);
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "横向/纵向")
        private FederatedLearningType federatedLearningType;

        //region getter/setter

        public FederatedLearningType getFederatedLearningType() {
            return federatedLearningType;
        }

        public void setFederatedLearningType(FederatedLearningType federatedLearningType) {
            this.federatedLearningType = federatedLearningType;
        }


        //endregion
    }
}
