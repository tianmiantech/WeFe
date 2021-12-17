
/*
 * *
 *  * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.welab.wefe.mpc.pir.sdk.confuse.impl;

import com.welab.wefe.mpc.commom.AccountEncryptionType;
import com.welab.wefe.mpc.commom.RandomPhoneNum;
import com.welab.wefe.mpc.pir.sdk.confuse.GenerateConfuse;

import java.util.List;

/**
 * @Author eval
 * @Date 2021/12/15
 **/
public class GenerateConfusePhone implements GenerateConfuse {

    @Override
    public List<Object> generate(int count, Object targetObject) {
        return (List)RandomPhoneNum.getKeys(count, (String)targetObject, AccountEncryptionType.md5.toString());
    }
}
