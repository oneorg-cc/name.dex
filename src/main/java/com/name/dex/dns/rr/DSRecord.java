package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class DSRecord extends ResourceRecord<DSRecord.Data> {

    public record Data(short keyTag, byte algorithm, byte digestType, String hexDigest) implements RecordData {}

    //

    public DSRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public DSRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.DS, data);
    }
    
}
