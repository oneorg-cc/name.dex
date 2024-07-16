package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class CDSRecord extends ResourceRecord<DSRecord.Data> {

    public CDSRecord(RecordData.Domain name, int ttl, DSRecord.Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public CDSRecord(RecordData.Domain name, int ttl, RecordClass clazz, DSRecord.Data data) {
        super(name, ttl, clazz, RecordType.CDS, data);
    }
    
}
