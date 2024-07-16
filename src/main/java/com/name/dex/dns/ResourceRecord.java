package com.name.dex.dns;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.name.dex.dns.rr.GenericRecord;
import com.name.dex.utils.HashKey;
import com.name.dex.utils.reflection.MethodsUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Map;

@JsonPropertyOrder({ "name", "ttl", "class", "type", "data" })
public abstract class ResourceRecord<D extends RecordData> {

    public static class Key extends HashKey {

        public static Key from(ResourceRecord<?> record) {
            return new Key(record);
        }

        //

        private final ResourceRecord<?> record;

        //

        private Key(ResourceRecord<?> record) {
            super(() -> {
                try {
                    return List.of(record.name(), record.type(), record.data().toContent());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });

            this.record = record;
        }

        //

        public ResourceRecord<?> record() { return this.record; }

    }

    //

    @JsonCreator
    public static ResourceRecord<?> deserialize(
        @JsonProperty("name") RecordData.Domain name,
        @JsonProperty("ttl") int ttl,
        @JsonProperty("class") RecordClass clazz,
        @JsonProperty("type") RecordType type,
        @JsonProperty("data") Map<String, Object> data,
        @SuppressWarnings("unused") @JsonProperty("content") String content
    ) {
        ResourceRecord<?> result = new GenericRecord(name, ttl, type, new GenericRecord.Data(data));

        //

        Class<? extends RecordData> dataClass = type.dataClass();
        Constructor<? extends RecordData>[] dataConstructors = (Constructor<? extends RecordData>[]) dataClass.getConstructors();

        RecordData finalData = null;
        Object[] finalDataArgs = new Object[data.keySet().size()];

        try {
            int argumentIndex = 0;
            for (RecordComponent component : dataClass.getRecordComponents()) {
                Object value = data.get(component.getName());
                Class<?> componentType = component.getType();

                if (componentType.isPrimitive()) {
                    Method valueOfMethod = MethodsUtil.filter(
                        value.getClass(),
                        method -> (
                            method.getName().equals(componentType.getName() + "Value")
                            && method.getParameterCount() == 0
                        )
                    ).get(0);

                    finalDataArgs[argumentIndex] = MethodsUtil.invoke(value, valueOfMethod);
                } else finalDataArgs[argumentIndex] = value;

//                System.err.println(finalDataArgs[argumentIndex]);
                argumentIndex++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int constructorIndex = 0; constructorIndex < dataConstructors.length && finalData == null; constructorIndex++) {
            try {
                finalData = dataConstructors[constructorIndex].newInstance(finalDataArgs);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        if (finalData == null) return null;

        //

        Class<? extends ResourceRecord<?>> recordClass = type.recordClass();
        Constructor<? extends ResourceRecord<?>>[] recordConstructors = (Constructor<? extends ResourceRecord<?>>[]) recordClass.getConstructors();

        for (int constructorIndex = 0; constructorIndex < recordConstructors.length && result instanceof GenericRecord; constructorIndex++) {
            try {
                result = recordConstructors[constructorIndex].newInstance(name, ttl, clazz, finalData);
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

//        return finalRecord == null ? new GenericRecord(name, 0, type, null) : finalRecord;
        return result;
    }

    //

    private final RecordData.Domain name;
    private int ttl;
    private RecordClass clazz;
    private final RecordType type;
    private D data;

    private final Key key;

    //

    protected ResourceRecord(String name, int ttl, RecordClass clazz, RecordType type, D data) throws RecordData.Domain.InvalidFormatException {
        this(new RecordData.Domain(name), ttl, clazz, type, data);
    }

    protected ResourceRecord(RecordData.Domain name, int ttl, RecordClass clazz, RecordType type, D data) {
        if(type == null) throw new IllegalArgumentException("Invalid record type (given: `" + type + "`).");

        this.name = name;
        this.ttl = ttl;
        this.clazz = clazz;
        this.type = type;
        this.data = data;

        this.key = Key.from(this);
    }

    //

    @JsonGetter("name")
    public RecordData.Domain name() { return this.name; }

    @JsonGetter("ttl")
    public int ttl() { return this.ttl; }
    public void setTtl(int ttl) { this.ttl = ttl; }

    @JsonGetter("class")
    public RecordClass clazz() { return this.clazz; }
    public void setClass(RecordClass clazz) { this.clazz = clazz; }

    @JsonGetter("type")
    public RecordType type() { return this.type; }

    @JsonGetter("data")
    public D data() { return this.data; }
    public void setData(D data) { this.data = data; }

    public Key key() { return this.key; }

}
