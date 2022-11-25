package com.welab.wefe.common.data.storage.service.persistent;

import com.welab.wefe.common.data.storage.model.DataItemModel;

import java.util.List;

public interface PersistentStorageStreamHandler {

    void handler(List<DataItemModel<byte[], byte[]>> itemModelList) throws Exception;
}
