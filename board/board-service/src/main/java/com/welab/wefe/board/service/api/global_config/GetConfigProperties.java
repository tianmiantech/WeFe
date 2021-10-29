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
package com.welab.wefe.board.service.api.global_config;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zane
 * @date 2021/10/29
 */
@Api(path = "global_config/config_properties", name = "get config items in config.properties file", login = false)
public class GetConfigProperties extends AbstractNoneInputApi<GetConfigProperties.Output> {

    @Value("${config.path}")
    private String configFilePath;

    private static final List<String> WHITE_LIST = Arrays.asList("wefe.job.backend");

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {

        if (!CurrentAccount.isAdmin()) {
            StatusCode.PERMISSION_DENIED.throwException("仅管理员可查看系统相关配置");
        }

        Map<String, String> configs = new LinkedHashMap<>();

        Path path = Paths.get(configFilePath);
        try {
            Files
                    .lines(path)
                    .filter(x -> {
                        String trimed = x.trim();
                        if (StringUtil.isEmpty(trimed)) {
                            return false;
                        }
                        if (trimed.startsWith("#")) {
                            return false;
                        }
                        return true;
                    })
                    .forEach(x -> {
                        String key = StringUtil.substringBefore(x, "=");
                        String value = StringUtil.substringAfter(x, "=");
                        if (WHITE_LIST.contains(key)) {
                            configs.put(key, value);
                        }
                    });
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return fail(e);
        }

        return success(new Output(configs));
    }

    public class Output {
        public Map<String, String> configs;

        public Output(Map<String, String> configs) {
            this.configs = configs;
        }
    }
}
