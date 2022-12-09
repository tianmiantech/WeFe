package com.welab.wefe.mpc.psi.sdk.dh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.mpc.key.DiffieHellmanKey;
import com.welab.wefe.mpc.psi.sdk.operation.ListOperator;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;

public class DhPsiClient {

    private static final Logger LOG = LoggerFactory.getLogger(DhPsiClient.class);
    private static int threads = Runtime.getRuntime().availableProcessors();
    private static int keySize = 1024;
    private ListOperator operator;
    private BigInteger clientPrivateD; // 客户端私钥
    private BigInteger p;

    private List<String> originalClientIds; // 原始客户端数据
    private List<String> serverIdWithClientKeys; // 经过客户端加密后的服务端数据
    private List<String> clientIdByServerKeys; // 保存经过服务端二次加密后客户端的数据

    public DhPsiClient() {
        this.clientPrivateD = generaterPrivateKey();
        DiffieHellmanKey diffieHellmanKey = DiffieHellmanUtil.generateKey(keySize);
        this.p = diffieHellmanKey.getP();
    }

    /**
     * step 3 psi
     */
    public List<String> psi() {
        return operator.operator(this.originalClientIds, clientIdByServerKeys, serverIdWithClientKeys);
    }

    /**
     * step 2 加密服务端ID
     */
    public void encryptServerDataset(List<String> encryptServerIds) {
        // 对服务端ID进行加密
        this.serverIdWithClientKeys = new ArrayList<>(encryptServerIds.size());
        for (String serverId : encryptServerIds) {
            String encryptValue = DiffieHellmanUtil.encrypt(serverId, this.clientPrivateD, this.p, false).toString(16);
            serverIdWithClientKeys.add(encryptValue);
        }
    }

    /**
     * step 1 加密客户端ID
     */
    public List<String> encryptClientOriginalDataset() {
        // 加密客户端id
        List<String> encryptClientIds = new ArrayList<>(this.originalClientIds.size());
        for (String id : this.originalClientIds) {
            encryptClientIds.add(DiffieHellmanUtil.encrypt(id, this.clientPrivateD, this.p).toString(16));
        }
        return encryptClientIds;
    }

    /**
     * step 0 生成一个私钥
     */
    private BigInteger generaterPrivateKey() {
        return DiffieHellmanUtil.generateRandomKey(this.keySize);
    }

    public ListOperator getOperator() {
        return operator;
    }

    public void setOperator(ListOperator operator) {
        this.operator = operator;
    }

    public BigInteger getClientPrivateD() {
        return clientPrivateD;
    }

    public void setClientPrivateD(BigInteger clientPrivateD) {
        this.clientPrivateD = clientPrivateD;
    }

    public BigInteger getP() {
        return p;
    }

    public void setP(BigInteger p) {
        this.p = p;
    }

    public List<String> getServerIdWithClientKeys() {
        return serverIdWithClientKeys;
    }

    public void setServerIdWithClientKeys(List<String> serverIdWithClientKeys) {
        this.serverIdWithClientKeys = serverIdWithClientKeys;
    }

    public List<String> getClientIdByServerKeys() {
        return clientIdByServerKeys;
    }

    public void setClientIdByServerKeys(List<String> clientIdByServerKeys) {
        this.clientIdByServerKeys = clientIdByServerKeys;
    }

    public List<String> getOriginalClientIds() {
        return originalClientIds;
    }

    public void setOriginalClientIds(List<String> originalClientIds) {
        this.originalClientIds = originalClientIds;
    }

}
