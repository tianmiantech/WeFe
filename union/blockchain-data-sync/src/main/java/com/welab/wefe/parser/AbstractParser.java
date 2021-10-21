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

package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.bo.data.EventBO;
import com.welab.wefe.common.data.mongodb.repo.AbstractMongoRepo;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * event parser
 *
 * @author yuxin.zhang
 */
public abstract class AbstractParser {
    public static final  String EXT_JSON = "ext_json";
    public static final  String PARAMS = "params";
    public static final  String RET_CODE = "ret_code";

    public static final Logger log = LoggerFactory.getLogger(AbstractParser.class);
    protected EventBO eventBO;
    protected String extJsonStr;
    protected JSONArray params;

    public void process(EventBO eventBO) throws BusinessException {
        long startParser = System.currentTimeMillis();
        this.eventBO = eventBO;
        extJsonStr = null == eventBO.getEntity().get(EXT_JSON) ? null : eventBO.getEntity().get(EXT_JSON).toString().trim();
        if (eventBO.getEntity().get(PARAMS) != null) {
            params = JSONObject.parseArray(eventBO.getEntity().get(PARAMS).toString());
        }

        String retCode = eventBO.getEntity().get(RET_CODE).toString();
        if (EventConstant.RUN_SUCCESS_CODE.equals(retCode)) {
            parseContractEvent();
        }
        long endParser = System.currentTimeMillis();
        log.info("parser:{} blockNum:{},event:{},parserData success spend:{} ms",
                this.getClass().getSimpleName(),
                eventBO.getBlockNumber(),
                eventBO.getEventName(),
                (endParser - startParser)
        );

    }

    /**
     * parse contract event
     * @throws BusinessException
     */
    protected abstract void parseContractEvent() throws BusinessException;
}
