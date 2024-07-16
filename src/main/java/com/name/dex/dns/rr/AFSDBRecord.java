package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class AFSDBRecord extends ResourceRecord<AFSDBRecord.Data> {

    public record Data(short subtype, RecordData.Domain hostname) implements RecordData {}

    //

    public AFSDBRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public AFSDBRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.AFSDB, data);
    }
    
}
