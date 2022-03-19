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

package com.welab.wefe.serving.service.database.serving.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.welab.wefe.serving.service.database.serving.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;

/**
 * @author hunter.zhao
 */
@Repository
public interface AccountRepository extends BaseRepository<AccountMySqlModel, String> {
    /**
     * Look for the phone number
     *
     * @param phoneNumber
     * @return AccountMySqlModel
     */
    AccountMySqlModel findByPhoneNumber(String phoneNumber);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update account a set a.superAdminRole = false,a.adminRole = false where a.id =?1 ")
    void cancelSuperAdmin(String id);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update account set last_action_time = now() where id =?1 ", nativeQuery = true)
    void updateLastActionTime(String id);
    
    /**
     * 禁用 90 天未活动的账号
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update account set cancelled=true where DATEDIFF(now(),last_action_time)>90", nativeQuery = true)
    int disableAccountWithoutAction90Days();
    
    /**
     * 注销 180 天未活动的账号
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = "update account set cancelled=true where DATEDIFF(now(),last_action_time)>180", nativeQuery = true)
    int cancelAccountWithoutAction180Days();
}
