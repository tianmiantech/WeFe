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

package com.welab.wefe.board.service.service.available.checkpoint;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.dto.vo.ServerCheckPointOutput;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.ServiceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author zane
 */
@Service
public abstract class AbstractCheckpoint {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    protected Config config;
    @Autowired
    protected GlobalConfigService globalConfigService;

    protected abstract ServiceType service();

    protected abstract String desc();

    protected abstract String value();

    protected abstract void doCheck() throws Exception;

    public ServerCheckPointOutput check() {
        long start = System.currentTimeMillis();

        Future<Exception> future = CommonThreadPool.submit(() -> {
            try {
                if (value() == null) {
                    StatusCode
                            .PARAMETER_CAN_NOT_BE_EMPTY
                            .throwException("相关配置为空，请进行设置后再进行检查。");
                }
                doCheck();
            } catch (Exception e) {
                return e;
            }
            return null;
        });

        Exception e;
        try {
            e = future.get(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            e = ex;
        }

        ServerCheckPointOutput output = new ServerCheckPointOutput();
        output.setService(service());
        output.setDesc(desc());
        output.setValue(value());
        output.setSpend(System.currentTimeMillis() - start);

        if (e == null) {
            output.setSuccess(true);
            output.setMessage("success");
        } else {
            output.setSuccess(false);
            output.setMessage(e.getMessage());
        }
        return output;
    }

    protected void log(Exception e) {
        LOG.error(e.getClass() + " " + e.getMessage(), e);
    }
}
