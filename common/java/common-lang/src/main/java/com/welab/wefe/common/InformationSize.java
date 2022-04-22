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
package com.welab.wefe.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 信息计量单位转换工具类
 *
 * @author zane
 * @date 2022/4/21
 */
public class InformationSize {
    private long bitSize;

    private InformationSize(long bitSize) {
        this.bitSize = bitSize;
    }

    public long toBit() {
        return bitSize;
    }

    public double toByte() {
        return BigDecimal.valueOf(bitSize)
                .divide(BigDecimal.valueOf(8), 2, RoundingMode.FLOOR).doubleValue();
    }

    public double toKiB() {
        return BigDecimal.valueOf(bitSize)
                .divide(BigDecimal.valueOf(8 * 1024), 2, RoundingMode.FLOOR).doubleValue();
    }

    public double toMiB() {
        return BigDecimal.valueOf(bitSize)
                .divide(BigDecimal.valueOf(8 * 1024 * 1024), 2, RoundingMode.FLOOR).doubleValue();
    }

    public double toGiB() {
        return BigDecimal.valueOf(bitSize)
                .divide(BigDecimal.valueOf(8L * 1024 * 1024 * 1024), 2, RoundingMode.FLOOR).doubleValue();
    }

    public static InformationSize fromBit(long bitSize) {
        return new InformationSize(bitSize);
    }

    public static InformationSize fromByte(double byteSize) {
        return new InformationSize(Convert.toLong(byteSize * 8));
    }

    public static InformationSize fromKiB(double KbSize) {
        return fromByte(KbSize * 1024);
    }


    @Override
    public String toString() {
        if (bitSize < 1024) {
            return bitSize + "bit";
        }

        double byteSize = toByte();
        if (byteSize < 1024) {
            return byteSize + "bytes";
        } else if (byteSize < 1024 * 1024) {
            return String.format("%.2f", byteSize / 1024.0) + "KB";
        } else if (byteSize < 1024 * 1024 * 1024) {
            return String.format("%.2f", byteSize / 1024.0 / 1024.0) + "MB";
        } else {
            return String.format("%.2f", byteSize / 1024.0 / 1024.0 / 1024.0) + "GB";
        }
    }

}
