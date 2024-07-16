package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class NSEC3PARAMRecord extends ResourceRecord<NSEC3PARAMRecord.Data> {

    public record Data(String ip) implements RecordData {}

    //

    public NSEC3PARAMRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public NSEC3PARAMRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.NSEC3PARAM, data);
    }
    
}
