package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class CSYNCRecord extends ResourceRecord<CSYNCRecord.Data> {

    public record Data(int soaSerial, short flags, long typeBitmap) implements RecordData {}

    //

    public CSYNCRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public CSYNCRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.CSYNC, data);
    }
    
}
