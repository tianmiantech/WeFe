package com.welab.wefe.board.service.api.fusion;

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


import com.welab.wefe.board.service.fusion.actuator.psi.ServerActuator;
import com.welab.wefe.board.service.fusion.manager.ActuatorManager;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

import java.io.IOException;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/align", name = "get align", desc = "get align")
public class AlignApi extends AbstractApi<AlignApi.Input, byte[][]> {


    @Override
    protected ApiResult<byte[][]> handle(Input input) throws StatusCodeWithException, IOException {
        ServerActuator actuator = (ServerActuator) ActuatorManager.get(input.getId());

        return success(actuator.compute(input.getBs()));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "id", require = true)
        String id;

        byte[][] bs;

        public Input(String id, byte[][] bs) {
            this.id = id;
            this.bs = bs;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public byte[][] getBs() {
            return bs;
        }

        public void setBs(byte[][] bs) {
            this.bs = bs;
        }
    }
}
