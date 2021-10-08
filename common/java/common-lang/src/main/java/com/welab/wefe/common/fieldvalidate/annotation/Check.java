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

package com.welab.wefe.common.fieldvalidate.annotation;


import com.welab.wefe.common.fieldvalidate.StandardFieldType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Zane
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Check {

    /**
     * Parameter name
     */
    String name() default "";

    /**
     * Parameter description
     */
    String desc() default "";

    /**
     * Whether null or empty is allowed. It is allowed by default.
     */
    boolean require() default false;

    /**
     * Message when non empty field is empty
     */
    String messageOnEmpty() default "";

    /**
     * Regular verification, which will be performed when the data will not be empty.
     */
    String regex() default "";

    /**
     * The declaration field is the standard data type (mailbox, mobile phone number, ID number...).
     * <p>
     * The fields of standard data types will be subject to format check and field standardization. See {@link StandardFieldType#standardize}
     */
    StandardFieldType type() default StandardFieldType.NONE;

    /**
     * Message when the value verification of the field fails
     */
    String messageOnInvalid() default "";

    /**
     * Declare that this parameter is hidden from the front end
     */
    boolean hiddenForFrontEnd() default false;
}
