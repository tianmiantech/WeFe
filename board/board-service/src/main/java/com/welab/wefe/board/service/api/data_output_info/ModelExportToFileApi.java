/*
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
package com.welab.wefe.board.service.api.data_output_info;

import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.service.TaskResultService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * @author hunter.zhao
 * @date 2022/3/7
 */
@Api(path = "data_output_info/model_export_to_file", name = "导出模型到文件中")
public class ModelExportToFileApi extends AbstractApi<ModelExportToFileApi.Input, ResponseEntity<?>> {


    @Autowired
    TaskResultService taskResultService;


    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws Exception {

        TaskResultMySqlModel taskResult = taskResultService.findByTaskIdAndTypeAndRole(input.taskId, TaskResultType.model_train.name(), input.getRole());

        if (taskResult == null) {
            throw new StatusCodeWithException("task result 不存在！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        File file = WeFeFileSystem
                .getBaseDir(WeFeFileSystem.UseType.Temp)
                .resolve(input.getTaskId() + ".json")
                .toFile();

//        FileUtil.writeTextToFile(taskResult.getResult(), file.toPath(), false);

        FileUtil.writeTextToFile("test", file.toPath(), false);

        return file(file);
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "taskId", require = true)
        private String taskId;

        @Check(name = "模型角色", require = true)
        private JobMemberRole role;

        //region getter/setter

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public JobMemberRole getRole() {
            return role;
        }

        public void setRole(JobMemberRole role) {
            this.role = role;
        }


        //endregion

    }
}
