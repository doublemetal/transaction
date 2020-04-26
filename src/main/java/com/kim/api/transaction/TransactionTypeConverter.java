package com.kim.api.transaction;

import com.kim.api.transaction.enums.TransactionType;

import javax.persistence.AttributeConverter;

public class TransactionTypeConverter implements AttributeConverter<TransactionType, String> {
    @Override
    public String convertToDatabaseColumn(TransactionType transactionType) {
        if (transactionType == null)
            return null;
        return transactionType.name();
    }

    @Override
    public TransactionType convertToEntityAttribute(String data) {
        return TransactionType.valueOf(data);
    }
}
