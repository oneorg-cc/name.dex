package com.name.dex.rest.api.dnsbackend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.name.dex.dns.RecordType;
import com.name.dex.dns.ResourceRecord;
import com.name.dex.utils.serialization.RecordSerializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Optional;
import java.util.Set;

@JsonSerialize(using = RecordSerializer.class)
public record RecordQuery(
    String qtype,
    String qname,
    String content,
    int ttl,
    @JsonProperty("domain_id") Optional<Integer> domainId,
    Optional<Integer> scopeMask,
    Optional<Boolean> auth
) {

    public enum QTYPE {
        ANY(Set.of(
            RecordType.SOA,
            RecordType.NS,
            RecordType.A,
            RecordType.AAAA,
            RecordType.CNAME
        ));

        //

        private final Set<RecordType> typeSet;

        //

        QTYPE(Set<RecordType> typeSet) {
            this.typeSet = typeSet;
        }

        //

        public Set<RecordType> typeSet() { return this.typeSet; }
    }

    //

    public static RecordQuery from(ResourceRecord<?> record, Optional<Integer> domainId, Optional<Integer> scopeMask, Optional<Boolean> auth) throws JsonProcessingException {
        return new RecordQuery(
            record.type().name(),
            record.name().toString(),
            record.data().toContent(),
            record.ttl(),
            domainId,
            scopeMask,
            auth
        );
    }

    //

    @Override
    @JsonProperty("domain_id")
    public Optional<Integer> domainId() { return domainId; }

}
