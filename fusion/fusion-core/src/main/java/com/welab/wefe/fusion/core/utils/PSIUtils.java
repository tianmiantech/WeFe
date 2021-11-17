/**
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

package com.welab.wefe.fusion.core.utils;

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

    private static BufferedOutputStream out = null;

    public static void writeLineToFile(File file, byte[] line, int begin, int end) {
        if (begin == 0) {
            try {
                out = new BufferedOutputStream(new FileOutputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            out.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (begin == end - 1) {
            try {
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendBytes(Socket socket, byte[] bytes) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] receiveBytes(Socket socket) {
        byte[] bytes = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            bytes = (byte[]) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static byte[][] receive2DBytes(Socket socket) {
        byte[][] bytes = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            bytes = (byte[][]) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static List<byte[]> receive2DBytes2(Socket socket) {
        List<byte[]> bytes = null;
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            bytes = (List<byte[]>) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static long receiveInteger(DataInputStream d_in) {
        try {
            long size = d_in.readLong();
            return size;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void send2DBytes(Socket socket, byte[][] bytes) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send2DBytes(Socket socket, List<byte[]> bytes) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void sendInteger(DataOutputStream d_out, long integer) {
        try {
            d_out.writeLong(integer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendString(Socket socket, String s) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter
                    (new OutputStreamWriter(socket.getOutputStream())), true);
            out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String receiveString(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void sendFile(DataOutputStream d_out, File file) {
        try {
            FileInputStream f_in = new FileInputStream(file);

            byte[] buffer = new byte[4096];
            int read = 0;
            while ((read = (f_in.read(buffer))) > 0) {
                d_out.write(buffer, 0, read);
            }
            f_in.close();
            d_out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BigInteger stringToBigInteger(String s) {
        byte[] input = null;
        try {
            input = s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

        BigInteger res = new BigInteger(1, block);

        return res;
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
