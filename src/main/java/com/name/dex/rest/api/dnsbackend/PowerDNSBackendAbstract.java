package com.name.dex.rest.api.dnsbackend;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.name.dex.dns.ZoneKind;
import com.name.dex.utils.rest.api.*;
import com.name.dex.utils.serialization.RecordSerializer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class PowerDNSBackendAbstract extends ApiRouter implements ApiRouteListener {

    @JsonSerialize(using = RecordSerializer.class)
    public record PartialDomainInfo(
        int id,
        String zone,
        Optional<List<String>> masters,
        Optional<ZoneKind> kind,
        int serial,
        @JsonProperty("notified_serial") int notifiedSerial
    ) {

        @JsonProperty("notified_serial")
        @Override
        public int notifiedSerial() { return notifiedSerial; }

        public DomainInfo toFixed() {
            return new DomainInfo(
                this.id(),
                this.zone(),
                this.masters().get(),
                this.kind().get(),
                this.serial(),
                this.notifiedSerial()
            );
        }

    }

    public record DomainInfo(
        int id,
        String zone,
        List<String> masters,
        ZoneKind kind,
        int serial,
        @JsonProperty("notified_serial") int notifiedSerial
    ) {

        @JsonProperty("notified_serial")
        @Override
        public int notifiedSerial() { return notifiedSerial; }

        public PartialDomainInfo toPartial() {
            return new PartialDomainInfo(
                this.id(),
                this.zone(),
                Optional.of(this.masters()),
                Optional.of(this.kind()),
                this.serial(),
                this.notifiedSerial()
            );
        }

    }

    //

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String rootPath;

    //

    protected PowerDNSBackendAbstract() throws RouteListenerAlreadyRegisteredException, URISyntaxException {
        this("");
    }

    protected PowerDNSBackendAbstract(String rootPath) throws RouteListenerAlreadyRegisteredException, URISyntaxException {
        this.objectMapper.setDefaultPrettyPrinter(new MinimalPrettyPrinter());
        this.x();

        this.setRootPath(rootPath);

        this.setRequestMapper(request -> {
            ApiRequest mappedRequest = null;

            String requestPath = request.uri().getPath();
            String _rootPath = this.getRootPath();

            if(
                requestPath.startsWith(_rootPath + "/")
                || requestPath.equals(_rootPath)
            ) {
                String newRequestPath = requestPath.substring(_rootPath.length());
                if(!newRequestPath.startsWith("/"))
                    newRequestPath = "/" + newRequestPath;

                mappedRequest = new ApiRequest(
                    request.method(),
                    newRequestPath,
                    request.queries(),
                    request.headers()
                );
            }

            return mappedRequest;
        });
    }

    private void x() throws RouteListenerAlreadyRegisteredException, URISyntaxException {
        this.registerRouteListener(this);
    }

    //

    public void setRootPath(String rootPath) { this.rootPath = rootPath.endsWith("/") ? rootPath.substring(0, rootPath.length()) : rootPath; }
    public String getRootPath() { return this.rootPath; }

    //

    /*
     * Always required.
     */

    protected abstract boolean initialize();

    @ApiRouteHandler(methods = ApiRequest.Method.GET, endpoint = "/lookup/{qname}/{qtype}")
    private void lookupEndpoint(
        ApiRequest request,
        Map<String, String> parameters,
        ApiRoute.ResponseSender sender
    ) throws ApiRouter.ResponseAlreadySentException, IOException {
        ApiResponse response = new ApiResponse();

        String qtype = parameters.get("qtype");
        String qname = parameters.get("qname");

        List<String> remoteHeaderValues = request.headers().get("X-RemoteBackend-remote");
        String remote = remoteHeaderValues == null || remoteHeaderValues.size() == 0 ? null : remoteHeaderValues.get(0);

        List<String> localHeaderValues = request.headers().get("X-RemoteBackend-local");
        String local = localHeaderValues == null || localHeaderValues.size() == 0 ? null : localHeaderValues.get(0);

        List<String> realRemoteHeaderValues = request.headers().get("X-RemoteBackend-real-remote");
        String realRemote = realRemoteHeaderValues == null || realRemoteHeaderValues.size() == 0 ? null : realRemoteHeaderValues.get(0);

        List<String> zoneIdHeaderValues = request.headers().get("X-RemoteBackend-zone-id");
        int zoneId = zoneIdHeaderValues == null || zoneIdHeaderValues.size() == 0 ? -1 : Integer.parseInt(zoneIdHeaderValues.get(0));

        List<RecordQuery> recordQueries = this.lookup(qtype, qname, remote, local, realRemote, zoneId);

        sender.send(response);

        try(
            JsonGenerator responseContentGenerator = this.objectMapper.createGenerator(response.body().writer())
        ) {
            responseContentGenerator.writeStartObject();
            responseContentGenerator.writeObjectField("result", recordQueries);
            responseContentGenerator.writeEndObject();
        }
    }
    protected abstract List<RecordQuery> lookup(String qtype, String qname, String remote, String local, String realRemote, int zoneId);

    /*
     * Master operations.
     */

    @ApiRouteHandler(methods = ApiRequest.Method.GET, endpoint = "/list/{domain_id}/{zonename}")
    private void listEndpoint(
        ApiRequest request,
        Map<String, String> parameters,
        ApiRoute.ResponseSender sender
    ) throws ApiRouter.ResponseAlreadySentException, IOException {

    }
    protected abstract List<RecordQuery> list(String zonename, int domainId);

    @ApiRouteHandler(methods = ApiRequest.Method.GET, endpoint = "/getUpdatedMasters")
    private void getUpdatedMastersEndpoint(
        ApiRequest request,
        Map<String, String> parameters,
        ApiRoute.ResponseSender sender
    ) throws ApiRouter.ResponseAlreadySentException, IOException {

    }
    protected abstract List<PartialDomainInfo> getUpdatedMasters();

    @ApiRouteHandler(methods = ApiRequest.Method.PATCH, endpoint = "/setnotified/{id}")
    private void setNotifiedEndpoint(
        ApiRequest request,
        Map<String, String> parameters,
        ApiRoute.ResponseSender sender
    ) throws ApiRouter.ResponseAlreadySentException, IOException {

    }
    protected abstract boolean setNotified(int id, int serial);

    /*
     * TO-DO: slavery requirements.
     */

//    void getUnfreshSlaveInfos();
//
//    void startTransaction();
//
//    void commitTransaction();
//
//    void abortTransaction();
//
//    void feedRecord();
//
//    void setFresh();

    /*
     * TO-DO: DNSSEC requirements.
     */

//    void getDomainKeys();
//
//    void getBeforeAndAfterNamesAbsolute();

    /*
     * TO-DO: DNSSEC requirements.
     */

    @ApiRouteHandler(methods = ApiRequest.Method.GET, endpoint = "/getAllDomains")
    private void getAllDomainsEndpoint(
        ApiRequest request,
        Map<String, String> parameters,
        ApiRoute.ResponseSender sender
    ) throws ApiRouter.ResponseAlreadySentException, IOException {
        ApiResponse response = new ApiResponse();

        boolean includeDisabled = String.valueOf(true).equals(request.queries().get("includeDisabled"));

        List<DomainInfo> domainInfos = this.getAllDomains(includeDisabled);

        sender.send(response);

        try(
            JsonGenerator responseContentGenerator = this.objectMapper.createGenerator(response.body().writer())
        ) {
            responseContentGenerator.writeStartObject();
            responseContentGenerator.writeObjectField("result", domainInfos);
            responseContentGenerator.writeEndObject();
        }
    }
    protected abstract List<DomainInfo> getAllDomains(boolean includeDisabled);

}
