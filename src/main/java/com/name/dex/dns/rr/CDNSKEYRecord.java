package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class CDNSKEYRecord extends ResourceRecord<DNSKEYRecord.Data> {

    public CDNSKEYRecord(RecordData.Domain name, int ttl, DNSKEYRecord.Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public CDNSKEYRecord(RecordData.Domain name, int ttl, RecordClass clazz, DNSKEYRecord.Data data) {
        super(name, ttl, clazz, RecordType.CDNSKEY, data);
    }
    
}
