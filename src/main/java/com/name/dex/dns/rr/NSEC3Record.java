package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class NSEC3Record extends ResourceRecord<NSEC3Record.Data> {

    public record Data(String ip) implements RecordData {}

    //

    public NSEC3Record(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public NSEC3Record(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.NSEC3, data);
    }
    
}
