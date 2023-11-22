package com.ryanair.services.utils;

import com.fasterxml.jackson.core.type.TypeReference;

public interface JsonService {
    <T> T mapper(String json, Class<T> clazz);
    <T> T mapper(String json, TypeReference<T> typeReference);
}
