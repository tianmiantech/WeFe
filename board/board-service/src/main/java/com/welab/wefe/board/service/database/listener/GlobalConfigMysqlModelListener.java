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

package com.welab.wefe.board.service.database.listener;

import com.welab.wefe.board.service.database.entity.GlobalConfigMysqlModel;
import com.welab.wefe.board.service.dto.globalconfig.base.ConfigGroupConstant;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.DatabaseEncryptUtil;
import org.apache.commons.collections.CollectionUtils;

import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalConfigMysqlModelListener {
    /**
     * Database protected fields
     */
    public final static Map<String, List<String>> DB_PROTECTED_FIELD_MAP = new HashMap<>(16);

    static {
        DB_PROTECTED_FIELD_MAP.put(ConfigGroupConstant.MEMBER_INFO, Arrays.asList("member_mobile"));
        DB_PROTECTED_FIELD_MAP.put(ConfigGroupConstant.MAIL_SERVER, Arrays.asList("mail_password"));
        DB_PROTECTED_FIELD_MAP.put(ConfigGroupConstant.ALIYUN_FC_CONFIG, Arrays.asList("access_key_id", "access_key_secret"));
        DB_PROTECTED_FIELD_MAP.put(ConfigGroupConstant.ALIYUN_SMS_CHANNEL, Arrays.asList("access_key_id", "access_key_secret"));
        DB_PROTECTED_FIELD_MAP.put(ConfigGroupConstant.CLICKHOUSE_STORAGE, Arrays.asList("password"));
    }


    /**
     * before save
     */
    @PrePersist
    public void prePersist(Object entity) throws StatusCodeWithException {
        if (null != entity) {
            GlobalConfigMysqlModel model = (GlobalConfigMysqlModel) entity;
            if (!check(model)) {
                return;
            }
            model.setValue(DatabaseEncryptUtil.encrypt(model.getValue()));
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
            GlobalConfigMysqlModel model = (GlobalConfigMysqlModel) entity;
            if (!check(model)) {
                return;
            }
            model.setValue(DatabaseEncryptUtil.decrypt(model.getValue()));
        }
    }

    private boolean check(GlobalConfigMysqlModel model) {
        String group = model.getGroup();
        String name = model.getName();
        String value = model.getValue();
        if (StringUtil.isEmpty(group) || StringUtil.isEmpty(name) || StringUtil.isEmpty(value) ||
                !DB_PROTECTED_FIELD_MAP.containsKey(group) || CollectionUtils.isEmpty(DB_PROTECTED_FIELD_MAP.get(group))
                || !DB_PROTECTED_FIELD_MAP.get(group).contains(name)) {
            return false;
        }
        return true;
    }
}
