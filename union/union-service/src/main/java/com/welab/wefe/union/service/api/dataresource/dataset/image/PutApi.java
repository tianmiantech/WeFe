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

package com.welab.wefe.union.service.api.dataresource.dataset.image;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.DeepLearningJobType;
import com.welab.wefe.union.service.dto.dataresource.DataResourcePutInput;
import com.welab.wefe.union.service.service.ImageDataSetService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "image_data_set/put", name = "image_data_set_put", allowAccessWithSign = true)
public class PutApi extends AbstractApi<PutApi.Input, AbstractApiOutput> {
    @Autowired
    private ImageDataSetService imageDataSetService;

    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        imageDataSetService.add(input);
        return success();
    }


    public static class Input extends DataResourcePutInput {
        private DeepLearningJobType forJobType;
        private String labelList;
        private int labeledCount;
        @Check(require = true)
        private boolean labelCompleted;
        private String filesSize;

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public DeepLearningJobType getForJobType() {
            return forJobType;
        }

        public void setForJobType(DeepLearningJobType forJobType) {
            this.forJobType = forJobType;
        }

        public String getLabelList() {
            return labelList;
        }

        public void setLabelList(String labelList) {
            this.labelList = labelList;
        }

        public int getLabeledCount() {
            return labeledCount;
        }

        public void setLabeledCount(int labeledCount) {
            this.labeledCount = labeledCount;
        }

        public boolean isLabelCompleted() {
            return labelCompleted;
        }

        public void setLabelCompleted(boolean labelCompleted) {
            this.labelCompleted = labelCompleted;
        }

        public String getFilesSize() {
            return filesSize;
        }

        public void setFilesSize(String filesSize) {
            this.filesSize = filesSize;
        }

        public String getPublicLevel() {
            return publicLevel;
        }

        public void setPublicLevel(String publicLevel) {
            this.publicLevel = publicLevel;
        }

        public String getPublicMemberList() {
            return publicMemberList;
        }

        public void setPublicMemberList(String publicMemberList) {
            this.publicMemberList = publicMemberList;
        }

        public int getUsageCountInJob() {
            return usageCountInJob;
        }

        public void setUsageCountInJob(int usageCountInJob) {
            this.usageCountInJob = usageCountInJob;
        }

        public int getUsageCountInFlow() {
            return usageCountInFlow;
        }

        public void setUsageCountInFlow(int usageCountInFlow) {
            this.usageCountInFlow = usageCountInFlow;
        }

        public int getUsageCountInProject() {
            return usageCountInProject;
        }

        public void setUsageCountInProject(int usageCountInProject) {
            this.usageCountInProject = usageCountInProject;
        }
    }
}
