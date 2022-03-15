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

package com.welab.wefe.common.web;

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.delegate.api_log.AbstractApiLogger;
import com.welab.wefe.common.web.function.*;
import com.welab.wefe.common.web.service.CaptchaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * @author Zane
 */
public class Launcher {

    private static Logger LOG = LoggerFactory.getLogger(Launcher.class);

    public static ApplicationContext CONTEXT;
    /**
     * API permission check policy
     */
    public static ApiPermissionPolicyFunction API_PERMISSION_POLICY;
    /**
     * Token Check Policy
     */
    public static CheckSessionTokenFunction CHECK_SESSION_TOKEN_FUNCTION;
    /**
     * IP Address Filtering Policy
     */
    public static FlowLimitByIpFunction FLOW_LIMIT_BY_IP_FUNCTION;
    public static FlowLimitByMobileFunction FLOW_LIMIT_BY_MOBILE_FUNCTION;
    public static String API_PACKAGE_PATH;

    /**
     * Events that are emitted before the API executes
     */
    public static BeforeApiExecuteFunction BEFORE_API_EXECUTE_FUNCTION;
    /**
     * Events that are triggered after API execution
     */
    public static AfterApiExecuteFunction AFTER_API_EXECUTE_FUNCTION;
    /**
     * The API performs event delegation after the exception
     */
    public static OnApiExceptionFunction ON_API_EXCEPTION_FUNCTION;
    /**
     * API Logger
     */
    public static AbstractApiLogger API_LOGGER;

    /**
     * Disable external instantiation
     */
    private Launcher() {
    }

    public static Launcher instance() {
        return new Launcher();
    }

    /**
     * Start the site
     */
    public void launch(Class<?> primarySource, String... args) {
        /**
         * Initialize CurrentAccount and preheat static resources
         */
        CurrentAccount.init();

        /**
         * Initialize CaptchaService to preheat static resources
         */
        CaptchaService.init();

        CONTEXT = SpringApplication.run(primarySource, args);
    }

    public static <T> T getBean(Class<T> requiredType) {
        Service service = requiredType.getAnnotation(Service.class);
        String name = null;
        if (service != null) {
            if (StringUtil.isNotEmpty(service.value())) {
                name = service.value();
            }
        } else {
            Repository repository = requiredType.getAnnotation(Repository.class);
            if (repository != null) {
                if (StringUtil.isNotEmpty(repository.value())) {
                    name = repository.value();
                }
            }
        }

        if (StringUtil.isEmpty(name)) {
            return CONTEXT.getBean(requiredType);
        } else {
            return CONTEXT.getBean(name, requiredType);
        }
    }

    /**
     * Sets the event for API execution exceptions
     */
    public Launcher onApiExceptionFunction(OnApiExceptionFunction func) {
        ON_API_EXCEPTION_FUNCTION = func;
        return this;
    }

    /**
     * Sets the events that are triggered before the API executes
     */
    public Launcher beforeApiExecuteFunction(BeforeApiExecuteFunction func) {
        BEFORE_API_EXECUTE_FUNCTION = func;
        return this;
    }

    /**
     * Sets the event that will be triggered after the API executes
     */
    public Launcher afterApiExecuteFunction(AfterApiExecuteFunction func) {
        AFTER_API_EXECUTE_FUNCTION = func;
        return this;
    }

    public Launcher apiLogger(AbstractApiLogger logger) {
        API_LOGGER = logger;
        return this;
    }

    /**
     * Example Set the API permission check policy
     */
    public Launcher apiPermissionPolicy(ApiPermissionPolicyFunction func) {
        API_PERMISSION_POLICY = func;
        return this;
    }

    /**
     * Example Set the session id check method
     */
    public Launcher checkSessionTokenFunction(CheckSessionTokenFunction func) {
        CHECK_SESSION_TOKEN_FUNCTION = func;
        return this;
    }

    /**
     * Set the IP flow control check method
     */
    public Launcher flowLimitByIpFunctionFunction(FlowLimitByIpFunction func) {
        FLOW_LIMIT_BY_IP_FUNCTION = func;
        return this;
    }

    /**
     * Set the method for checking mobile phone number flow control
     */
    public Launcher flowLimitByMobileFunctionFunction(FlowLimitByMobileFunction func) {
        FLOW_LIMIT_BY_MOBILE_FUNCTION = func;
        return this;
    }

    /**
     * Set the API package path
     */
    public Launcher apiPackagePath(String path) {
        API_PACKAGE_PATH = path;
        return this;
    }

    /**
     * Set the flag class object under the API package path to prevent package names from being hardcoded with API_PACKAGE_PATH
     */
    public Launcher apiPackageClass(Class<?> packageClass) {
        API_PACKAGE_PATH = packageClass.getPackage().getName();
        return this;
    }

}
