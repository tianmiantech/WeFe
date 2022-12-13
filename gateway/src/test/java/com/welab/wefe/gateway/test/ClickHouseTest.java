package com.welab.wefe.gateway.test;

import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorage;
import com.welab.wefe.common.data.storage.service.persistent.PersistentStorageStreamHandler;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.wefe.dto.storage.ClickhouseConfig;

import java.util.List;

public class ClickHouseTest {
    public static void main(String[] args) throws Exception {
        String host = "172.31.131.30";
        int port = 8122;
        String username = "wefe";
        String password = "wefe2020";
        String dataBase = "wefe_process";
        String table = "bbd5c9e04cfa447799dc6ec33cfd9bcf_promoter_Intersection_1670489400078749_promoter_49ef2a69ba354e7ab6df648ffb2d40a3_6ab166ae-79f7-11ed-9f80-0242ac120007";
        PersistentStorage.init(new ClickhouseConfig(host, port, username, password));
        PersistentStorage storage = PersistentStorage.getInstance();
        int byteSize = 10;
        int pageSize = storage.getCountByByteSize(dataBase, table, byteSize);
        long startTime = System.currentTimeMillis();
        storage.getByStream(dataBase, table, pageSize, new MyPersistentStorageStreamHandler());
        System.out.println("finish time:" + (System.currentTimeMillis() - startTime));
    }


    public static class MyPersistentStorageStreamHandler implements PersistentStorageStreamHandler {

        @Override
        public void handler(List<DataItemModel<byte[], byte[]>> itemModelList) throws Exception {
            System.out.println("data size=" + itemModelList.size());
            ThreadUtil.sleep(500);
        }

        @Override
        public void finish(long totalCount) {

        }
    }

}
