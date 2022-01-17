package com.welab.wefe.common.util;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author yuxin.zhang
 */
public class SM2Util {
    private static final Logger LOG = LoggerFactory.getLogger(IpAddressUtil.class);

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static Sm2KeyPair generateKeyPair() {

        KeyPair keyPair = null;
        try {
            ECGenParameterSpec sm2Spec = new ECGenParameterSpec("sm2p256v1");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());
            //kpg.initialize(sm2Spec);
            kpg.initialize(sm2Spec, new SecureRandom());
            keyPair = kpg.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            LOG.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error(e.getMessage(), e);
        }
        return new Sm2KeyPair(
                Base64Util.encode(keyPair.getPublic().getEncoded()),
                Base64Util.encode(keyPair.getPrivate().getEncoded())
        );
    }

    public static class Sm2KeyPair {
        public String publicKey;
        public String privateKey;

        public Sm2KeyPair(String publicKey, String privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }
    }

    /**
     * Public key format conversion: Convert from string type to PublicKey type
     */
    public static PublicKey getPublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(publicKeyStr.getBytes(StandardCharsets.UTF_8));
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        return keyFactory.generatePublic(x509KeySpec);
    }

    /**
     * PrivateKey key format conversion: Convert from string type to PrivateKey type
     */
    public static PrivateKey getPrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(privateKeyStr.getBytes(StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
    }


    /**
     * The private key signature
     */
    public static String sign(String data, String privateKeyStr) throws Exception {
        Signature signature = Signature.getInstance(
                GMObjectIdentifiers.sm2sign_with_sm3.toString()
                , new BouncyCastleProvider());

        signature.initSign(getPrivateKey(privateKeyStr));
        signature.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.encodeBase64String(signature.sign());

    }

    public static boolean verify(byte[] data, PublicKey publicKey, String sign) throws Exception {
        Signature signature = Signature.getInstance(
                GMObjectIdentifiers.sm2sign_with_sm3.toString()
                , new BouncyCastleProvider());
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(Base64.decodeBase64(sign.getBytes()));
    }

}
