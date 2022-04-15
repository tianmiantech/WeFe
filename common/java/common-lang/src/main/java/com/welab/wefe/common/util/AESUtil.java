package com.welab.wefe.common.util;

import com.welab.wefe.common.constant.Constant;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.SecureRandom;


/**
 * AES 加解密工具类
 *
 * @author hunter
 */
public class AESUtil {
    private static final Logger LOG = LoggerFactory.getLogger(AESUtil.class);

    private static final String AESTYPE = "AES/ECB/PKCS5Padding";
    private static final String AES_CBC_NO_PADDING = "AES/CBC/NoPadding";
    public static final String AES2 = "AES2";

    /**
     * AES 加密
     *
     * @param content 需要加密的内容
     * @param key     加密密码
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] content, byte[] key) {
        return aesSecure(content, key, Cipher.ENCRYPT_MODE);
    }

    /**
     * 不同 type 调用不同 AES加密
     * @param content
     * @param key
     * @param type
     * @return
     */
    public static byte[] encrypt(byte[] content, byte[] key, String type) {
        if(AES2.equals(type)){
            return aesSecure2(content, key, Cipher.ENCRYPT_MODE);
        }
        return aesSecure(content, key, Cipher.ENCRYPT_MODE);
    }

    /**
     * AES 加密
     *
     * @param content 需要加密的内容
     * @param key     加密密码
     * @return
     * @throws Exception
     */
    public static String encrypt(String content, String key) {
        try {
            byte[] bytes = encrypt(content.getBytes(Constant.ENCODING_UTF8), key.getBytes(Constant.ENCODING_UTF8));
            return EncryptUtils.parseByte2HexStr(bytes);
        } catch (UnsupportedEncodingException e) {
            LOG.error("【encrypt】String 编码异常，data：" + key, e);
        }
        return null;
    }

    /**
     * 不同type 调用不同 AES 加密
     * @param content
     * @param key
     * @param type
     * @return
     */
    public static String encrypt(String content, String key, String type) {
        try {
            byte[] bytes = encrypt(content.getBytes(Constant.ENCODING_UTF8), key.getBytes(Constant.ENCODING_UTF8), type);
            return EncryptUtils.parseByte2HexStr(bytes);
        } catch (UnsupportedEncodingException e) {
            LOG.error("【encrypt】String 编码异常，data：{},key:{},type:{},e:{}", content, key, type, e);
        }
        return null;
    }

    /**
     * AES 解密
     *
     * @param content 待解密内容
     * @param key     解密密钥
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] content, byte[] key) {
        return aesSecure(content, key, Cipher.DECRYPT_MODE);
    }

    /**
     * 不同type 调用不同 AES 解密
     * @param content
     * @param key
     * @param type
     * @return
     */
    public static byte[] decrypt(byte[] content, byte[] key, String type) {
        if(AES2.equals(type)){
            return aesSecure2(content, key, Cipher.DECRYPT_MODE);
        }
        return aesSecure(content, key, Cipher.DECRYPT_MODE);
    }

    /**
     * AES 解密
     *
     * @param content 待解密内容
     * @param key     解密密钥
     * @return
     * @throws Exception
     */
    public static String decrypt(String content, String key) {
        try {
            byte[] bytes = decrypt(EncryptUtils.parseHexStr2Byte(content), key.getBytes(Constant.ENCODING_UTF8));
            return new String(bytes, Constant.ENCODING_UTF8);
        } catch (UnsupportedEncodingException e) {
            LOG.error("【decrypt】String 编码异常，data：" + key, e);
        }
        return null;
    }

    /**
     * 不同type 调用不同方式 AES 解密
     *
     * @param content 待解密内容
     * @param key     解密密钥
     * @return
     * @throws Exception
     */
    public static String decrypt(String content, String key, String type) {
        try {
            byte[] bytes = decrypt(EncryptUtils.parseHexStr2Byte(content), key.getBytes(Constant.ENCODING_UTF8), type);
            return new String(bytes, Constant.ENCODING_UTF8);
        } catch (UnsupportedEncodingException e) {
            LOG.error("【decrypt】String 编码异常，data：" + key, e);
        }
        return null;
    }

    /**
     * AES 加解密
     *
     * @param content
     * @param key
     * @param mode
     * @return
     * @throws Exception
     */
    private static byte[] aesSecure(byte[] content, byte[] key, int mode) {

        byte[] result = null;
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(key);
            kgen.init(128, random);
            SecretKeySpec secretKey = new SecretKeySpec(kgen.generateKey().getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(mode, secretKey);
            result = cipher.doFinal(content);

        } catch (Exception e) {
            LOG.error("AES 加解密异常，mode：" + mode, e);
        }
        return result;

    }

    /**
     * AES2 加解密
     *
     * @param encryptData
     * @param keyStr
     * @param mode
     * @return
     */
    public static byte[] aesSecure2(byte[] encryptData, byte[] keyStr, int mode) {
        byte[] result = null;
        try {
            Key key = generateKey(keyStr);
            Cipher cipher = Cipher.getInstance(AESTYPE);
            cipher.init(mode, key);
            // decrypt = cipher.doFinal(Base64.decodeBase64(encryptData));
            result = cipher.doFinal(encryptData);
        } catch (Exception e) {
            LOG.error("AES 加解密异常，mode：" + mode, e);
        }
        return result;
    }


    /**
     * 加密方法
     *
     * @param data 要加密的数据
     * @param key  加密key
     * @param iv   加密iv
     * @return 加密的结果
     * @throws Exception
     */
    public static String aesSecureWithViParam(String data, String key, String iv){
        try {
            //"算法/模式/补码方式"
            Cipher cipher = Cipher.getInstance(AES_CBC_NO_PADDING);
            int blockSize = cipher.getBlockSize();

            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);

            return new Base64().encodeToString(encrypted);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static Key generateKey(byte[] key) throws Exception {
        SecretKeySpec keySpec = null;
        try {
            keySpec = new SecretKeySpec(key, "AES");
            return keySpec;
        } catch (Exception e) {
            LOG.error("AES2 generateKey error，key：{}" + key, e);
        }
        return null;
    }


    public static String encryptWithKeyBase64(String content, String key) {
        byte[] bytes = new byte[0];
        try {
            bytes = encrypt(content.getBytes("UTF-8"), new BASE64Decoder().decodeBuffer(key));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EncryptUtils.parseByte2HexStr(bytes);
    }

    public static String decryptWithKeyBase64(String content, String key) {
        byte[] originalData = EncryptUtils.parseHexStr2Byte(content);
        byte[] bytes = new byte[0];
        try {
            bytes = decrypt(originalData, new BASE64Decoder().decodeBuffer(key));
        } catch (IOException e) {
            LOG.error("【decryptWithKeyBase64】base64 编码异常，data：" + key, e);
        }
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("【decryptWithKeyBase64】String 编码异常，data：" + key, e);
        }
        return null;
    }
}
