package com.welab.wefe.mpc.psi.sdk.dh;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.mpc.key.DiffieHellmanKey;
import com.welab.wefe.mpc.psi.sdk.util.PartitionUtil;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;

import cn.hutool.core.collection.CollectionUtil;

public class DhPsiClient {

    private static final Logger LOG = LoggerFactory.getLogger(DhPsiClient.class);
    private static int threads = Math.max(Runtime.getRuntime().availableProcessors(), 8);
    private static int keySize = 1024;
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
     * 
     * 返回两个列表的计算结果
     *
     * @param originIds        原始id列表
     * @param encryptOriginIds 加密原始id列表，大小与原始id列表相同
     * @param encryptServerIds 加密服务器id列表
     * @return
     */
    public List<String> psi() {
        if (CollectionUtil.isEmpty(originalClientIds) || CollectionUtil.isEmpty(clientIdByServerKeys)
                || CollectionUtil.isEmpty(serverIdWithClientKeys)) {
            return new ArrayList<>();
        }
        List<String> result = clientIdByServerKeys.stream().filter(item -> serverIdWithClientKeys.contains(item))
                .map(item -> originalClientIds.get(clientIdByServerKeys.indexOf(item))).collect(Collectors.toList());
        return result;
    }

    /**
     * step 2 加密服务端ID
     */
    public void encryptServerDataset(List<String> encryptServerIds) {
        LOG.info("client begin encryptServerDataset, threads = " + threads);
        List<String> doubleEncryptServerIds = new CopyOnWriteArrayList<>();
        List<Set<String>> partitionList = PartitionUtil.partitionList(encryptServerIds, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());
        for (Set<String> partition : partitionList) {
            executorService.submit(() -> {
                for (String serverId : partition) {
                    String encryptValue = DiffieHellmanUtil.encrypt(serverId, this.clientPrivateD, this.p, false)
                            .toString(16);
                    doubleEncryptServerIds.add(encryptValue);
                }
            });
        }
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // pass
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        // 对服务端ID进行加密
        this.serverIdWithClientKeys = new ArrayList<>(doubleEncryptServerIds);
        LOG.info("client end encryptServerDataset");
    }

    /**
     * step 1 加密客户端ID
     */
    public List<String> encryptClientOriginalDataset() {
        LOG.info("client begin encryptClientOriginalDataset, threads = " + threads);
        List<String> encryptClientIds = new CopyOnWriteArrayList<>();
        List<Set<String>> partitionList = PartitionUtil.partitionList(this.originalClientIds, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());
        for (Set<String> partition : partitionList) {
            executorService.submit(() -> {
                for (String id : partition) {
                    encryptClientIds.add(DiffieHellmanUtil.encrypt(id, this.clientPrivateD, this.p).toString(16));
                }
            });
        }
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // pass
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        LOG.info("client end encryptClientOriginalDataset");
        return encryptClientIds;
    }

    /**
     * step 0 生成一个私钥
     */
    private BigInteger generaterPrivateKey() {
        return DiffieHellmanUtil.generateRandomKey(this.keySize);
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
