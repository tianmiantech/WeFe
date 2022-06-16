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
package com.welab.wefe.board.service.api.data_output_info;

import com.alibaba.fastjson.JSON;
import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.ServingService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.AESUtil;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.TreeMap;

/**
 * @author hunter.zhao
 * @date 2022/3/7
 */
@Api(path = "data_output_info/model_export_to_file", name = "导出模型到文件中")
public class ModelExportToFileApi extends AbstractApi<ModelExportToFileApi.Input, ResponseEntity<?>> {


    @Autowired
    ServingService servingService;

    @Value("${wefe.model.export.aes.key}")
    private String AES_KEY;

    @Override
    protected ApiResult<ResponseEntity<?>> handle(Input input) throws Exception {

        TreeMap<String, Object> body = servingService.setBody(input.getTaskId(), input.getRole());

        File file = WeFeFileSystem
                .getBaseDir(WeFeFileSystem.UseType.Temp)
                .resolve(input.getTaskId() + ".txt")
                .toFile();

        FileUtil.writeTextToFile(JSON.toJSONString(body), file.toPath(), false);

        //RSA加密
        String aes_key_str = RSAUtil.encryptByPublicKey(AES_KEY, CacheObjects.getRsaPublicKey());
        FileUtil.writeTextToFile(aes_key_str + System.lineSeparator(), file.toPath(), false);

        //将ASE密钥加密后放入加密文件的第一行
        String data = AESUtil.encrypt(JSON.toJSONString(body), AES_KEY);
        FileUtil.writeTextToFile(data, file.toPath(), true);

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
