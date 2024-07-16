package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class SMIMEARecord extends ResourceRecord<SMIMEARecord.Data> {

    public record Data(String ip) implements RecordData {}

    //

    public SMIMEARecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public SMIMEARecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.SMIMEA, data);
    }
    
}
