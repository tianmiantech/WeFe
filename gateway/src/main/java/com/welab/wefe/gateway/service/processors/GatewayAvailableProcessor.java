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

package com.welab.wefe.gateway.service.processors;

import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.repo.Storage;
import com.welab.wefe.common.data.storage.service.StorageService;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.base.Processor;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.sdk.BoardHelper;
import com.welab.wefe.gateway.sdk.UnionHelper;
import com.welab.wefe.gateway.service.base.AbstractMemberService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gateway availability processor
 * <p>
 * For example, whether the gateway can be connected to MySQL, storage, union, etc
 * </p>
 *
 * @author aaron.li
 **/
@Processor(name = "gatewayAvailableProcessor", desc = "Gateway availability processor")
public class GatewayAvailableProcessor extends AbstractProcessor {
    private final Logger LOG = LoggerFactory.getLogger(GatewayAvailableProcessor.class);

    /**
     * MySQL service name
     */
    private static final String SERVICE_NAME_MYSQL = "mysql";
    /**
     * STORAGE service name
     */
    private static final String SERVICE_NAME_STORAGE = "storage";
    /**
     * Create directory permission service name
     */
    private static final String SERVICE_NAME_AUTO_CREATE_DIR = "autoCreateDirectory";
    /**
     * union service name
     */
    private static final String SERVICE_NAME_UNION = "union";
    /**
     * board service name
     */
    private static final String SERVICE_NAME_BOARD = "board";

    /**
     * status description：Service normal
     */
    private static final String SERVICE_MESSAGE_NORMAL = "服务正常";
    /**
     * tatus description：Service exception
     */
    private static final String SERVICE_MESSAGE_EXCEPTION = "服务异常";


    @Autowired
    private AbstractMemberService memberService;

    @Autowired
    private ConfigProperties mConfigProperties;

    @Autowired
    private StorageService storageService;


    @Override
    public BasicMetaProto.ReturnStatus preToRemoteProcess(GatewayMetaProto.TransferMeta transferMeta) {
        // Check self
        if (isCheckSelf(transferMeta)) {
            List<CheckResult> checkResultList = checkService();
            return ReturnStatusBuilder.ok(transferMeta.getSessionId(), JObject.create().append("checkResultList", checkResultList).toJSONString());
        }

        return super.toRemote(transferMeta);
    }

    @Override
    public BasicMetaProto.ReturnStatus remoteProcess(GatewayMetaProto.TransferMeta transferMeta) {
        List<CheckResult> checkResultList = checkService();
        return ReturnStatusBuilder.ok(transferMeta.getSessionId(), JObject.create().append("checkResultList", checkResultList).toJSONString());
    }

    /**
     * Check whether relevant services are normal
     *
     * @return check result list
     */
    private List<CheckResult> checkService() {
        List<CheckResult> checkResultList = new ArrayList<>();
        // Check whether the MySQL service is normal
        checkResultList.add(checkMysql());
        // Check whether the union service is normal
        checkResultList.add(checkUnion());
        // Check whether the storage service is normal
        checkResultList.add(checkStorage());
        // Check whether the board service is normal
        checkResultList.add(checkBoard());
        // Check whether the program has permission to automatically create directories or files
        checkResultList.add(checkCreateDirPermission());

        return checkResultList;
    }


    /**
     * Check whether the MySQL service is normal
     */
    private CheckResult checkMysql() {
        long startTime = System.currentTimeMillis();
        CheckResult checkResult = new CheckResult(SERVICE_NAME_MYSQL);
        try {
            memberService.findSelf();
        } catch (Exception e) {
            LOG.error("Gateway availability check, MySQL exception：", e);
            checkResult.setSuccess(false);
            checkResult.setMessage(SERVICE_MESSAGE_EXCEPTION + ": " + e.getMessage());
        }
        checkResult.setSpend(System.currentTimeMillis() - startTime);
        return checkResult;
    }


    /**
     * Check whether the union service is normal
     */
    private CheckResult checkUnion() {
        long startTime = System.currentTimeMillis();
        CheckResult checkResult = new CheckResult(SERVICE_NAME_UNION);
        try {
            UnionHelper.checkAlive();
        } catch (Exception e) {
            LOG.error("Gateway availability check, union exception：", e);
            checkResult.setSuccess(false);
            checkResult.setMessage(SERVICE_MESSAGE_EXCEPTION + ": " + e.getMessage());
        }
        checkResult.setSpend(System.currentTimeMillis() - startTime);
        return checkResult;
    }

    /**
     * Check whether the storage service is normal
     */
    private CheckResult checkStorage() {
        long startTime = System.currentTimeMillis();
        CheckResult checkResult = new CheckResult(SERVICE_NAME_STORAGE);
        String name = RandomStringUtils.randomAlphabetic(6);
        Storage storage = storageService.getStorage();
        try {
            storage.put(Constant.DBName.WEFE_DATA, name, new DataItemModel<>(name, "test"));
        } catch (Exception e) {
            LOG.error("Gateway availability check, storage exception：", e);
            checkResult.setSuccess(false);
            checkResult.setMessage(SERVICE_MESSAGE_EXCEPTION + ": " + e.getMessage());
            checkResult.setSpend(System.currentTimeMillis() - startTime);
            return checkResult;
        }

        try {
            storage.dropTB(Constant.DBName.WEFE_DATA, name);
        } catch (Exception e) {
            LOG.error("Gateway availability check, storage exception：", e);
            checkResult.setSuccess(false);
            checkResult.setMessage(SERVICE_MESSAGE_EXCEPTION + ": " + e.getMessage());
        }
        checkResult.setSpend(System.currentTimeMillis() - startTime);
        return checkResult;
    }

    /**
     * Check whether the board service is normal
     */
    private CheckResult checkBoard() {
        long startTime = System.currentTimeMillis();
        CheckResult checkResult = new CheckResult(SERVICE_NAME_BOARD);
        try {
            BoardHelper.testAvailable();
        } catch (Exception e) {
            LOG.error("Gateway availability check, board exception：", e);
            checkResult.setSuccess(false);
            checkResult.setMessage(SERVICE_MESSAGE_EXCEPTION + ": " + e.getMessage());
        }
        checkResult.setSpend(System.currentTimeMillis() - startTime);
        return checkResult;
    }


    /**
     * Check whether the program has permission to automatically create directories or files
     */
    private CheckResult checkCreateDirPermission() {
        long startTime = System.currentTimeMillis();
        CheckResult checkResult = new CheckResult(SERVICE_NAME_AUTO_CREATE_DIR);
        String sendDir = mConfigProperties.getSendTransferMetaPersistentTempDir();
        String recvDir = mConfigProperties.getRecvTransferMetaPersistentTempDir();
        sendDir = ((sendDir.endsWith("\\") || sendDir.endsWith("/")) ? sendDir : sendDir + File.separator);
        recvDir = ((recvDir.endsWith("\\") || recvDir.endsWith("/")) ? recvDir : recvDir + File.separator);

        try {
            FileUtil.createDir(sendDir);
            FileUtil.createDir(recvDir);
            if (!new File(sendDir).exists()) {
                checkResult.setSuccess(false);
                checkResult.setMessage(SERVICE_MESSAGE_EXCEPTION + ": " + "程序无自动创建目录：" + sendDir + " 权限, 请确保网关具有自动创建目录或文件的读写权限.");
                return checkResult;
            }
            if (!new File(recvDir).exists()) {
                checkResult.setSuccess(false);
                checkResult.setMessage(SERVICE_MESSAGE_EXCEPTION + ": " + "程序无自动创建目录：" + recvDir + " 权限, 请确保网关具有自动创建目录或文件的读写权限.");
                return checkResult;
            }

        } catch (Exception e) {
            LOG.error("Gateway availability check, program creation directory exception：", e);
            checkResult.setSuccess(false);
            checkResult.setMessage(SERVICE_MESSAGE_EXCEPTION + ": " + e.getMessage());
        }
        checkResult.setSpend(System.currentTimeMillis() - startTime);
        return checkResult;
    }

    /**
     * Is check self availability
     */
    private boolean isCheckSelf(GatewayMetaProto.TransferMeta transferMeta) {
        GatewayMetaProto.Member srcMember = transferMeta.getSrc();
        GatewayMetaProto.Member dstMember = transferMeta.getDst();
        return StringUtil.isEmpty(dstMember.getMemberId()) || srcMember.getMemberId().equals(dstMember.getMemberId());
    }


    /**
     * Check result
     */
    public static class CheckResult {
        /**
         * service name
         */
        private String service;
        /**
         * Check result: true: success, false: failure
         */
        private boolean success = true;
        /**
         * Result description
         */
        private String message = SERVICE_MESSAGE_NORMAL;
        /**
         * time consuming
         */
        private long spend;

        public CheckResult(String serviceName) {
            this.service = serviceName;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public long getSpend() {
            return spend;
        }

        public void setSpend(long spend) {
            this.spend = spend;
        }
    }

}
