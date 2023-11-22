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

package com.welab.wefe.data.fusion.service.database.listener;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.data.fusion.service.database.entity.AccountMysqlModel;
import com.welab.wefe.data.fusion.service.utils.FusionSM4Util;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class AccountMysqlModelListener {

    /**
     * before save
     */
    @PrePersist
    public void prePersist(Object entity) throws StatusCodeWithException {
        if (null != entity) {
            AccountMysqlModel model = (AccountMysqlModel) entity;
            model.setPhoneNumber(FusionSM4Util.encryptPhoneNumber(model.getPhoneNumber()));
        }
    }

    /**
     * before update
     */
    @PreUpdate
    public void preUpdate(Object entity) throws StatusCodeWithException {
        prePersist(entity);
    }

    /**
     * query
     */
    @PostLoad
    public void postLoad(Object entity) throws StatusCodeWithException {
        if (null != entity) {
            AccountMysqlModel model = (AccountMysqlModel) entity;
            model.setPhoneNumber(FusionSM4Util.decryptPhoneNumber(model.getPhoneNumber()));
        }
    }
}
