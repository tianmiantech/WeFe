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

package com.welab.wefe.common.web.delegate.api_log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.fastjson.LoggerSerializeConfig;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.function.AfterApiExecuteFunction;
import com.welab.wefe.common.web.service.account.AccountInfo;
import com.welab.wefe.common.web.util.HttpServletRequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 **/
public abstract class AbstractApiLogger implements AfterApiExecuteFunction {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
    /**
     * 用户的最后活动时间
     * <p>
     * id : Date
     */
    private static ConcurrentHashMap<String, Date> ACCOUNT_LAST_ACTION_TIME_MAP = new ConcurrentHashMap();
    /**
     * 不需要记录日志的 api 列表
     */
    private static List<String> IGNORE_LOG_APIS = new ArrayList<>();

    /**
     * 获取不需要记录日志的 api 列表
     * <p>
     * 通常需要排除 文件上传、非用户触发的查询类心跳等 api
     */
    protected abstract List<Class<? extends AbstractApi>> getIgnoreLogApiList();

    /**
     * 保存 api 日志
     */
    protected abstract void save(ApiLog apiLog) throws Exception;

    /**
     * 更新用户的最后活动时间
     */
    protected abstract void updateAccountLastActionTime(String userId) throws Exception;

    public AbstractApiLogger() {
        // 初始化列表到静态对象进行缓存，以增强性能。
        List<Class<? extends AbstractApi>> list = getIgnoreLogApiList();
        if (list != null) {
            IGNORE_LOG_APIS = list.stream()
                    .map(x -> x.getAnnotation(Api.class).path())
                    .collect(Collectors.toList());
        }

        new Thread(() -> {
        }).start();
    }

    /**
     * 默认忽略未登录状态的请求日志
     */
    protected boolean ignoreWithoutLogin() {
        return true;
    }

    @Override
    public void action(HttpServletRequest httpServletRequest, long start, AbstractApi<?, ?> api, JSONObject params, ApiResult<?> result) {
        final AccountInfo accountInfo = CurrentAccount.get();

        if (ignoreWithoutLogin() && accountInfo == null) {
            return;
        }

        // 异步保存日志
        CommonThreadPool.run(
                () -> {
                    try {
                        // 存日志
                        JSONObject requestParams = beforeSaveLog(httpServletRequest, start, api, params, result, accountInfo);
                        saveLog(httpServletRequest, start, api, requestParams, result, accountInfo);

                        // 更新用户的最后活动时间
                        logAccountLastActionTime(accountInfo.id);
                    } catch (Exception e) {
                        LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                    }
                }
        );
    }

    private void logAccountLastActionTime(String userId) throws Exception {
        // 如果是第一次访问，立即更新一次。
        if (!ACCOUNT_LAST_ACTION_TIME_MAP.contains(userId)) {
            updateAccountLastActionTime(userId);
        }
        // 每间隔一分钟更新一次
        else if (System.currentTimeMillis() - ACCOUNT_LAST_ACTION_TIME_MAP.get(userId).getTime() > 60_000) {
            updateAccountLastActionTime(userId);
        }

        ACCOUNT_LAST_ACTION_TIME_MAP.put(userId, new Date());
    }


    /**
     * 调用 saveLog 之前的动作，可以在这里对参数进行处理。
     */
    protected JSONObject beforeSaveLog(HttpServletRequest httpServletRequest, long start, AbstractApi<?, ?> api, JSONObject params, ApiResult<?> result, AccountInfo accountInfo) {
        return params;
    }

    private void saveLog(HttpServletRequest httpServletRequest, long start, AbstractApi<?, ?> api, JSONObject params, ApiResult<?> result, AccountInfo accountInfo) {
        Api annotation = api.getClass().getAnnotation(Api.class);

        if (ignore(httpServletRequest, annotation)) {
            return;
        }

        String ip = HttpServletRequestUtil.getClientIp(httpServletRequest);

        ApiLog log = new ApiLog();
        log.setRequestTime(new Date(start));
        log.setSpend(result.spend);
        log.setResponseTime(new Date(start + result.spend));
        log.setCallerIp(ip);
        //union api accountInfo is null
        if(accountInfo != null) {
            log.setCallerId(accountInfo.getId());
            log.setCallerName(accountInfo.getNickname());
        } else {
            log.setCallerId(params.getString("caller_id"));
        }
        log.setApiName(annotation.path());
        log.setRequestData(JSON.toJSONString(params, LoggerSerializeConfig.instance()));
        log.setResponseCode(result.code);
        log.setResponseMessage(result.message);

        try {
            save(log);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
    }

    /**
     * 判断当前 api 请求是否需要记录日志
     * 1. 参数中有 request-from-refresh 字段，且值为 true，则不记录日志。
     * 2. IgnoreLogApiList 中包含当前 api，则不记录日志。
     */
    private boolean ignore(HttpServletRequest httpServletRequest, Api annotation) {
        // Automatically refresh from the front end without writing logs.
        if (httpServletRequest.getQueryString() != null) {
            String value = httpServletRequest.getParameter("request-from-refresh");
            if (StringUtil.isNotEmpty(value) && "true".equals(value)) {
                return true;
            }
        }

        // Blacklist, do not write logs.
        String api = StringUtil.trim(annotation.path().toLowerCase(), '/', ' ');
        return IGNORE_LOG_APIS.contains(api);
    }
}
