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

package com.welab.wefe.board.service.onlinedemo;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.dto.AbstractApiInput;

/**
 * Some strategies specific to the online experience environment (demo environment).
 *
 * @author zane.luo
 */
public class OnlineDemoBranchStrategy {

    public static void hackOnDelete(AbstractApiInput input, AbstractBaseMySqlModel model, String message) throws StatusCodeWithException {
        Config config = Launcher.getBean(Config.class);

        if (!config.isOnlineDemo()) {
            return;
        }

        if (StringUtil.isEmpty(model.getCreatedBy())) {
            return;
        }

        if (input.fromGateway()) {
            return;
        }

        // Administrators can delete other people’s data
        if (CurrentAccount.isAdmin()) {
            return;
        }

        // If the current data is not created by the current member, deletion is not allowed.
        if (!model.getCreatedBy().equals(CurrentAccount.id())) {
            throw new StatusCodeWithException(message, StatusCode.UNSUPPORTED_HANDLE);
        }
    }
}
