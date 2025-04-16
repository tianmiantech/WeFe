/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.fusion.core.utils;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * @author hunter.zhao
 */
public class PSIUtils {

    protected static final Logger LOG = LoggerFactory.getLogger(PSIUtils.class);

    private static BufferedOutputStream out = null;

    public static void writeLineToFile(File file, byte[] line, int begin, int end) {
        if (begin == 0) {
            try {
                out = new BufferedOutputStream(new FileOutputStream(file));
            } catch (Exception e) {
                LOG.error("PSIUtils writeLineToFile exception: ", e);
            }
        }

        try {
            out.write(line);
        } catch (Exception e) {
            LOG.error("PSIUtils writeLineToFile exception: ", e);
        }

        if (begin == end - 1) {
            try {
                out.flush();
                out.close();
            } catch (Exception e) {
                LOG.error("PSIUtils writeLineToFile exception: ", e);
            }
        }
    }

    // 使用socket快速传输一个long数组
    public static void sendLongs(Socket socket, long[] longs) {
        LOG.info("begin send longs, length=" + longs.length);
        try {
            DataOutputStream sender = new DataOutputStream(socket.getOutputStream());
            sender.writeInt(longs.length);
            LOG.info("send longs, writeInt " + longs.length);
            for (long i : longs) {
                sender.writeLong(i);
                // 批量发送，可以提高性能。
                if (i % 500 == 0) {
                    sender.flush();
                }
            }
            sender.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOG.info("end send longs");
    }

    // 使用socket快速接收一个long数组
    public static long[] receiveLongs(Socket socket) throws StatusCodeWithException {
        LOG.info("begin receive longs");
        try {
            DataInputStream receiver = new DataInputStream(socket.getInputStream());
            int length = receiver.readInt();
            LOG.info("receive longs, length = " + length);
            if (length <= 0) {
                throw new StatusCodeWithException(
                        StatusCode.REMOTE_SERVICE_ERROR,
                        "receiveLongs error, first int = " + length);
            }
            long[] datas = new long[length];
            int count = 0;
            while (count < length) {
                long item = receiver.readLong();
                datas[count++] = item;
            }
            return datas;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new long[0];
    }


    public static void sendBytes(Socket socket, byte[] bytes) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(bytes);
        } catch (Exception e) {
            LOG.error("PSIUtils sendBytes exception: ", e);
        }
    }

    public static byte[] receiveBytes(Socket socket) {
        byte[] bytes = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            bytes = (byte[]) in.readObject();
        } catch (Exception e) {
            LOG.error("PSIUtils receiveBytes exception: ", e);
        }

        return bytes;
    }

    public static byte[][] receive2DBytes(Socket socket) {
        byte[][] bytes = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            bytes = (byte[][]) in.readObject();
        } catch (Exception e) {
            LOG.error("PSIUtils receive2DBytes exception: ", e);
        }

        return bytes;
    }

    public static List<byte[]> receive2DBytes2(Socket socket) {
        List<byte[]> bytes = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            bytes = (List<byte[]>) in.readObject();
        } catch (Exception e) {
            LOG.error("PSIUtils receive2DBytes2 exception: ", e);
        }
        return bytes;
    }

    public static List<String> receiveStringList(Socket socket) {
        List<String> stringList = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            stringList = (List<String>) in.readObject();
        } catch (Exception e) {
            LOG.error("PSIUtils receiveStringList exception: ", e);
        }

        return stringList;
    }

    public static long receiveLong(DataInputStream dIn) {
        try {
            return dIn.readLong();
        } catch (Exception e) {
            LOG.error("PSIUtils receiveInteger exception: ", e);
        }
        return 0;
    }

    public static void send2DBytes(Socket socket, byte[][] bytes) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(bytes);
        } catch (IOException e) {
            LOG.error("PSIUtils send2DBytes exception: ", e);
        }
    }

    public static void send2DBytes(Socket socket, List<byte[]> bytes) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(bytes);
        } catch (IOException e) {
            LOG.error("PSIUtils send2DBytes exception: ", e);
        }
    }

    public static void sendStringList(Socket socket, List<String> stringList) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(stringList);
        } catch (IOException e) {
            LOG.error("PSIUtils sendStringList exception: ", e);
        }
    }

    public static void sendInteger(DataOutputStream dOut, int integer) {
        try {
            dOut.writeInt(integer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendLong(DataOutputStream dOut, long integer) {
        try {
            dOut.writeLong(integer);
        } catch (IOException e) {
            LOG.error("PSIUtils sendInteger exception: ", e);
        }
    }

    public static void sendString(Socket socket, String s) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter
                    (new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(s);
        } catch (IOException e) {
            LOG.error("PSIUtils sendString exception: ", e);
        }
    }

    public static String receiveString(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return in.readLine();
        } catch (IOException e) {
            LOG.error("PSIUtils receiveString exception: ", e);
        }
        return "";
    }

    public static void sendFile(DataOutputStream dOut, File file) {
        try {
            FileInputStream fIn = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = (fIn.read(buffer))) > 0) {
                dOut.write(buffer, 0, read);
            }
            fIn.close();
            dOut.flush();
        } catch (IOException e) {
            LOG.error("PSIUtils sendFile exception: ", e);
        }
    }

    public static BigInteger stringToBigInteger(String s) {
        byte[] input = null;
        try {
            input = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            LOG.error("PSIUtils stringToBigInteger exception: ", e);
        }
        return bytesToBigInteger(input, 0, input.length);
    }

    public static BigInteger bytesToBigInteger(byte[] in, int inOff, int inLen) {
        byte[] block;

        if (inOff != 0 || inLen != in.length) {
            block = new byte[inLen];

            System.arraycopy(in, inOff, block, 0, inLen);
        } else {
            block = in;
        }

        return new BigInteger(1, block);
    }

    public static byte[] bigIntegerToBytes(BigInteger input, boolean forEncryption) {
        byte[] output = input.toByteArray();

        // have ended up with an extra zero byte, copy down.
        if (output[0] == 0) {
            byte[] tmp = new byte[output.length - 1];
            System.arraycopy(output, 1, tmp, 0, tmp.length);
            return tmp;
        }

        return output;
    }

    static byte[] sha1(String input, int length) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(input.getBytes());

        //128 bits
        return Arrays.copyOfRange(result, 0, length / 8);
    }

    static String bytesToBinaryString(byte[] input) {
        StringBuilder output = new StringBuilder("");
        for (int i = 0; i < input.length; i++) {
            output = output.append(String.format("%8s", Integer.toBinaryString(input[i] & 0xFF)).replace(' ', '0'));
        }
        return output.toString();
    }

    static byte[] padBytes(byte[] input, int size) {
        byte[] output = new byte[size];
        int start = size - input.length;
        System.arraycopy(input, 0, output, start, input.length);
        return output;
    }

}
