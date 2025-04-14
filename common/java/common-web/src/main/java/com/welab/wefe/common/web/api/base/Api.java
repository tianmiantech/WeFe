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

package com.welab.wefe.common.web.api.base;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Zane
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface Api {
    /**
     * The interface path
     */
    String path();

    /**
     * The name of the interface
     */
    String name();

    /**
     * Interface specification
     */
    String desc() default "";

    /**
     * 接口是否要求在登录的情况下访问
     */
    boolean login() default true;

    /**
     * 接口是否允许在签名的情况下访问
     */
    boolean allowAccessWithSign() default false;

    /**
     * Whether to perform SM2 verification
     */
    boolean sm2Verify() default false;

    /**
     * The caller
     */
    Caller domain() default Caller.Member;

    /**
     * The level of logging
     */
    String logLevel() default "info";

    /**
     * 日志采样打印周期（ms）
     * 此参数用于减少日志打印，节省磁盘。
     * 采样输出策略仅针对响应 code 为 0 时，code 不为 0 的响应不会被省略日志输出。
     * <p>
     * 默认值：0
     * 不使用采样，将每次 api 响应内容进行打印。
     * <p>
     * 大于0：
     * 在周期内仅输出一次完整的 api 响应结果，其它响应省略输出。
     */
    long logSaplingInterval() default 0L;

    /**
     * forward matching uri
     */
    boolean forward() default false;
}
