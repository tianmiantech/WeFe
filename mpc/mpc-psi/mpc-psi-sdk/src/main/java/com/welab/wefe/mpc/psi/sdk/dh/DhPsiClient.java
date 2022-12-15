package com.welab.wefe.mpc.psi.sdk.dh;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.mpc.key.DiffieHellmanKey;
import com.welab.wefe.mpc.psi.sdk.util.PartitionUtil;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;

public class DhPsiClient {

    private static final Logger LOG = LoggerFactory.getLogger(DhPsiClient.class);
    private static int threads = Math.max(Runtime.getRuntime().availableProcessors(), 8);
    private static int keySize = 1024;
    private BigInteger clientPrivateD; // 客户端私钥
    private BigInteger p;

    private AtomicLong idAtomicCounter; // id 计数器
    private Map<Long, String> originalClientIds; // 原始客户端数据
    private Set<String> serverIdWithClientKeys; // 经过客户端加密后的服务端数据
    private Map<Long, String> clientIdByServerKeys; // 保存经过服务端二次加密后客户端的数据

    public DhPsiClient() {
        this.idAtomicCounter = new AtomicLong(0);
        this.originalClientIds = new ConcurrentHashMap<>();
//        this.clientIdByServerKeys = new ConcurrentHashMap<>();
        this.serverIdWithClientKeys = ConcurrentHashMap.newKeySet();
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
    public Set<String> psi() {
        LOG.info("client begin psi, threads = " + threads);
        Set<String> psi = ConcurrentHashMap.newKeySet();
        // 对客户端的数据进行分片
        List<Map<Long, String>> reversedMapPartition = PartitionUtil.partitionMap(this.clientIdByServerKeys, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(reversedMapPartition.size());
        for (Map<Long, String> partition : reversedMapPartition) {
            executorService.submit(() -> {
                for (Map.Entry<Long, String> entry : partition.entrySet()) {
                    // 如果服务端的数据中存在客户端的数据
                    if (this.serverIdWithClientKeys.contains(entry.getValue()))
                        psi.add(this.originalClientIds.get(entry.getKey()));
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
        LOG.info("client end psi");
        return psi;
    }

    /**
     * step 2 加密服务端ID
     */
    public void encryptServerDataset(List<String> encryptServerIds) {
        this.serverIdWithClientKeys = ConcurrentHashMap.newKeySet();
        LOG.info("client begin encryptServerDataset, threads = " + threads);
        List<Set<String>> partitionList = PartitionUtil.partitionList(encryptServerIds, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());
        for (Set<String> partition : partitionList) {
            executorService.submit(() -> {
                for (String serverId : partition) {
                    String encryptValue = DiffieHellmanUtil.encrypt(serverId, this.clientPrivateD, this.p, false)
                            .toString(16);
                    this.serverIdWithClientKeys.add(encryptValue);
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
        LOG.info("client end encryptServerDataset");
    }

    /**
     * step 1 加密客户端ID
     */
    public Map<Long, String> encryptClientOriginalDataset(List<String> clientIds) {
        idAtomicCounter = new AtomicLong(0);
        LOG.info("client begin encryptClientOriginalDataset, threads = " + threads);
        Map<Long, String> clientEncryptedDatasetMap = new ConcurrentHashMap<>();
        List<Set<String>> partitionList = PartitionUtil.partitionList(clientIds, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());
        for (Set<String> partition : partitionList) {
            executorService.submit(() -> {
                for (String id : partition) {
                    Long key = idAtomicCounter.incrementAndGet();
                    this.originalClientIds.put(key, id);
                    clientEncryptedDatasetMap.put(key,
                            DiffieHellmanUtil.encrypt(id, this.clientPrivateD, this.p).toString(16));
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
        return clientEncryptedDatasetMap;
    }

    /**
     * step 0 生成一个私钥
     */
    private static BigInteger generaterPrivateKey() {
        return DiffieHellmanUtil.generateRandomKey(keySize);
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

    public Map<Long, String> getOriginalClientIds() {
        return originalClientIds;
    }

    public void setOriginalClientIds(Map<Long, String> originalClientIds) {
        this.originalClientIds = originalClientIds;
    }

    public Set<String> getServerIdWithClientKeys() {
        return serverIdWithClientKeys;
    }

    public void setServerIdWithClientKeys(Set<String> serverIdWithClientKeys) {
        this.serverIdWithClientKeys = serverIdWithClientKeys;
    }

    public Map<Long, String> getClientIdByServerKeys() {
        return clientIdByServerKeys;
    }

    public void setClientIdByServerKeys(Map<Long, String> clientIdByServerKeys) {
        this.clientIdByServerKeys = clientIdByServerKeys;
    }

}
