package com.name.dex.dns.rr;

import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class CNAMERecord extends ResourceRecord<CNAMERecord.Data> {

    public record Data(RecordData.Domain domain) implements RecordData {

        public Data(String domain) throws Domain.InvalidFormatException {
            this(new RecordData.Domain(domain));
        }

        public Data(RecordData.Domain domain) {
            this.domain = domain;
        }

    }

    //

    public CNAMERecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public CNAMERecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.CNAME, data);
    }
    
}
