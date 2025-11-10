package com.delivery.common.cache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ICacheService {

    <T> Optional<T> get(String key, Class<T> type);

    <T> void set(String key, T value, long ttlSeconds);

    void delete(String key);

    <T> boolean setIfAbsent(String key, T value, long ttlSeconds);

    <T> Map<String, T> multiGet(List<String> keys, Class<T> type);

    void geoAdd(String key, double longitude, double latitude, String member);

    List<String> geoRadius(String key, double longitude, double latitude, double radiusKm);
}

