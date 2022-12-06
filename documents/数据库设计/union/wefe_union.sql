db.getCollection('BlockSyncContractHeight').createIndex({"block_number":1},{background: true});

db.getCollection('BlockSyncDetailInfo').createIndex({"block_number":1},{background: true});
db.getCollection('BlockSyncDetailInfo').createIndex({"group_id":1},{background: true});
db.getCollection('BlockSyncDetailInfo').createIndex({"group_id":1,"block_number":1},{background: true});

db.getCollection('BloomFilter').createIndex({"data_resource_id":1},{background: true});

db.getCollection('DataResource').createIndex({"data_resource_id":1},{background: true});
db.getCollection('DataResource').createIndex({"member_id":1},{background: true});
db.getCollection('DataResource').createIndex({"status":1},{background: true});
db.getCollection('DataResource').createIndex({"enable":1},{background: true});
db.getCollection('DataResource').createIndex({"data_resource_type":1},{background: true});
db.getCollection('DataResource').createIndex({"name":1},{background: true});

db.getCollection('ImageDataSet').createIndex({"data_resource_id":1},{background: true});

db.getCollection('Member').createIndex({"member_id":1},{background: true});
db.getCollection('Member').createIndex({"name":1},{background: true});

db.getCollection('OperationLog').createIndex({"api_name":1},{background: true});

db.getCollection('TableDataSet').createIndex({"data_resource_id":1},{background: true});

db.getCollection('TableDataSet').createIndex({"data_resource_id":1},{background: true});

db.getCollection('fs.chunks').createIndex({"files_id":1},{background: true});

db.getCollection('fs.files').createIndex({"filename":1,"uploadDate":1},{background: true});