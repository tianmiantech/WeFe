package com.welab.wefe.dto;

import com.mongodb.client.model.changestream.ChangeStreamDocument;
import org.bson.BsonDocument;
import org.bson.Document;

/**
 * @author aaron.li
 * @date 2021/12/13 14:05
 **/
public class EventDto {
    private ChangeStreamDocument<Document> event;
    private String dataBaseName;
    private String collectionName;
    private Document fullDocument;
    private BsonDocument documentKey;
    private String operationType;

    public ChangeStreamDocument<Document> getEvent() {
        return event;
    }

    public void setEvent(ChangeStreamDocument<Document> event) {
        this.event = event;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public void setDataBaseName(String dataBaseName) {
        this.dataBaseName = dataBaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public Document getFullDocument() {
        return fullDocument;
    }

    public void setFullDocument(Document fullDocument) {
        this.fullDocument = fullDocument;
    }

    public BsonDocument getDocumentKey() {
        return documentKey;
    }

    public void setDocumentKey(BsonDocument documentKey) {
        this.documentKey = documentKey;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
}
