package com.ryanair.services.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Log4j2
@Service
public class JsonServiceImp implements JsonService {
    private ObjectMapper objectMapper;

    @Autowired
    public void set(ObjectMapper objectMapper) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalTime.class, new CustomLocalTimeDeserializer());
        objectMapper.registerModule(module);
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T mapper(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Json mapper error: " + json + System.lineSeparator() + e.getMessage());
        }
        return null;
    }

    @Override
    public <T> T mapper(String json,TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("Json mapper error: " + json + System.lineSeparator() + e.getMessage());
        }
        return null;
    }
}