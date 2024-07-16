package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class RPRecord extends ResourceRecord<RPRecord.Data> {

    public record Data(String ip) implements RecordData {}

    //

    public RPRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public RPRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.RP, data);
    }
    
}
