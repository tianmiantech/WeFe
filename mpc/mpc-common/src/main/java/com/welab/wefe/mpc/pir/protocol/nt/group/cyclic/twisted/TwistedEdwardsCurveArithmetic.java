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

package com.welab.wefe.mpc.pir.protocol.nt.group.cyclic.twisted;

import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.pir.protocol.nt.field.GaloisFieldElement;
import com.welab.wefe.mpc.pir.protocol.nt.field.integers.IntegersModuloPrimeArithmetic;
import com.welab.wefe.mpc.pir.protocol.nt.field.integers.IntegersModuloPrimeElement;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupArithmetic;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupElement;
import com.welab.wefe.mpc.pir.protocol.nt.group.cyclic.CyclicGroupArithmetic;

import java.math.BigInteger;
import java.util.List;

/**
 * @Author eval
 * @Date 2020-11-17
 **/
public class TwistedEdwardsCurveArithmetic extends CyclicGroupArithmetic implements GroupArithmetic {

    public TwistedEdwardsCurveArithmetic() {
        super(null, null, null, null, null);
    }

    /**
     * (x1, y1) + (x2, y2) = ((x1y2 + y1x2) / (1 + dx1x2y1y2), (y1y2 - ax1x2) / (1 - dx1x2y1y2))
     *
     * @param a
     * @param b
     * @return
     */
    @Override
    public GroupElement add(GroupElement a, GroupElement b) {
        GaloisFieldElement x1 = a.x;
        GaloisFieldElement y1 = a.y;
        GaloisFieldElement x2 = b.x;
        GaloisFieldElement y2 = b.y;

        GaloisFieldElement x1y2 = arithmetic.mul(x1, y2);
        GaloisFieldElement x2y1 = arithmetic.mul(x2, y1);
        GaloisFieldElement ax1x2 = arithmetic.mul(this.a, arithmetic.mul(x1, x2));
        GaloisFieldElement y1y2 = arithmetic.mul(y1, y2);
        GaloisFieldElement dx1x2y1y2 = arithmetic.mul(d, arithmetic.mul(x1y2, x2y1));
        GaloisFieldElement numerator_x3 = arithmetic.add(x1y2, x2y1);
        GaloisFieldElement denominator_x3 = arithmetic.add(arithmetic.getMulIdentity(), dx1x2y1y2);
        GaloisFieldElement x3 = arithmetic.div(numerator_x3, denominator_x3);

        GaloisFieldElement numerator_y3 = arithmetic.sub(y1y2, ax1x2);
        GaloisFieldElement denominator_y3 = arithmetic.sub(arithmetic.getMulIdentity(), dx1x2y1y2);
        GaloisFieldElement y3 = arithmetic.div(numerator_y3, denominator_y3);

        return new TwistedEdwardsCurveElement(x3, y3);
    }

    /**
     * -(x, y) = (-x, y)
     *
     * @param a
     * @return
     */
    @Override
    public GroupElement neg(GroupElement a) {
        return new TwistedEdwardsCurveElement(arithmetic.neg(a.x), a.y);
    }

    @Override
    public GroupElement sub(GroupElement a, GroupElement b) {
        return add(a, neg(b));
    }

    @Override
    public GroupElement mul(BigInteger scalar, GroupElement a) {
        if (scalar.equals(BigInteger.ZERO)) {
            return getIdentity();
        }
        List<Integer> binary_representation = Conversion.intToBinaryRepresentation(scalar);
        GroupElement res = getIdentity();
        for (Integer exponent : binary_representation) {
            res = add(res, multipleTwice(exponent, a));
        }
        return res;
    }

    /**
     * 2 * (x, y) = (2xy / (ax^2 + y^2), (y^2 - ax^2) / (2 - ax^2 - y^2))
     *
     * @param a
     * @return
     */
    public GroupElement twice(GroupElement a) {
        GaloisFieldElement x = a.x;
        GaloisFieldElement y = a.y;
        GaloisFieldElement ax_square = arithmetic.mul(this.a, arithmetic.pow(x, 2));
        GaloisFieldElement y_square = arithmetic.pow(y, 2);
        GaloisFieldElement two = new GaloisFieldElement(BigInteger.valueOf(2));

        GaloisFieldElement numerator_x3 = arithmetic.mul(two, arithmetic.mul(x, y));
        GaloisFieldElement denominator_x3 = arithmetic.add(ax_square, y_square);
        GaloisFieldElement x3 = arithmetic.div(numerator_x3, denominator_x3);

        GaloisFieldElement numerator_y3 = arithmetic.sub(y_square, ax_square);
        GaloisFieldElement denominator_y3 = arithmetic.sub(two, denominator_x3);
        GaloisFieldElement y3 = arithmetic.div(numerator_y3, denominator_y3);

        return new GroupElement(x3, y3);
    }

    /**
     * 2^multiple * a
     *
     * @param multiple
     * @param a
     * @return
     */
    public GroupElement multipleTwice(int multiple, GroupElement a) {
        if (multiple == 0) {
            return a;
        }
        GroupElement res = a;
        for (int i = 0; i < multiple; i++) {
            res = twice(res);
        }
        return res;
    }

    @Override
    public boolean isInGroup(GroupElement element) {
        GaloisFieldElement x = element.x;
        GaloisFieldElement y = element.y;
        GaloisFieldElement axSquare = arithmetic.mul(a,
                arithmetic.pow(x, 2));
        GaloisFieldElement ySquare = arithmetic.pow(y, 2);
        GaloisFieldElement left = arithmetic.add(axSquare, ySquare);

        GaloisFieldElement one = arithmetic.getMulIdentity();
        GaloisFieldElement dxSquareYSquare = arithmetic.mul(d,
                arithmetic.mul(arithmetic.pow(x, 2),
                        arithmetic.pow(y, 2)));
        GaloisFieldElement right = arithmetic.add(one, dxSquareYSquare);
        return arithmetic.sub(left, right).val.toString().equals(arithmetic.getAddIdentity().val.toString());
    }

    @Override
    public byte[] encode(GroupElement a) {
        byte posSign = 0x00;
        byte negSign = (byte) 0xFF;
        byte[] yBytes = Conversion.intToBytes(a.y.val);
        byte[] res = new byte[yBytes.length + 1];
        System.arraycopy(yBytes, 0, res, 1, yBytes.length);
        if (arithmetic.isPositive(a.x)) {
            res[0] = posSign;
        } else {
            res[0] = negSign;
        }
        return res;
    }

    @Override
    public GroupElement decode(byte[] bytes) {
        GaloisFieldElement y = new IntegersModuloPrimeElement(Conversion.bytesToInt(bytes),
                (IntegersModuloPrimeArithmetic) arithmetic);
        GaloisFieldElement denominator = arithmetic.sub(a,
                arithmetic.mul(d, arithmetic.pow(y, 2)));
        GaloisFieldElement numerator = arithmetic.sub(arithmetic.getMulIdentity(),
                arithmetic.pow(y, 2));
        GaloisFieldElement[] res = arithmetic.sqrt(arithmetic.div(numerator, denominator));
        GaloisFieldElement x = res[0];
        if (bytes[0] < 128) {
            x = res[0];
        } else {
            x = res[1];
        }
        return new TwistedEdwardsCurveElement(x, y);
    }

    @Override
    public BigInteger getFieldOrder() {
        return arithmetic.mod;
    }

    @Override
    public GroupElement getIdentity() {
        return identity;
    }

    @Override
    public GroupElement getGenerator() {
        return generator;
    }
}
