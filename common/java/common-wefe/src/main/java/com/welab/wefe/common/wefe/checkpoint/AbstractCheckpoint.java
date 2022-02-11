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

package com.welab.wefe.common.wefe.checkpoint;

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceCheckPointOutput;
import com.welab.wefe.common.wefe.enums.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author zane
 */
public abstract class AbstractCheckpoint {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected abstract ServiceType service();

    protected abstract String desc();

    protected abstract String getConfigValue();

    /**
     * 如果检查点相关的 config value 允许为 null，则此方法返回null。
     * 如果不允许为 null，则返回相应的提示语，前端会展示这些信息，所以提示语应当尽量指导用户的操作。
     */
    protected abstract String messageWhenConfigValueEmpty();

    protected abstract void doCheck(String value) throws Exception;

    protected String configValue;

    public ServiceCheckPointOutput check() {
        long start = System.currentTimeMillis();
        Exception exception = null;

        // 获取配置项
        try {
            configValue = tryGetConfigValue();
        } catch (Exception e) {
            exception = new RuntimeException("获取检查点 config value 失败，" + e.getClass().getSimpleName() + ":" + e.getMessage());
        }

        // 执行 check 动作
        if (exception == null) {
            exception = tryCheck();
        }


        ServiceCheckPointOutput output = new ServiceCheckPointOutput();
        output.setService(service());
        output.setDesc(desc());
        output.setValue(configValue);
        output.setSpend(System.currentTimeMillis() - start);

        if (exception == null) {
            output.setSuccess(true);
            output.setMessage("success");
        } else {
            output.setSuccess(false);
            output.setMessage(exception.getMessage());
        }
        return output;
    }

    private Exception tryCheck() {
        // 异步执行 check 动作
        Future<Exception> future = CommonThreadPool.submit(() -> {
            try {
                doCheck(configValue);
            } catch (Exception e) {
                return e;
            }
            return null;
        });

        // 检查

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            return new Exception("检查点执行检查超时，未得到检查结果。");
        } catch (Exception ex) {
            return ex;
        }
    }

    private String tryGetConfigValue() {
        String configValue;
        String message;
        configValue = getConfigValue();
        message = messageWhenConfigValueEmpty();

        if (StringUtil.isEmpty(configValue) && StringUtil.isNotEmpty(message)) {
            throw new RuntimeException(message);
        }

        return configValue;

    }

    protected void log(Exception e) {
        LOG.error(e.getClass() + " " + e.getMessage(), e);
    }
}
