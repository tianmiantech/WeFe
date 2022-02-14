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

package com.welab.wefe.mpc.pir.protocol.ot.hauck;


import com.welab.wefe.mpc.pir.protocol.nt.field.integers.IntegersModuloPrimeElement;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupArithmetic;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupElement;
import com.welab.wefe.mpc.pir.protocol.nt.group.cyclic.twisted.TwistedEdwardsCurveArithmetic;
import com.welab.wefe.mpc.pir.protocol.nt.group.cyclic.twisted.TwistedEdwardsCurveElement;
import com.welab.wefe.mpc.pir.protocol.ro.hf.HashFunction;
import com.welab.wefe.mpc.pir.protocol.ro.hf.Sha256;
import com.welab.wefe.mpc.pir.protocol.ro.mac.HashBasedMessageAuthenticationCode;
import com.welab.wefe.mpc.pir.protocol.ro.mac.Sha256MAC;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author eval
 */
public class HauckObliviousTransfer {

    public String uuid;
    public GroupArithmetic arithmetic;
    public HashFunction hash;
    public HashBasedMessageAuthenticationCode mac;

    public HauckObliviousTransfer(String uuid) {
        this.uuid = uuid;
        this.arithmetic = new TwistedEdwardsCurveArithmetic();
        hash = new Sha256();
    }

    public BigInteger genRandomScalar() {
        BigInteger q = arithmetic.getFieldOrder();
        BigInteger res = new BigInteger(q.bitLength(), new SecureRandom());
        return res.mod(q);
    }

    public GroupElement hashTecElement(GroupElement element) {
        byte[] elementBytes = arithmetic.encode(element);
        byte[] elementDigest = hash.digest(elementBytes);
        return arithmetic.decode(elementDigest);
    }

    public void initMac(GroupElement s, GroupElement r) {
        byte[] sBytes = arithmetic.encode(s);
        byte[] rBytes = arithmetic.encode(r);
        byte[] key = new byte[sBytes.length + rBytes.length];
        System.arraycopy(sBytes, 0, key, 0, sBytes.length);
        System.arraycopy(rBytes, 0, key, sBytes.length, rBytes.length);
        mac = new Sha256MAC(key);
    }

    public byte[] macTecElement(GroupElement element) {
        byte[] elementBytes = arithmetic.encode(element);
        byte[] elementDigest = mac.digest(elementBytes);
        return elementDigest;
    }

    public GroupElement getGroupElement(Object object) {
        String string = "";
        if (object instanceof String) {
            string = (String) object;
        }
        String[] values = string.split(",");
        return new TwistedEdwardsCurveElement(new IntegersModuloPrimeElement(new BigInteger(values[0])),
                new IntegersModuloPrimeElement(new BigInteger(values[1])));
    }

    public HauckTarget generateHauckTarget() {
        BigInteger y;
        GroupElement s;
        GroupElement t;
        for (; ; ) {
            y = genRandomScalar();
            s = arithmetic.mul(y, arithmetic.getGenerator());
            t = hashTecElement(s);
            if (arithmetic.isInGroup(s) && !"-1".equals(t.x.val.toString())) {
                return new HauckTarget(y, s, t);
            }
        }
    }
}
