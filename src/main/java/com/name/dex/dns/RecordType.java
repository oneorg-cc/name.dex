package com.name.dex.dns;

import com.name.dex.dns.rr.*;

public enum RecordType {
    A(ARecord.class, ARecord.Data.class),
    AAAA(AAAARecord.class, AAAARecord.Data.class),
    AFSDB(AFSDBRecord.class, AFSDBRecord.Data.class),
    APL(APLRecord.class, APLRecord.Data.class),
    CAA(CAARecord.class, CAARecord.Data.class),
    CDNSKEY(CDNSKEYRecord.class, DNSKEYRecord.Data.class),
    CDS(CDSRecord.class, DSRecord.Data.class),
    CERT(CERTRecord.class, CERTRecord.Data.class),
    CNAME(CNAMERecord.class, CNAMERecord.Data.class),
    CSYNC(CSYNCRecord.class, CSYNCRecord.Data.class),
    DHCID(DHCIDRecord.class, DHCIDRecord.Data.class),
    DLV(DLVRecord.class, DLVRecord.Data.class),
    DNAME(DNAMERecord.class, DNAMERecord.Data.class),
    DNSKEY(DNSKEYRecord.class, DNSKEYRecord.Data.class),
    DS(DSRecord.class, DSRecord.Data.class),
    EUI48(EUI48Record.class, EUI48Record.Data.class),
    EUI64(EUI64Record.class, EUI64Record.Data.class),
    HINFO(HINFORecord.class, HINFORecord.Data.class),
    HIP(HIPRecord.class, HIPRecord.Data.class),
    HTTPS(HTTPSRecord.class, HTTPSRecord.Data.class),
    IPSECKEY(IPSECKEYRecord.class, IPSECKEYRecord.Data.class),
    KEY(KEYRecord.class, KEYRecord.Data.class),
    KX(KXRecord.class, KXRecord.Data.class),
    LOC(LOCRecord.class, LOCRecord.Data.class),
    MX(MXRecord.class, MXRecord.Data.class),
    NAPTR(NAPTRRecord.class, NAPTRRecord.Data.class),
    NS(NSRecord.class, NSRecord.Data.class),
    NSEC(NSECRecord.class, NSECRecord.Data.class),
    NSEC3(NSEC3Record.class, NSEC3Record.Data.class),
    NSEC3PARAM(NSEC3PARAMRecord.class, NSEC3PARAMRecord.Data.class),
    OPENPGPKEY(OPENPGPKEYRecord.class, OPENPGPKEYRecord.Data.class),
    PTR(PTRRecord.class, PTRRecord.Data.class),
    RRSIG(RRSIGRecord.class, RRSIGRecord.Data.class),
    RP(RPRecord.class, RPRecord.Data.class),
    SIG(SIGRecord.class, SIGRecord.Data.class),
    SMIMEA(SMIMEARecord.class, SMIMEARecord.Data.class),
    SOA(SOARecord.class, SOARecord.Data.class),
    SRV(SRVRecord.class, SRVRecord.Data.class),
    SSHFP(SSHFPRecord.class, SSHFPRecord.Data.class),
    SVCB(SVCBRecord.class, SVCBRecord.Data.class),
    TA(TARecord.class, TARecord.Data.class),
    TKEY(TKEYRecord.class, TKEYRecord.Data.class),
    TLSA(TLSARecord.class, TLSARecord.Data.class),
    TSIG(TSIGRecord.class, TSIGRecord.Data.class),
    TXT(TXTRecord.class, TXTRecord.Data.class),
    URI(URIRecord.class, URIRecord.Data.class),
    ZONEMD(ZONEMDRecord.class, ZONEMDRecord.Data.class);

    //

    private final Class<? extends ResourceRecord<?>> recordClass;
    private final Class<? extends RecordData> dataClass;

    //

    RecordType(
        Class<? extends ResourceRecord<?>> recordClass,
        Class<? extends RecordData> dataClass
    ) {
        this.recordClass = recordClass;
        this.dataClass = dataClass;
    }

    //

    public Class<? extends ResourceRecord<?>> recordClass() { return this.recordClass; }
    public Class<? extends RecordData> dataClass() { return this.dataClass; }
}
