package com.welab.wefe.api;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
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
public class UpdateController {
    @Value("${spring.datasource.mongodb.union.databaseName}")
    private String unionDatabaseName;

    @Autowired
    private MongoClient mongoUnionClient;

    @RequestMapping(value = "/update")
    public void test() {
        MongoDatabase database = mongoUnionClient.getDatabase(unionDatabaseName);
        MongoCollection<Document> collection = database.getCollection("AaronTest");
        Document document = new Document();
        document.put("a", "20");
        document.put("b", "-1");
        collection.insertOne(document);
        collection.updateOne(document, Updates.set("a", "sample movie document update"));
        //Document document2 = new Document();
        //document2.put("_id","61b31c3b7d93c00974746e3d");
        //collection.deleteOne(Filters.eq("_id",new ObjectId("61b31c3b7d93c00974746e3d")));

        //collection.deleteMany(Filters.eq("a","20"));
    }
}
