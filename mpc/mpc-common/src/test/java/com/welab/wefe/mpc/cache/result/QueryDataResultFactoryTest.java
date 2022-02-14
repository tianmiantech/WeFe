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

package com.welab.wefe.mpc.cache.result;

import com.welab.wefe.mpc.cache.result.impl.SecondDataResultCache;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

/**
 * @Author: eval
 * @Date: 2021-12-28
 **/
public class QueryDataResultFactoryTest {

    @Test
    public void getQueryDataResult() {
//        QueryDataResultFactory.init(new SecondDataResultCache());
        QueryDataResult<BigInteger> queryDataResult = QueryDataResultFactory.getQueryDataResult();

        queryDataResult.save("bb", new BigInteger("333"));
        BigInteger value = queryDataResult.query("bb");
        Assert.assertEquals(new BigInteger("333"), value);

        new Thread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            QueryDataResult<String> queryDataResultString = QueryDataResultFactory.getQueryDataResult();
            queryDataResultString.save("aa", "1234567890");
        }).start();

        QueryDataResult<String> queryDataResultString = QueryDataResultFactory.getQueryDataResult();
        String value1 = queryDataResultString.query("aa");
        Assert.assertEquals("1234567890", value1);
    }
}