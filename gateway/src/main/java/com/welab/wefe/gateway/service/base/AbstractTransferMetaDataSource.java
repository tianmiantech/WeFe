/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.gateway.service.base;

import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;

import java.util.ArrayList;
import java.util.List;

/**
 * The message is a data source forwarding service interface of dsourceprocessor type
 *
 * @author aaron.li
 **/
public abstract class AbstractTransferMetaDataSource {

    /**
     * Get data and push to remote
     *
     * @param transferMeta Client request metadata
     * @return Push results
     */
    public abstract BasicMetaProto.ReturnStatus getDataAndPushToRemote(GatewayMetaProto.TransferMeta transferMeta);

    /**
     * Response result collector
     */
    public static class AsyncResponseCollector {

        /**
         * Push successful fragment list
         */
        private List<GatewayMetaProto.TransferMeta> successList = new ArrayList<>();
        /**
         * Push failed fragment list
         */
        private List<GatewayMetaProto.TransferMeta> failedList = new ArrayList<>();


        public List<GatewayMetaProto.TransferMeta> getSuccessList() {
            return successList;
        }

        public void setSuccessList(List<GatewayMetaProto.TransferMeta> successList) {
            this.successList = successList;
        }

        public List<GatewayMetaProto.TransferMeta> getFailedList() {
            return failedList;
        }

        public void setFailedList(List<GatewayMetaProto.TransferMeta> failedList) {
            this.failedList = failedList;
        }
    }

}
