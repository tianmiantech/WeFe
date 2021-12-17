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

package com.welab.wefe.mpc.pir.protocol.nt.group.cyclic;

import com.welab.wefe.mpc.pir.protocol.nt.field.GaloisFieldArithmetic;
import com.welab.wefe.mpc.pir.protocol.nt.field.GaloisFieldElement;
import com.welab.wefe.mpc.pir.protocol.nt.field.integers.IntegersModuloPrimeArithmetic;
import com.welab.wefe.mpc.pir.protocol.nt.field.integers.IntegersModuloPrimeElement;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupElement;
import com.welab.wefe.mpc.pir.protocol.nt.group.cyclic.twisted.TwistedEdwardsCurveElement;

import java.math.BigInteger;

/**
 * @Author eval
 * @Date 2020-11-17
 **/
public abstract class CyclicGroupArithmetic {
    public GaloisFieldArithmetic arithmetic;
    public GaloisFieldElement a;
    public GaloisFieldElement d;
    public GroupElement identity;
    public GroupElement generator;

    public CyclicGroupArithmetic(GaloisFieldArithmetic arithmetic, GaloisFieldElement a, GaloisFieldElement d, GroupElement identity, GroupElement generator) {
        this.arithmetic = arithmetic != null ? arithmetic : new IntegersModuloPrimeArithmetic(
                BigInteger.valueOf(2).pow(255).subtract(BigInteger.valueOf(19)));
        this.a = a != null ? a : new GaloisFieldElement(
                BigInteger.valueOf(2).pow(255).subtract(BigInteger.valueOf(20)));
        this.d = d != null ? d : new GaloisFieldElement(
                new BigInteger("37095705934669439343138083508754565189542113879843219016388785533085940283555"));
        this.identity = identity != null ? identity : new GroupElement(
                new IntegersModuloPrimeElement(BigInteger.ZERO),
                new IntegersModuloPrimeElement(BigInteger.ONE));
        this.generator = generator != null ? generator : defaultGenerator();
    }

    public static TwistedEdwardsCurveElement defaultGenerator() {
        IntegersModuloPrimeElement x = new IntegersModuloPrimeElement(
                new BigInteger("15112221349535400772501151409588531511454012693041857206046113283949847762202"));
        IntegersModuloPrimeElement y = new IntegersModuloPrimeElement(
                new BigInteger("46316835694926478169428394003475163141307993866256225615783033603165251855960"));
        return new TwistedEdwardsCurveElement(x, y);
    }
}
