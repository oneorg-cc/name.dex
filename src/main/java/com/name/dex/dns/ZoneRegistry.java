package com.name.dex.dns;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Supplier;

public class ZoneRegistry {

    private final File datadir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    //

    public ZoneRegistry(String datadir) {
        this(new File(datadir));
    }

    public ZoneRegistry(File datadir) {
        if(!datadir.isDirectory() || !datadir.canRead())
            throw new IllegalArgumentException("The given data directory argument is not a directory or not readable.");

        this.datadir = datadir;
//        this.objectMapper.setDefaultPrettyPrinter(new MinimalPrettyPrinter());
        this.objectMapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
    }

    //

    public File datadir() { return this.datadir; }

    //

    public List<Supplier<ZoneConfiguration>> zones() throws IOException {
        return Files.find(
            this.datadir.toPath(),
            1,
            (path, attributes) -> path.toFile().getName().endsWith(ZoneConfiguration.FILE_EXTENSION)
        ).map(path -> (Supplier<ZoneConfiguration>) () -> {
            try {
                File zonefile = path.toFile();
                return this.readZonefile(zonefile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }

    //

    public List<String> zonenames() throws IOException {
        return Files.find(
            this.datadir.toPath(),
            1,
            (path, attributes) -> {
                return path.toFile().getName().endsWith(ZoneConfiguration.FILE_EXTENSION);
            }
        ).map(path -> {
            String zonefileName = path.toFile().getName();
            return zonefileName.substring(
                0, zonefileName.length() - ZoneConfiguration.FILE_EXTENSION.substring(1).length()
            );
        }).toList();
    }

    //

    public ZoneConfiguration getZone(String origin) throws RecordData.Domain.InvalidFormatException, IOException {
        return this.getZone(new RecordData.Domain(origin));
    }

    public ZoneConfiguration getZone(RecordData.Domain origin) throws IOException {
        return this.readZonefile(ZoneConfiguration.getZoneFile(this.datadir(), origin));
    }

    public boolean hasZone(String origin) throws RecordData.Domain.InvalidFormatException, IOException {
        return this.getZone(new RecordData.Domain(origin)) != null;
    }

    public boolean hasZone(RecordData.Domain origin) throws IOException {
        return this.getZone(origin) != null;
    }

    //

    public ZoneConfiguration findMasterZone(RecordData.Domain name) throws IOException, RecordData.Domain.InvalidFormatException {
        if(name.toString().equals(".")) return null;

        ZoneConfiguration result = this.getZone(name);
        return result == null ? this.findMasterZone(name.shifted()) : result;
    }

    public boolean hasMasterZone(RecordData.Domain name) throws IOException, RecordData.Domain.InvalidFormatException {
        return this.findMasterZone(name) != null;
    }

    //

    public void setZone(ZoneConfiguration zone) throws IOException {
        this.objectMapper.writerWithDefaultPrettyPrinter().writeValue(zone.getZoneFile(this.datadir()), zone);
    }

    //

    private ZoneConfiguration readZonefile(File zonefile) throws IOException {
        return zonefile.exists() ? this.objectMapper.readValue(zonefile, ZoneConfiguration.class) : null;
    }

    private ZoneConfiguration writeZonefile(File zonefile) throws IOException {
        return zonefile.exists() ? this.objectMapper.readValue(zonefile, ZoneConfiguration.class) : null;
    }

}
