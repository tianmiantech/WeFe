package com.welab.wefe.mpc.psi.sdk.dh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.mpc.util.DiffieHellmanUtil;

public class DhPsiServer {

    private static final Logger LOG = LoggerFactory.getLogger(DhPsiServer.class);
    private static int threads = Runtime.getRuntime().availableProcessors();

    private BigInteger serverPrivateD;
    private BigInteger p;
    private static int keySize = 1024;

    public DhPsiServer(String p) {
        this.serverPrivateD = generaterPrivateKey();
        this.p = new BigInteger(p, 16);
    }

    /**
     * step 1 对自己的数据集进行加密
     */
    public List<String> encryptDataset(List<String> serverIds) {
        List<String> encryptServerIds = new ArrayList<>(serverIds.size());
        serverIds.forEach(serverId -> encryptServerIds
                .add(DiffieHellmanUtil.encrypt(serverId, this.serverPrivateD, this.p).toString(16)));
        return encryptServerIds;
    }

    /**
     * step 2 对输入（客户端）的数据进行加密操作
     */
    public List<String> encryptClientDatasetMap(List<String> clientIds) {
        List<String> encryptClientIds = new ArrayList<>(clientIds.size());
        clientIds.forEach(id -> encryptClientIds
                .add(DiffieHellmanUtil.encrypt(id, this.serverPrivateD, this.p, false).toString(16)));
        return encryptClientIds;
    }

    /**
     * step 0 生成一个私钥
     */
    private BigInteger generaterPrivateKey() {
        return new BigInteger(keySize, new Random());
    }

    public BigInteger getP() {
        return p;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }

    public BigInteger getServerPrivateD() {
        return serverPrivateD;
    }

    public void setServerPrivateD(BigInteger serverPrivateD) {
        this.serverPrivateD = serverPrivateD;
    }

}
