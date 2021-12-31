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
     * Whether to log in
     */
    boolean login() default true;

    /**
     * Whether to perform RSA verification
     */
    boolean rsaVerify() default false;

    /**
     * The caller
     */
    Caller domain() default Caller.Member;

    /**
     * The level of logging
     */
    String logLevel() default "info";
    
    /**
     * forward matching uri
     * */
    boolean forward() default false;
}
