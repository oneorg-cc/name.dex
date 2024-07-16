package com.name.dex.dns.rr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

import java.util.HashMap;
import java.util.Map;

public class GenericRecord extends ResourceRecord<GenericRecord.Data> {

    public static class Data extends HashMap<String, Object> implements RecordData {

        public Data(Map<String, Object> data) {
            this.putAll(data);
        }

    }

    //

    public GenericRecord(RecordData.Domain name, int ttl, RecordType type, GenericRecord.Data data) {
        this(name, ttl, RecordClass.IN, type, data);
    }

    public GenericRecord(RecordData.Domain name, int ttl, RecordClass clazz, RecordType type, GenericRecord.Data data) {
        super(name, ttl, clazz, type, data);
    }

}
