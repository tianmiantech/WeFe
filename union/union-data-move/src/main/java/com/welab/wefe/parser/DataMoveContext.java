package com.welab.wefe.parser;

import com.welab.wefe.App;

/**
 * @author aaron.li
 * @date 2021/12/13 14:28
 **/
public class DataMoveContext {
    private final static String COLLECTION_NAME_DATASET = "DataSet";

    public AbstractDataMoveParser getParser(String collectionName) {
        switch (collectionName) {
            case COLLECTION_NAME_DATASET:
                return (AbstractDataMoveParser) App.CONTEXT.getBean("dataSetCollectionDataMoveParser");
            default:
                return null;
        }
    }
}
