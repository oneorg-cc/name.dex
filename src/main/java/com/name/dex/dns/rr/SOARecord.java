package com.name.dex.dns.rr;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.name.dex.dns.RecordClass;
import com.name.dex.dns.RecordData;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;

public class SOARecord extends ResourceRecord<SOARecord.Data> {

    public record Data(RecordData.Domain nameserver, RecordData.Domain email, int serial, int refresh, int retry, int expire, int ttl) implements RecordData {

        public Data(String nameserver, String email, int serial, int refresh, int retry, int expire, int ttl) throws Domain.InvalidFormatException {
            this(new RecordData.Domain(nameserver), new RecordData.Domain(email), serial, refresh, retry, expire, ttl);
        }

        public Data(RecordData.Domain nameserver, RecordData.Domain email, int serial, int refresh, int retry, int expire, int ttl) {
            this.nameserver = nameserver;
            this.email = email;
            this.serial = serial;
            this.refresh = refresh;
            this.retry = retry;
            this.expire = expire;
            this.ttl = ttl;
        }

    }

    //

    public SOARecord(RecordData.Domain name, int ttl, Data data) {
        this(name, ttl, RecordClass.IN, data);
    }

    public SOARecord(RecordData.Domain name, int ttl, RecordClass clazz, Data data) {
        super(name, ttl, clazz, RecordType.SOA, data);
    }
    
}
