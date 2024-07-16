package com.name.dex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.name.dex.dns.*;
import com.name.dex.dns.rr.AAAARecord;
import com.name.dex.dns.rr.ARecord;
import com.name.dex.dns.rr.CNAMERecord;
import com.name.dex.rest.api.dnsbackend.PowerDNSBackendAbstract;
import com.name.dex.rest.api.dnsbackend.RecordQuery;
import com.name.dex.utils.EnumsUtil;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class PowerDNSBackend extends PowerDNSBackendAbstract {

    private final ZoneRegistry zoneRegistry;

    //

    public PowerDNSBackend(ZoneRegistry zoneRegistry) throws RouteListenerAlreadyRegisteredException, URISyntaxException {
        super();

        this.zoneRegistry = zoneRegistry;
    }

    //

    public ZoneRegistry zoneRegistry() { return this.zoneRegistry; }

    //

    @Override
    public boolean initialize() {
        return false;
    }

    @Override
    public List<RecordQuery> lookup(String qtype, String qname, String remote, String local, String realRemote, int zoneId) {
        List<RecordQuery> result = List.of();

        try {
            ZoneConfiguration zone = this.zoneRegistry().findMasterZone(new RecordData.Domain(qname));
//            ZoneConfiguration zone = this.zoneRegistry().getZone(new RecordData.Domain(qname));

            if(zone != null) {
                Predicate<ResourceRecord<?>> qtypeFilter = RecordQuery.QTYPE.ANY.name().equals(qtype)
                    ? resourceRecord -> RecordQuery.QTYPE.ANY.typeSet().contains(resourceRecord.type())
                    : resourceRecord -> resourceRecord.type().equals(EnumsUtil.safeValueIgnoreCaseOf(qtype, RecordType.class));

                Predicate<ResourceRecord<?>> qnameFilter = resourceRecord -> resourceRecord.name().toString().equals(qname);

                List<? extends ResourceRecord<?>> records = zone.records().stream().filter(resourceRecord -> qtypeFilter.test(resourceRecord) && qnameFilter.test(resourceRecord)).toList();
                records = records.stream().map(resourceRecord -> {
                    ResourceRecord<?> resultRecord = resourceRecord;

                    System.out.println(resultRecord.getClass());
                    System.out.println(resultRecord.name() + " | " + zone.origin());

                    if(
                        (resourceRecord instanceof CNAMERecord cnameRecord)
                        && resourceRecord.name().equals(zone.origin())
                    ) {
                        try {
                            InetAddress address = InetAddress.getByName(cnameRecord.data().domain().toString());
                            String ip = address.getHostAddress();

                            if(address instanceof Inet4Address) {
                                resultRecord = new ARecord(cnameRecord.name(), cnameRecord.ttl(), cnameRecord.clazz(), new ARecord.Data(ip));
                            }
                            else if(address instanceof Inet6Address) {
                                resultRecord = new AAAARecord(cnameRecord.name(), cnameRecord.ttl(), cnameRecord.clazz(), new AAAARecord.Data(ip));
                            }
                        } catch (UnknownHostException e) {
                        }
                    }

                    return resultRecord;
                }).toList();

                result = records.stream().map(resourceRecord -> {
                    try {
                        return RecordQuery.from(resourceRecord, Optional.empty(), Optional.empty(), Optional.empty());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();
            }
        } catch (RecordData.Domain.InvalidFormatException e) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public List<RecordQuery> list(String zonename, int domainId) {
        return List.of();
    }

    @Override
    public List<PartialDomainInfo> getUpdatedMasters() {
        return List.of();
    }

    @Override
    public boolean setNotified(int id, int serial) {
        return false;
    }

    @Override
    public List<DomainInfo> getAllDomains(boolean includeDisabled) {
        try {
            return this.zoneRegistry().zonenames().stream().map(zonename -> {
                int id = zonename.hashCode();
                int serial = id;
                int notifiedSerial = serial;

                return new DomainInfo(id, zonename, List.of(), ZoneKind.MASTER, serial, notifiedSerial);
            }).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
