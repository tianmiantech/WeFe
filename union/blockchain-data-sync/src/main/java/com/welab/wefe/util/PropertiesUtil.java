/**
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

package com.welab.wefe.util;

import cn.hutool.core.collection.CollectionUtil;
import com.welab.wefe.bo.contract.ContractInfo;
import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aaron.li
 **/
public class PropertiesUtil {
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class);

    public static List<ContractInfo> getContractInfos(String solidityPath) {
        List<ContractInfo> contractInfoList = new ArrayList<>();
        if (StringUtil.isEmpty(solidityPath)) {
            return contractInfoList;
        }
        solidityPath = (solidityPath.endsWith("\\") || solidityPath.endsWith("/") ? solidityPath : solidityPath + File.separator);
        Map<String, File> abiMap = getFiles(solidityPath + "abi", ".abi");
        Map<String, File> binMap = getFiles(solidityPath + "bin", ".bin");
        if (CollectionUtil.isEmpty(abiMap) || CollectionUtil.isEmpty(binMap)) {
            return contractInfoList;
        }
        for (Map.Entry<String, File> entry : abiMap.entrySet()) {
            if (!binMap.containsKey(entry.getKey())) {
                continue;
            }
            StringBuilder abi = new StringBuilder();
            try {
                List<String> abis = Files.readAllLines(Paths.get(entry.getValue().toURI()), StandardCharsets.UTF_8);
                for (String str : abis) {
                    abi.append(str);
                }
            } catch (IOException e) {
                LOG.error("abi read failed ", e);
            }
            StringBuilder bin = new StringBuilder();
            try {
                List<String> bins = Files.readAllLines(Paths.get(binMap.get(entry.getKey()).toURI()), StandardCharsets.UTF_8);
                for (String str : bins) {
                    bin.append(str);
                }
            } catch (IOException e) {
                LOG.error("abi read failed ", e);
            }
            ContractInfo contractInfo = new ContractInfo();
            contractInfo.setBinary(bin.toString());
            contractInfo.setAbi(abi.toString());
            contractInfo.setAbiHash(DigestUtils.md5Hex(abi.toString()));
            contractInfo.setContractName(entry.getKey());
            contractInfo.setEventMetaInfoList(BlockUtil.parseToEventMetaInfoList(contractInfo.getAbi(), contractInfo.getContractName()));
            contractInfoList.add(contractInfo);

            LOG.info("Load contract info success from properties, name: {}", contractInfo.getContractName());
        }
        return contractInfoList;
    }


    private static Map<String, File> getFiles(String path, String fileType) {
        Map<String, File> abiMap = new HashMap<>(16);
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return abiMap;
            }
            for (File value : files) {
                if (!value.isDirectory()) {
                    if (value.getName().endsWith(fileType)) {
                        abiMap.put(value.getName().substring(0, value.getName().length() - 4), value);
                    }
                }
            }
        }
        return abiMap;
    }
}
