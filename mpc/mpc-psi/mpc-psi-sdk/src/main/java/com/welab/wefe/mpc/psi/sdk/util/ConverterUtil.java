/*
 * Copyright 2022 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.mpc.psi.sdk.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

public class ConverterUtil {

    public static BigInteger convertString2BigInteger(String s) {
        return new BigInteger(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static String convertBigInteger2String(BigInteger bigInteger) {
        return new String(bigInteger.toByteArray(), StandardCharsets.ISO_8859_1);
    }

    public static String convertECPoint2String(ECPoint point) {
        return Base64.getEncoder().encodeToString(point.getEncoded(true));
//        return new String(point.getEncoded(true), StandardCharsets.ISO_8859_1);
    }

    public static ECPoint convertString2ECPoint(ECCurve ecCurve, String value) {
//        return ecCurve.decodePoint(value.getBytes(StandardCharsets.ISO_8859_1));
        return ecCurve.decodePoint(Base64.getDecoder().decode(value));
    }
}
