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

package com.welab.wefe.union.service.service;

import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.union.service.dto.common.ServiceAvailableOutput;
import com.welab.wefe.union.service.dto.common.ServiceStatusOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aaron.li
 **/
@Service
public class CommonService {
    private static final Logger LOG = LoggerFactory.getLogger(CommonService.class);

    @Autowired
    private MemberMongoReop memberMongoReop;
    @Autowired
    private MemberContractService memberContractService;

    /**
     * Check the availability of union
     */
    public ServiceAvailableOutput checkUnionAvailable() {
        List<ServiceStatusOutput> serviceStatusOutputList = new ArrayList<>();
        serviceStatusOutputList.add(checkBlockChain());
        serviceStatusOutputList.add(checkMongo());
        // Whether all are successful
        boolean isTotalSuccess = true;
        for (ServiceStatusOutput output : serviceStatusOutputList) {
            if (!output.isSuccess()) {
                isTotalSuccess = false;
                break;
            }
        }

        ServiceAvailableOutput serviceAvailableOutput = new ServiceAvailableOutput();
        serviceAvailableOutput.setService("UNION");
        serviceAvailableOutput.setSuccess(isTotalSuccess);
        serviceAvailableOutput.setServiceStatusOutputList(serviceStatusOutputList);

        return serviceAvailableOutput;
    }

    /**
     * Check the blockchain
     */
    private ServiceStatusOutput checkBlockChain() {
        ServiceStatusOutput output = new ServiceStatusOutput();
        output.setName("blockChain");
        output.setSuccess(false);

        long startTime = System.currentTimeMillis();
        try {
            memberContractService.isExist("test_member_id");
            output.setSuccess(true);
            output.setMessage("Service is normal");
        } catch (Exception e) {
            String msg = "union availability check, BlockChain error: ";
            LOG.error(msg, e);
            output.setMessage(msg + e.getMessage());
        }
        output.setSpend(System.currentTimeMillis() - startTime);
        return output;
    }


    /**
     * Check mongo
     */
    private ServiceStatusOutput checkMongo() {
        ServiceStatusOutput output = new ServiceStatusOutput();
        output.setName("mongo");
        output.setSuccess(false);
        long startTime = System.currentTimeMillis();
        try {
            memberMongoReop.existsByMemberId("checkMongo");
            output.setSuccess(true);
            output.setMessage("Service is normal");
        } catch (Exception e) {
            String msg = "union availability check, Mongo error: ";
            LOG.error(msg, e);
            output.setMessage(msg + e.getMessage());
        }
        output.setSpend(System.currentTimeMillis() - startTime);
        return output;
    }

}
