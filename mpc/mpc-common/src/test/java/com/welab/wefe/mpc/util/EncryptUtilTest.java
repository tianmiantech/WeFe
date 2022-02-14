/*
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

package com.welab.wefe.mpc.util;

import com.welab.wefe.mpc.commom.AccountEncryptionType;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * @Author: eval
 * @Date: 2021-12-23
 **/
public class EncryptUtilTest extends TestCase {

    public void testEncrypt() {
        String value = "1234";
        String encryptValue = EncryptUtil.encrypt(value, AccountEncryptionType.md5.name());
        Assert.assertEquals("81dc9bdb52d04dc20036dbd8313ed055", encryptValue);
    }

}