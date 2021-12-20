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
package com.welab.wefe.gateway.service.processors.available;

import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.ServiceType;
import com.welab.wefe.gateway.service.base.AbstractMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author zane
 * @date 2021/12/20
 */
@Service
public class MysqlCheckpoint extends AbstractCheckpoint {
    @Autowired
    private Environment env;
    @Autowired
    private AbstractMemberService memberService;


    @Override
    protected ServiceType service() {
        return ServiceType.MysqlService;
    }

    @Override
    protected String desc() {
        return "检查 mysql 服务的可用性";
    }

    @Override
    protected String value() {
        return env.getProperty("");
    }

    @Override
    protected void doCheck() throws Exception {
        memberService.findSelf();
    }
}
