package com.name.dex.utils.rest.api;

import com.sun.net.httpserver.Headers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiHeaders {

    public static ApiHeaders unmodifiable(ApiHeaders headers) {
        return new ApiHeaders(headers.map());
    }

    //

    private final Map<String, Map.Entry<String, List<String>>> internalHeaderMap = new HashMap<>();

    //

    public ApiHeaders() {}

    private ApiHeaders(Map<String, List<String>> headerMap) {
        this.setFrom(headerMap);
    }

    //

    public Map<String, List<String>> map() {
        Map<String, List<String>> headerMap = new HashMap<>();

        for(Map.Entry<String, Map.Entry<String, List<String>>> entry : this.internalHeaderMap.entrySet()) {
            Map.Entry<String, List<String>> realEntry = entry.getValue();
            headerMap.put(realEntry.getKey(), realEntry.getValue());
        }

        return Collections.unmodifiableMap(headerMap);
    }

    //

    private List<String> get(ApiHeader header) {
        return this.get(header.key());
    }

    public List<String> get(String key) {
        return this.internalHeaderMap.containsKey(key.toLowerCase()) ? this.internalHeaderMap.get(key.toLowerCase()).getValue() : null;
    }

    public ApiHeaders set(ApiHeader header, String value) {
        return this.set(header.key(), value);
    }

    public ApiHeaders set(String key, String value) {
        return this.set(key, List.of(value));
    }

    public ApiHeaders set(ApiHeader header, List<String> values) {
        return this.set(header.key(), values);
    }

    public ApiHeaders set(String key, List<String> values) {
        String flattenedKey = key.toLowerCase();

        this.internalHeaderMap.remove(flattenedKey);
        this.internalHeaderMap.put(flattenedKey, Map.entry(key, values));

        return this;
    }

    public ApiHeaders add(ApiHeader header, String value) {
        return this.add(header.key(), value);
    }

    public ApiHeaders add(String key, String value) {
        return this.add(key, List.of(value));
    }

    public ApiHeaders add(ApiHeader header, List<String> values) {
        return this.add(header.key(), values);
    }

    public ApiHeaders add(String key, List<String> values) {
        String flattenedKey = key.toLowerCase();

        if(!this.internalHeaderMap.containsKey(flattenedKey))
            this.internalHeaderMap.put(flattenedKey, Map.entry(key, values));
        else
            this.internalHeaderMap.get(flattenedKey).getValue().addAll(values);

        return this;
    }

    //

    public ApiHeaders setFrom(Headers httpHeaders) {
        for(Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
            this.set(entry.getKey(), entry.getValue());
        }

        return this;
    }

    public ApiHeaders setFrom(ApiHeaders headers) {
        return this.setFrom(headers.map());
    }

    public ApiHeaders setFrom(Map<String, List<String>> headerMap) {
        for(Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
            this.internalHeaderMap.put(entry.getKey().toLowerCase(), entry);
        }

        return this;
    }

}
