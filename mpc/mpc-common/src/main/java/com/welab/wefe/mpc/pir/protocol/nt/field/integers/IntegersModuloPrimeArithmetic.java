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

package com.welab.wefe.mpc.pir.protocol.nt.field.integers;

import com.welab.wefe.mpc.pir.protocol.nt.field.GaloisFieldArithmetic;
import com.welab.wefe.mpc.pir.protocol.nt.field.GaloisFieldElement;

import java.math.BigInteger;

public class IntegersModuloPrimeArithmetic extends GaloisFieldArithmetic {

    public IntegersModuloPrimeArithmetic(BigInteger mod) {
        super(mod);
        addIdentity = new IntegersModuloPrimeElement(BigInteger.ZERO);
        mulIdentity = new IntegersModuloPrimeElement(BigInteger.ONE);
        check_mod_prime();
    }

    BigInteger rectify(BigInteger a) {
        return a.mod(mod);
    }

    @Override
    public GaloisFieldElement add(GaloisFieldElement a, GaloisFieldElement b) {
        return new IntegersModuloPrimeElement(a.val.add(b.val).mod(mod));
    }

    @Override
    public GaloisFieldElement neg(GaloisFieldElement a) {
        return new IntegersModuloPrimeElement(mod.subtract(a.val));
    }

    @Override
    public GaloisFieldElement sub(GaloisFieldElement a, GaloisFieldElement b) {
        return add(a, neg(b));
    }

    @Override
    public GaloisFieldElement mul(GaloisFieldElement a, GaloisFieldElement b) {
        return new IntegersModuloPrimeElement(a.val.multiply(b.val).mod(mod));
    }

    @Override
    public GaloisFieldElement invert(GaloisFieldElement a) {
        return new IntegersModuloPrimeElement(a.val.modInverse(mod));
    }

    @Override
    public GaloisFieldElement div(GaloisFieldElement a, GaloisFieldElement b) {
        return mul(a, invert(b));
    }

    @Override
    public GaloisFieldElement pow(GaloisFieldElement a, int e) {
        if (e == 0) {
            return mulIdentity;
        }
        return new IntegersModuloPrimeElement(a.val.modPow(BigInteger.valueOf(e), mod));
    }

    @Override
    public boolean isPositive(GaloisFieldElement a) {
        return a.val.compareTo(mod.divide(BigInteger.valueOf(2))) < 0;
    }

    @Override
    public GaloisFieldElement[] sqrt(GaloisFieldElement a) {
        if (is_a_quadratic_residue(a)) {
            BigInteger root_raw = tonelli(a.val, mod);
            BigInteger root_raw_other = mod.subtract(root_raw);
            if (root_raw.compareTo(root_raw_other) > 0) {
                return new GaloisFieldElement[]{
                        new IntegersModuloPrimeElement(root_raw_other),
                        new IntegersModuloPrimeElement(root_raw)
                };
            } else {
                return new GaloisFieldElement[]{
                        new IntegersModuloPrimeElement(root_raw),
                        new IntegersModuloPrimeElement(root_raw_other)
                };
            }
        }
        return new GaloisFieldElement[]{
                new IntegersModuloPrimeElement(BigInteger.valueOf(-1)),
                new IntegersModuloPrimeElement(BigInteger.valueOf(-1))};
    }

    boolean is_positive(GaloisFieldElement a) {
        return a.val.compareTo(mod.divide(BigInteger.valueOf(2))) < 0;
    }

    boolean check_mod_prime() {
        return mod.isProbablePrime(1);
    }

    boolean is_a_quadratic_residue(GaloisFieldElement a) {
        return legendre(a.val, mod).intValue() == 1;
    }

    BigInteger legendre(BigInteger a, BigInteger p) {
        return a.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.valueOf(2)), p);
    }

    BigInteger tonelli(BigInteger n, BigInteger p) {
        BigInteger q = p.subtract(BigInteger.ONE);
        BigInteger s = BigInteger.ZERO;
        BigInteger TWO = BigInteger.valueOf(2);
        while (q.mod(TWO).intValue() == 0) {
            q = q.divide(TWO);
            s = s.add(BigInteger.ONE);
        }
        if (s.intValue() == 1) {
            return n.modPow(p.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), p);
        }
        int z = 2;
        for (; z < p.intValue(); z++) {
            if (p.subtract(BigInteger.ONE).equals(legendre(BigInteger.valueOf(z), p))) {
                break;
            }
        }
        BigInteger c = BigInteger.valueOf(z).modPow(q, p);
        BigInteger r = n.modPow(q.add(BigInteger.ONE).divide(TWO), p);
        BigInteger t = n.modPow(q, p);
        BigInteger m = s;
        while (t.subtract(BigInteger.ONE).mod(p).intValue() != 0) {
            BigInteger t2 = t.multiply(t).mod(p);
            int i = 1;
            for (; i < m.intValue(); i++) {
                if (t2.subtract(BigInteger.ONE).mod(p).intValue() == 0) {
                    break;
                }
                t2 = t2.multiply(t2).mod(p);
            }
            BigInteger b = c.modPow(BigInteger.ONE.shiftLeft(m.intValue() - i - 1), p);
            r = r.multiply(b).mod(p);
            c = b.multiply(b).mod(p);
            t = t.multiply(c).mod(p);
            m = BigInteger.valueOf(i);
        }
        return r;
    }

}
