package com.welab.wefe.gateway.util;

import javax.persistence.AttributeConverter;

public class DatabaseEncryptConverter implements AttributeConverter<String, String> {
    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            return DatabaseEncryptUtil.encrypt(attribute);
        } catch (Exception e) {
        }
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            return DatabaseEncryptUtil.decrypt(dbData);
        } catch (Exception e) {
        }
        return dbData;
    }
}
