package com.welab.wefe.entity;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.exception.CustomRuntimeException;

public class EcdhPsiClient {

    private static final Logger LOG = LoggerFactory.getLogger(EcdhPsiClient.class);

    private BigInteger clientPrivateD; // 客户端私钥
    private ECCurve ecCurve; // 椭圆曲线
    private EllipticCurve ellipticCurve; // 椭圆曲线实现类

    private Map<Long, BigInteger> clientClearDatasetMap; // 保存已经处理过的数据，key为自增序列ID
    private Map<Long, ECPoint> clientDoubleEncryptedDatasetMap; // 保存经过服务端二次加密后客户端的数据
    private Set<ECPoint> serverDoubleEncryptedDataset; // 保存经过自己二次加密后的服务端的数据

    private AtomicLong idAtomicCounter; // id 计数器

    public EcdhPsiClient() {
        this.serverDoubleEncryptedDataset = ConcurrentHashMap.newKeySet();
        this.clientClearDatasetMap = new ConcurrentHashMap<>();
        this.clientDoubleEncryptedDatasetMap = new ConcurrentHashMap<>();

        this.idAtomicCounter = new AtomicLong(0);
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
        this.ellipticCurve = new EllipticCurve(ecSpec);
        this.ecCurve = ecSpec.getCurve();
        this.clientPrivateD = generaterPrivateKey(ecSpec);
    }

    /**
     * 生成一个私钥
     */
    private BigInteger generaterPrivateKey(ECParameterSpec ecSpec) {
        Security.addProvider(new BouncyCastleProvider());
        KeyPairGenerator keyGenerator;
        try {
            keyGenerator = KeyPairGenerator.getInstance("EC", "BC");
            keyGenerator.initialize(ecSpec, new SecureRandom());
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CustomRuntimeException("EC key generator not available");
        }
        KeyPair pair = keyGenerator.genKeyPair();
        return ((ECPrivateKey) pair.getPrivate()).getD();
    }

    /**
     * 生成一个私钥
     */
    private BigInteger generaterPrivateKey1(ECParameterSpec ecSpec) {
        BigInteger n = ecSpec.getN();
        // 随机状态
        SecureRandom secureRandom = new SecureRandom();
        BigInteger k = new BigInteger(n.bitLength(), secureRandom).mod(n);
        return k;
    }
}
