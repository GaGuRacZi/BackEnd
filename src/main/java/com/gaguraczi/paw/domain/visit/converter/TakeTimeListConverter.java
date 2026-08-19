package com.gaguraczi.paw.domain.visit.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.visit.enums.TakeTime;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Converter
public class TakeTimeListConverter implements AttributeConverter<List<TakeTime>, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<TakeTime>> LIST_TYPE = new TypeReference<>() {
    };

    @Override
    public String convertToDatabaseColumn(List<TakeTime> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize take times", e);
        }
    }

    @Override
    public List<TakeTime> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<TakeTime> parsed = OBJECT_MAPPER.readValue(dbData, LIST_TYPE);
            return parsed == null ? new ArrayList<>() : new ArrayList<>(parsed);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to deserialize take times", e);
        }
    }
}
