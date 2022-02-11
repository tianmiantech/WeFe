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

package com.welab.wefe.mpc.pir.protocol.nt.field;

import java.math.BigInteger;

public abstract class GaloisFieldArithmetic {
    public GaloisFieldElement addIdentity;
    public GaloisFieldElement mulIdentity;
    public BigInteger mod;

    public GaloisFieldArithmetic(BigInteger mod) {
        this.mod = mod;
    }

    public abstract GaloisFieldElement add(GaloisFieldElement a, GaloisFieldElement b);

    public abstract GaloisFieldElement neg(GaloisFieldElement a);

    public abstract GaloisFieldElement sub(GaloisFieldElement a, GaloisFieldElement b);

    public abstract GaloisFieldElement mul(GaloisFieldElement a, GaloisFieldElement b);

    public abstract GaloisFieldElement invert(GaloisFieldElement a);

    public abstract GaloisFieldElement div(GaloisFieldElement a, GaloisFieldElement b);

    public abstract GaloisFieldElement pow(GaloisFieldElement a, int e);

    public abstract boolean isPositive(GaloisFieldElement a);

    public abstract GaloisFieldElement[] sqrt(GaloisFieldElement a);

    public GaloisFieldElement getAddIdentity() {
        return addIdentity;
    }

    public GaloisFieldElement getMulIdentity() {
        return mulIdentity;
    }

    public BigInteger getMod() {
        return mod;
    }
}
