package com.name.dex.dns;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.name.dex.dns.rr.GenericRecord;
import com.name.dex.utils.HashKey;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ZoneConfiguration {

    public static final String FILE_EXTENSION = ".zone.json";

    //

    public static String getZoneFilename(ZoneConfiguration zone) {
        return getZoneFilename(zone.origin());
    }

    public static String getZoneFilename(RecordData.Domain origin) {
        return origin.toString() + ZoneConfiguration.FILE_EXTENSION.substring(1);
    }

    public static File getZoneFile(File datadir, ZoneConfiguration zone) {
        return getZoneFile(datadir, zone.origin());
    }

    public static File getZoneFile(File datadir, RecordData.Domain origin) {
        return new File(Paths.get(datadir.getPath().toString(), ZoneConfiguration.getZoneFilename(origin)).toString());
    }

    //

    public static class Key extends HashKey {

        public static ZoneConfiguration.Key from(ZoneConfiguration zone) {
            return new ZoneConfiguration.Key(zone);
        }

        //

        private final ZoneConfiguration zone;

        //

        private Key(ZoneConfiguration zone) {
            super(() -> List.of(zone.origin()));
            this.zone = zone;
        }

        //

        public ZoneConfiguration zone() { return this.zone; }

    }

    //

    public static ZoneConfiguration load(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, ZoneConfiguration.class);
    }

    //

    @JsonCreator
    public static ZoneConfiguration deserialize(
        @JsonProperty("origin") String origin,
        @JsonProperty("records") List<ResourceRecord<?>> records
    ) throws RecordData.Domain.InvalidFormatException {
        return new ZoneConfiguration(origin).setAllRecords(records);
    }

    //

    private final RecordData.Domain origin;
    private final Map<ResourceRecord.Key, ResourceRecord<?>> recordMap = new HashMap<>();

    //

    public ZoneConfiguration(String origin) throws RecordData.Domain.InvalidFormatException {
        this(new RecordData.Domain(origin));
    }

    public ZoneConfiguration(RecordData.Domain origin) {
        this.origin = origin;
    }

    //

    @JsonGetter("origin")
    public RecordData.Domain origin() { return this.origin; }

    public File getZoneFile(File datadir) {
        return ZoneConfiguration.getZoneFile(datadir, this);
    }

    @JsonGetter("records")
    public List<ResourceRecord<?>> records() {
        return this.recordMap.values().stream().toList();
    }

    public List<ResourceRecord<?>> records(Predicate<ResourceRecord<?>> filter) {
        return this.records().stream().filter(filter).toList();
    }

    //

    public boolean containsRecord(String name, RecordType type) throws RecordData.Domain.InvalidFormatException {
        return this.containsRecord(new RecordData.Domain(name), type);
    }

    public boolean containsRecord(RecordData.Domain name, RecordType type) {
        return this.containsRecord(new GenericRecord(name, 0, type, null));
    }

    public boolean containsRecord(ResourceRecord<?> record) {
        if(record == null)
            throw new IllegalArgumentException();

        return this.recordMap.containsKey(record.key());
    }

    //

    public ResourceRecord<?> getRecord(String name, RecordType type) throws RecordData.Domain.InvalidFormatException {
        return this.getRecord(new RecordData.Domain(name), type);
    }

    public ResourceRecord<?> getRecord(RecordData.Domain name, RecordType type) {
        return this.getRecord(new GenericRecord(name, 0, type, null));
    }

    public ResourceRecord<?> getRecord(ResourceRecord<?> record) {
        if(record == null)
            throw new IllegalArgumentException();

        return this.recordMap.get(record.key());
    }

    //

    public ResourceRecord<?> removeRecord(String name, RecordType type) throws RecordData.Domain.InvalidFormatException {
        return this.removeRecord(new RecordData.Domain(name), type);
    }

    public ResourceRecord<?> removeRecord(RecordData.Domain name, RecordType type) {
        return this.removeRecord(new GenericRecord(name, 0, type, null));
    }

    public ResourceRecord<?> removeRecord(ResourceRecord<?> record) {
        if(record == null)
            throw new IllegalArgumentException();

        return this.recordMap.remove(record.key());
    }

    //

    public ZoneConfiguration setRecord(ResourceRecord<?> record) {
        if(record == null)
            throw new IllegalArgumentException();

        this.removeRecord(record);
        this.recordMap.put(record.key(), record);

        return this;
    }

    @JsonSetter("records")
    public ZoneConfiguration setAllRecords(List<ResourceRecord<?>> records) {
        if(records == null)
            throw new IllegalArgumentException();

        for(ResourceRecord<?> record : records)
            this.setRecord(record);

        return this;
    }

}
