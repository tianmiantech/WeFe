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

package com.welab.wefe.board.service.service.modelexport;

import com.welab.wefe.board.service.database.entity.job.JobMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.service.JobService;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.ModelExportLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Model export unified entry
 *
 * @author aaron.li
 **/
@Service
public class ModelExportService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JobService jobService;

    @Autowired
    private LogisticRegressionModelExportService logisticRegressionModelExportService;

    @Autowired
    private XgboostModelExportService xgboostModelExportService;

    @Autowired
    private TaskResultService taskResultService;

    /**
     * Execute export
     *
     * @param jobId           job id
     * @param modelFlowNodeId model flow node id
     * @param language        export language
     * @return Export results
     */
    public String handle(String jobId, String modelFlowNodeId, String role, String language) throws StatusCodeWithException {
        try {
            if (!ModelExportLanguage.isExist(language)) {
                throw new StatusCodeWithException("language 参数值非法", StatusCode.PARAMETER_VALUE_INVALID);
            }

            JobMySqlModel jobMySqlModel = jobService.findByJobId(jobId, JobMemberRole.valueOf(role));
            if (null == jobMySqlModel) {
                throw new StatusCodeWithException("任务信息不存在", StatusCode.DATA_NOT_FOUND);
            }

            if (!FederatedLearningType.horizontal.equals(jobMySqlModel.getFederatedLearningType())) {
                throw new StatusCodeWithException("只支持横向导出", StatusCode.DATA_NOT_FOUND);
            }

            TaskResultMySqlModel taskResultMySqlModel = taskResultService.findModelByJobIdAndNodeIdAndRole(jobId, modelFlowNodeId, JobMemberRole.valueOf(role));
            if (null == taskResultMySqlModel) {
                throw new StatusCodeWithException("找不到模型信息", StatusCode.DATA_NOT_FOUND);
            }
            JObject modelParam = JObject.create(taskResultMySqlModel.getResult());

            if (ComponentType.HorzLR.equals(taskResultMySqlModel.getComponentType())) {
                return logisticRegressionModelExportService.export(modelParam.getJObject("model_param"), language);
            }
            return xgboostModelExportService.export(modelParam.getJObject("model_param"), language);

        } catch (StatusCodeWithException e) {
            LOG.error("Export model exception：", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Export model exception：", e);
            throw new StatusCodeWithException("系统异常: " + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

}
