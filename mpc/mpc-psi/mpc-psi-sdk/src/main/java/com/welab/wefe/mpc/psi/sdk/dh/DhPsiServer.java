package com.welab.wefe.mpc.psi.sdk.dh;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.mpc.psi.sdk.util.PartitionUtil;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;

public class DhPsiServer {

    private static final Logger LOG = LoggerFactory.getLogger(DhPsiServer.class);
    private int threads = Math.max(Runtime.getRuntime().availableProcessors(), 8);

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
        LOG.info("server begin encryptDataset, threads = " + threads);
        List<String> encryptedSet = new CopyOnWriteArrayList<>();
        List<Set<String>> partitionList = PartitionUtil.partitionList(serverIds, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());
        for (Set<String> partition : partitionList) {
            executorService.submit(() -> {
                partition.forEach(serverId -> encryptedSet
                        .add(DiffieHellmanUtil.encrypt(serverId, this.serverPrivateD, this.p).toString(16)));
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
        LOG.info("server end encryptDataset");
        return encryptedSet;
    }

    /**
     * step 2 对输入（客户端）的数据进行加密操作
     */
    public List<String> encryptClientDatasetMap(List<String> clientIds) {
        LOG.info("server begin encryptClientDatasetMap, threads = " + threads);
        List<String> encryptClientIds = new CopyOnWriteArrayList<>();
        List<Set<String>> partitionList = PartitionUtil.partitionList(clientIds, threads);
        ExecutorService executorService = Executors.newFixedThreadPool(partitionList.size());
        for (Set<String> partition : partitionList) {
            executorService.submit(() -> {
                partition.forEach(id -> encryptClientIds
                        .add(DiffieHellmanUtil.encrypt(id, this.serverPrivateD, this.p, false).toString(16)));
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
        LOG.info("server end encryptClientDatasetMap");
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
