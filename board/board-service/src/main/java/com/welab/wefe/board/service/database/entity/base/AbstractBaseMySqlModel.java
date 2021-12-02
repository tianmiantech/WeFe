/**
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

package com.welab.wefe.board.service.database.entity.base;

import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.dto.AbstractApiInput;

import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * mysql 数据库实体的抽象父类
 *
 * @author Zane
 */
@MappedSuperclass
public abstract class AbstractBaseMySqlModel extends AbstractMySqlModel {

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 更新人
     */
    private String updatedBy;

    public AbstractBaseMySqlModel() {
        setCreatedBy(CurrentAccount.id());
    }

    //region getter/setter

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedBy(AbstractApiInput input) {
        String id = getOperatorId(createdBy, input);
        setCreatedBy(id);
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        super.setUpdatedTime(new Date());
    }

    public void setUpdatedBy(AbstractApiInput input) {
        String id = getOperatorId(updatedBy, input);
        setUpdatedBy(id);
    }

    public String getOperatorId(AbstractApiInput input) {
        return getOperatorId(null, input);
    }

    public String getOperatorId(String operatorId, AbstractApiInput input) {

        String result;
        if (CacheObjects.isMemberId(operatorId)) {
            result = operatorId;
        } else if (input.fromGateway()) {
            result = input.callerMemberInfo.getMemberId();
        } else {
            result = CurrentAccount.id();
        }
        return result;
    }

    //endregion
}
