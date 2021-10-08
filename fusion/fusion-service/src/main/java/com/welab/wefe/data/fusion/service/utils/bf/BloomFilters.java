/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.data.fusion.service.utils.bf;

import com.google.common.base.Preconditions;

import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.Collection;

/**
 * Bloom filter implementation class
 * define：http://en.wikipedia.org/wiki/Bloom_filter
 *
 * @param <E> E specifies the type of element to be inserted into the filter, eg, String Integer
 * @author hunter.zhao
 */
public class BloomFilters<E> implements Serializable {

    private static final long serialVersionUID = -2326638072608273135L;
    private BitSet bitset;
    private int bitSetSize;
    private double bitsPerElement;
    //Maximum number of elements that can be added
    private int expectedNumberOfFilterElements;
    //The actual number of elements in the filter container
    private int numberOfAddedElements;
    // The number of hash functions
    private int k;

    //The encoding of a string that stores hash values
    static final Charset CHARSET = Charset.forName("UTF-8");

    //In most cases, MD5 provides better hashing accuracy. Change to SHA1 if necessary
    static final String HASHNAME = "MD5";
    //The MessageDigest class is used to provide an application with the capabilities of information summarization algorithms, such as MD5 or SHA algorithms
    static final MessageDigest DIGESTFUNCTION;

    // Initialize the digest algorithm object for MessageDigest
    static {
        MessageDigest tmp;
        try {
            tmp = MessageDigest.getInstance(HASHNAME);
        } catch (NoSuchAlgorithmException e) {
            tmp = null;
        }
        DIGESTFUNCTION = tmp;
    }

    /**
     * Construct an empty Bloom filter. The length of the filter is C x N
     *
     * @param c Represents how many bits each element occupies
     * @param n Represents the maximum number of elements that a filter can add
     * @param k Represents the number of hash functions to be used
     */
    public BloomFilters(double c, int n, int k) {
        this.expectedNumberOfFilterElements = n;
        this.k = k;
        this.bitsPerElement = c;
        this.bitSetSize = (int) Math.ceil(c * n);
        numberOfAddedElements = 0;
        this.bitset = new BitSet(bitSetSize);
    }

    /**
     * Construct an empty Bloom filter. The number of optimal hash functions will be determined by the total size of the filter and the expected number of elements.
     *
     * @param bitSetSize              Specifies the total size of the filter
     * @param expectedNumberOElements Specifies the maximum number of elements that a filter can add
     */
    public BloomFilters(int bitSetSize, int expectedNumberOElements) {
        this(bitSetSize / (double) expectedNumberOElements, expectedNumberOElements, (int) Math.round((bitSetSize / (double) expectedNumberOElements) * Math.log(2.0)));
    }

    /**
     * Construct a filter by specifying a false positive rate.
     * The number of bits per element and the number of hash functions are calculated based on the false positive rate.
     *
     * @param falsePositiveProbability Expected false positives rate.
     * @param expectedNumberOfElements The number of elements to add
     */
    public BloomFilters(double falsePositiveProbability, int expectedNumberOfElements) {
        // c = k/ln(2)
        this(Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2))) / Math.log(2),
                expectedNumberOfElements,
                // k = ln(2)m/n
                (int) Math.ceil(-(Math.log(falsePositiveProbability) / Math.log(2))));
    }

    /**
     * Rebuild a new filter based on the data of the old filter
     *
     * @param bitSetSize                     Specifies the size of the bits required for the filter
     * @param expectedNumberOfFilterElements Specifies the maximum number of elements that a filter can add
     *                                       to contain.
     * @param actualNumberOfFilterElements   Specifies the amount of data for the original filter
     *                                       <code>filterData</code> BitSet.
     * @param filterData                     The BitSet object in the original filter
     */
    public BloomFilters(int bitSetSize, int expectedNumberOfFilterElements,
                        int actualNumberOfFilterElements, BitSet filterData) {
        this(bitSetSize, expectedNumberOfFilterElements);
        this.bitset = filterData;
        this.numberOfAddedElements = actualNumberOfFilterElements;
    }

    /**
     * Generates a summary from the contents of the string
     *
     * @param val     The contents of a string
     * @param charset How input data is encoded
     * @return The output is of type long
     */
    public static long createHash(String val, Charset charset) {
        return createHash(val.getBytes(charset));
    }

    /**
     * Generate a summary from the string contents
     *
     * @param val Specifies the input string. The default encoding is UTF-8
     * @return The output is of type long
     */
    public static long createHash(String val) {
        return createHash(val, CHARSET);
    }

    /**
     * Generate a digest from a byte array
     *
     * @param data The input data
     * @return The output is a summary of type long
     */
    public static long createHash(byte[] data) {
        long h = 0;
        byte[] res;

        synchronized (DIGESTFUNCTION) {
            res = DIGESTFUNCTION.digest(data);
        }

        for (int i = 0; i < 4; i++) {
            h <<= 8;
            h |= ((int) res[i]) & 0xFF;
        }
        return h;
    }

    /**
     * Override equals
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BloomFilters<E> other = (BloomFilters<E>) obj;
        if (this.expectedNumberOfFilterElements != other.expectedNumberOfFilterElements) {
            return false;
        }
        if (this.k != other.k) {
            return false;
        }
        if (this.bitSetSize != other.bitSetSize) {
            return false;
        }
        if (this.bitset != other.bitset
                && (this.bitset == null || !this.bitset.equals(other.bitset))) {
            return false;
        }
        return true;
    }

    /**
     * Overrides the hashCode method
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + (this.bitset != null ? this.bitset.hashCode() : 0);
        hash = 61 * hash + this.expectedNumberOfFilterElements;
        hash = 61 * hash + this.bitSetSize;
        hash = 61 * hash + this.k;
        return hash;
    }

    /**
     * The false positive rate is calculated based on the maximum number of elements and the size of the filter.
     * Method returns the false positive rate. If the number of inserted elements is less than the maximum, the false positive rate is less than the return value.
     *
     * @return Expected false positives rate.
     */
    public double expectedFalsePositiveProbability() {
        return getFalsePositiveProbability(expectedNumberOfFilterElements);
    }

    /**
     * The actual false positive rate is calculated by the number of inserted elements and the filter container size。
     *
     * @param numberOfElements The number of inserted elements.
     * @return The rate of false positives.
     */
    public double getFalsePositiveProbability(double numberOfElements) {
        // (1 - e^(-k * n / m)) ^ k
        return Math.pow((1 - Math.exp(-k * (double) numberOfElements
                / (double) bitSetSize)), k);

    }

    /**
     * The actual false positive rate is calculated by the number of elements actually inserted and the filter container size.
     *
     * @return The rate of false positives.
     */
    public double getFalsePositiveProbability() {
        return getFalsePositiveProbability(numberOfAddedElements);
    }

    /**
     * Returns the number of hash functions k
     *
     * @return k.
     */
    public int getK() {
        return k;
    }

    /**
     * Emptying filter elements
     */
    public void clear() {
        bitset.clear();
        numberOfAddedElements = 0;
    }

    /**
     * Add elements to the filter.
     * The toString() method of the added element will be called, returning the string as the output of the hash function.
     *
     * @param element The element to add
     */
    public void add(E element) {
        long hash;
        String valString = element.toString();
        for (int x = 0; x < k; x++) {
            hash = createHash(valString + Integer.toString(x));
            hash = hash % (long) bitSetSize;
            bitset.set(Math.abs((int) hash), true);
        }
        numberOfAddedElements++;
    }

    /**
     * Add a collection of elements to the filter
     *
     * @param c Elements in the collection.
     */
    public void addAll(Collection<? extends E> c) {
        for (E element : c) {
            add(element);
        }
    }

    /**
     * Used to determine if the element is in the filter. Return true if it already exists.
     *
     * @param element The element to check.
     * @return Returns true if the element is estimated to already exist
     */
    public boolean contains(E element) {
        long hash;
        String valString = element.toString();
        for (int x = 0; x < k; x++) {
            hash = createHash(valString + Integer.toString(x));
            hash = hash % (long) bitSetSize;
            if (!bitset.get(Math.abs((int) hash))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines whether all elements in a collection are in the filter
     *
     * @param c Collection of elements to examine
     * @return Returns true if all elements of the collection are in the filter.
     */
    public boolean containsAll(Collection<? extends E> c) {
        for (E element : c) {
            if (!contains(element)) {
                return false;
            }
        }

        return true;
    }

    /**
     * I'm going to get some value
     *
     * @param bit The location of the bit.
     * @return Returns true if the bit is set.
     */
    public boolean getBit(int bit) {
        return bitset.get(bit);
    }

    /**
     * Sets the value of a bit of the filter
     *
     * @param bit   The location to set.
     * @param value True indicates that the configuration is successful. False indicates that the change is cleared.
     */
    public void setBit(int bit, boolean value) {
        bitset.set(bit, value);
    }

    /**
     * Returns an array of bits that hold information.
     *
     * @return An array of.
     */
    public BitSet getBitSet() {
        return bitset;
    }

    /**
     * Gets the size of the bit array in the filter。
     *
     * @return The array size.
     */
    public int size() {
        return this.bitSetSize;
    }

    /**
     * Returns the number of added elements
     *
     * @return Number of elements.
     */
    public int count() {
        return this.numberOfAddedElements;
    }

    /**
     * Gets the maximum number of elements that can be added
     *
     * @return The largest number.
     */
    public int getExpectedNumberOfElements() {
        return expectedNumberOfFilterElements;
    }

    /**
     * Get the expected value of the number of bits occupied by each element
     *
     * @return The number of bits occupied by each element
     */
    public double getExpectedBitsPerElement() {
        return this.bitsPerElement;
    }

    /**
     * Get the actual number of bits occupied by each element
     *
     * @return The number of bits occupied by each element.
     */
    public double getBitsPerElement() {
        return this.bitSetSize / (double) numberOfAddedElements;
    }


    public void writeTo(OutputStream out) throws IOException {
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeInt(bitSetSize);
        dout.writeInt(expectedNumberOfFilterElements);
        dout.writeInt(getBitSet().toByteArray().length);
        dout.write(getBitSet().toByteArray());
    }

    public static BloomFilters readFrom(InputStream in) throws IOException {
        Preconditions.checkNotNull(in, "InputStream");

        try {
            DataInputStream din = new DataInputStream(in);
            int bitSetSize = din.readInt();
            int expectedNumberOfFilterElements = din.readInt();
            int dataLength = din.readInt();
            byte[] data = new byte[dataLength];

            for (int i = 0; i < data.length; ++i) {
                data[i] = din.readByte();
            }

            return new BloomFilters(bitSetSize, expectedNumberOfFilterElements, expectedNumberOfFilterElements, BitSet.valueOf(data));
        } catch (RuntimeException var9) {
            String message = "Unable to deserialize BloomFilter from InputStream.";
            throw new IOException(message, var9);
        }
    }
}
