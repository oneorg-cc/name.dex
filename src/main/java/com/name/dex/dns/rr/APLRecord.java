package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

import java.util.List;

public class APLRecord extends ResourceRecord<APLRecord.Data> {

    public record Data(List<String> addressPrefixedList) implements RecordData {}

    //

    public APLRecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public APLRecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.APL, data);
    }
    
}
