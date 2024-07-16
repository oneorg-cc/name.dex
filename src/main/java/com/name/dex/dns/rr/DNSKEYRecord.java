package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class DNSKEYRecord extends ResourceRecord<DNSKEYRecord.Data> {

    public record Data(short flags, byte protocol, byte algorithm, String b64PubKey) implements RecordData {}

    //

    public DNSKEYRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public DNSKEYRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.DNSKEY, data);
    }
    
}
