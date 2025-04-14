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
/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.cert.toolkit.encrypt;

import org.bouncycastle.util.BigIntegers;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * @author aaronchu
 * @Description
 * @data 2021/01/12
 */
public class KeyPresenter {

    private KeyPresenter(){}

    /**
     * Convert key bytes to hex string
     * @param keyBytes can be private key or public key
     * @return String
     */
    public static String asString(byte[] keyBytes){
        String s = Numeric.toHexString(keyBytes);
        if(s.contains("0x") || s.contains("0X")) return s;
        return "0x" + s;
    }

    /**
     * Convert key bytes to big integer
     * @param keyBytes can be private key or public key
     * @return BigInteger
     */
    public static BigInteger asBigInteger(byte[] keyBytes){
        return new BigInteger(1, keyBytes);
    }

    /**
     * Return key bytes
     * @param hexKey Hex format key. Can be private key or public key
     * @return byte[]
     */
    public static byte[] asBytes(String hexKey){
        byte[] bytes = Numeric.hexStringToByteArray(hexKey);
        return bytes;
    }

    /**
     * Return key bytes
     * @param bigIntegerKey BigInteger format key. Can be private key or public key
     * @return byte[]
     */
    public static byte[] asBytes(BigInteger bigIntegerKey, int len){
        return BigIntegers.asUnsignedByteArray(len, bigIntegerKey);
    }


}
