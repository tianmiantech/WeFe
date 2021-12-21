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

package com.welab.wefe.board.service.service.available;

import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.available.checkpoint.GatewayInternetCheckpoint;
import com.welab.wefe.board.service.service.available.checkpoint.GatewayIntranetCheckpoint;
import com.welab.wefe.board.service.service.available.checkpoint.StorageCheckpoint;
import com.welab.wefe.board.service.service.available.checkpoint.UnionConnectionCheckpoint;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.checkpoint.dto.ServerAvailableCheckOutput;
import com.welab.wefe.common.wefe.checkpoint.dto.ServerCheckPointOutput;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zane
 */
@Service
public class ServerAvailableCheckService extends AbstractService {

    private static final List<Class<? extends AbstractCheckpoint>> CHECKPOINT_LIST = Arrays.asList(
            UnionConnectionCheckpoint.class,
            GatewayIntranetCheckpoint.class,
            GatewayInternetCheckpoint.class,
            StorageCheckpoint.class
    );

    public ServerAvailableCheckOutput check() {

        List<ServerCheckPointOutput> list = new ArrayList<>();

        for (Class<? extends AbstractCheckpoint> clazz : CHECKPOINT_LIST) {
            AbstractCheckpoint checkpoint = Launcher.getBean(clazz);
            ServerCheckPointOutput result = checkpoint.check();
            list.add(result);
        }

        return new ServerAvailableCheckOutput(list);
    }
}
