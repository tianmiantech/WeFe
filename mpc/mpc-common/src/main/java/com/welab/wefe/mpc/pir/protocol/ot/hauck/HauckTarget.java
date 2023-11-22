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

import com.welab.wefe.mpc.commom.Conversion;
import com.welab.wefe.mpc.pir.protocol.nt.group.GroupElement;

import java.math.BigInteger;

public class HauckTarget {
    public BigInteger y;
    public GroupElement s;
    public GroupElement t;

    public HauckTarget(BigInteger y, GroupElement s, GroupElement t) {
        this.y = y;
        this.s = s;
        this.t = t;
    }

    @Override
    public String toString() {
        return "HauckTarget{" +
                "y=" + y.toString(16) +
                ", s=" + Conversion.groupElementToString(s) +
                ", t=" + Conversion.groupElementToString(t) +
                '}';
    }
}
