package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class CERTRecord extends ResourceRecord<CERTRecord.Data> {

    public record Data(short certType, short keyTag, byte algorithm, String b64Cert) implements RecordData {}

    //

    public CERTRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public CERTRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.CERT, data);
    }
    
}
