package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class HTTPSRecord extends ResourceRecord<HTTPSRecord.Data> {

    public record Data(short priority, String target, String value) implements RecordData {}

    //

    public HTTPSRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public HTTPSRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.HTTPS, data);
    }
    
}
