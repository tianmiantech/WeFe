package com.welab.wefe.task;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.welab.wefe.common.OperationType;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.dto.EventDto;
import com.welab.wefe.parser.AbstractDataMoveParser;
import com.welab.wefe.parser.DataMoveContext;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author aaron.li
 * @date 2021/12/13 11:40
 **/
@Component
public class DataMoveTask {

    private final static List<Bson> MONITOR_OPERATION_TYPE = Arrays.asList(Aggregates.match(Filters.in("operationType", Arrays.asList(OperationType.insert, OperationType.update, OperationType.delete))));

    @Value("${move.collection.names}")
    private String moveCollectionNames;

    @Value("${spring.datasource.mongodb.union.databaseName}")
    private String unionDatabaseName;

    @Autowired
    private MongoClient mongoUnionClient;

    public void start() {
        if (StringUtil.isEmpty(moveCollectionNames) || (StringUtil.isEmpty(moveCollectionNames = moveCollectionNames.trim()))) {
            return;
        }

        List<String> moveCollectionNameList = Arrays.asList(moveCollectionNames.split(","));
        for (String moveCollectionName : moveCollectionNameList) {
            move(moveCollectionName);
        }
    }

    /**
     * 迁移
     *
     * @param collectionName 集合名
     */
    private void move(String collectionName) {
        try {
            AbstractDataMoveParser parser = new DataMoveContext().getParser(collectionName);
            if (null == parser) {
                return;
            }
            MongoDatabase database = mongoUnionClient.getDatabase(unionDatabaseName);
            MongoCollection<Document> collection = database.getCollection(collectionName);
            cn.hutool.core.thread.ThreadUtil.execAsync(() -> {
                if (!parser.fullSync()) {
                    return;
                }

                ChangeStreamIterable<Document> changeStream = collection.watch(MONITOR_OPERATION_TYPE).fullDocument(FullDocument.UPDATE_LOOKUP);
                changeStream.forEach((Block<? super ChangeStreamDocument<Document>>) event -> {
                    EventDto eventDto = new EventDto();
                    eventDto.setEvent(event);
                    eventDto.setDataBaseName(event.getDatabaseName());
                    eventDto.setCollectionName(collectionName);
                    eventDto.setFullDocument(event.getFullDocument());
                    eventDto.setDocumentKey(event.getDocumentKey());
                    eventDto.setOperationType(event.getOperationType().getValue());

                    parser.singleSync(eventDto);
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
