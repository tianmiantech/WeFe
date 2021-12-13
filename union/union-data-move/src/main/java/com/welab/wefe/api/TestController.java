package com.welab.wefe.api;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author aaron.li
 * @date 2021/12/10 15:10
 **/
@RestController
public class TestController {
    @Value("${spring.datasource.mongodb.union.databaseName}")
    private String unionDatabaseName;

    @Autowired
    private MongoClient mongoUnionClient;

    @RequestMapping(value = "/test")
    public void test() {
        MongoDatabase database = mongoUnionClient.getDatabase(unionDatabaseName);
        MongoCollection<Document> collection = database.getCollection("AaronTest");
        List<Bson> pipeline = Arrays.asList(Aggregates.match(Filters.in("operationType", Arrays.asList("insert", "update", "delete"))));
       /* ChangeStreamIterable<Document> changeStream = database.watch(pipeline)
                .fullDocument(FullDocument.UPDATE_LOOKUP);
        changeStream.forEach((Block<? super ChangeStreamDocument<Document>>) event -> {
            System.out.println("Received a change to the collection: " + event);
        });*/
        ChangeStreamIterable<Document> changeStream2 = collection.watch(pipeline).fullDocument(FullDocument.UPDATE_LOOKUP);
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        changeStream2.forEach((Block<? super ChangeStreamDocument<Document>>) event -> {
            System.out.println("Received a change to the collection: " + event);
            String dataBaseName = event.getDatabaseName();
            String collectionName = event.getNamespace().getCollectionName();
            Document fullDocument = event.getFullDocument();
            BsonDocument documentKey = event.getDocumentKey();
            String operationType = event.getOperationType().getValue();
            System.out.println("dataBaseName=" + dataBaseName + ", collectionName=" + collectionName +
                    ", fullDocument=" + fullDocument.toJson() + ", documentKey=" + documentKey.toJson() + ", operationType=" + operationType);
        });
    }
}
