ALTER TABLE wefe_data_fusion.bloom_filter CHANGE process_count processed_count int(255) NULL;


-- 添加融合主键展示字段
ALTER TABLE bloom_filter ADD hash_function varchar(255) NULL;


