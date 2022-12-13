package com.welab.wefe.data.fusion.service.utils.bf;

import java.math.RoundingMode;
import java.util.Arrays;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;

/**
 * @Author tracy.zhang
 * @create 2022/11/24 17:52
 */
public class BitArray implements Cloneable, java.io.Serializable{
    
    private static final long serialVersionUID = -138867652760714173L;
    private final long[] data;
    long bitCount;

    BitArray(long bits) {
        this(new long[Ints.checkedCast(LongMath.divide(bits, 64L, RoundingMode.CEILING))]);
    }

    BitArray(long[] data) {
        Preconditions.checkArgument(data.length > 0, "data length is zero!");
        this.data = data;
        long bitCount = 0L;
        long[] arr$ = data;
        int len$ = data.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            long value = arr$[i$];
            bitCount += (long)Long.bitCount(value);
        }

        this.bitCount = bitCount;
    }

    boolean set(long index) {
        if (!this.get(index)) {
            long[] var10000 = this.getData();
            var10000[(int)(index >>> 6)] |= 1L << (int)index;
            ++this.bitCount;
            return true;
        } else {
            return false;
        }
    }

    boolean get(long index) {
        return (this.getData()[(int)(index >>> 6)] & 1L << (int)index) != 0L;
    }

    long bitSize() {
        return (long)this.getData().length * 64L;
    }
    
    int size() {
        return this.getData().length;
    }

    long bitCount() {
        return this.bitCount;
    }

    BitArray copy() {
        return new BitArray((long[])this.getData().clone());
    }

    void putAll(BitArray array) {
        Preconditions.checkArgument(this.getData().length == array.getData().length, "BitArrays must be of equal length (%s != %s)", this.getData().length, array.getData().length);
        this.bitCount = 0L;

        for(int i = 0; i < this.getData().length; ++i) {
            long[] var10000 = this.getData();
            var10000[i] |= array.getData()[i];
            this.bitCount += (long)Long.bitCount(this.getData()[i]);
        }

    }

    public boolean equals(@Nullable Object o) {
        if (o instanceof BitArray) {
            BitArray bitArray = (BitArray)o;
            return Arrays.equals(this.getData(), bitArray.getData());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.getData());
    }

//    public byte[] toByteArray() throws IOException {
//        ByteArrayOutputStream bao = new ByteArrayOutputStream();
//        DataOutputStream dos =new DataOutputStream(bao);
//        for(long l : data) {
//            dos.writeLong(l);    
//        }
//        byte [] buf =bao.toByteArray();
//        return buf;
//    }

    public static BitArray valueOf(long[] data) {
        int n;
        for (n = data.length; n > 0 && data[n - 1] == 0; n--)
            ;
        return new BitArray(Arrays.copyOf(data, n));
    }

    public long[] getData() {
        return data;
    }
}
