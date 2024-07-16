package com.name.dex.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.name.dex.PowerDNSBackend;
import com.name.dex.dns.*;
import com.name.dex.dns.rr.GenericRecord;
import com.name.dex.utils.rest.api.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Test {

    public static void main(String[] args) throws Exception {
//        ApiClient client = new ApiClient("https://api.github.com/");
//
//        ApiRequest request = client.request(
//                ApiRequest.Method.GET,
//                "/repos/{owner}/{repo}/contents/{path}",
//                Map.of(
//                        "owner", "oneorg-cc",
//                        "repo", "oneorg.cc",
//                        "path", ""
//                )
//        );
//
//        System.out.println(request.uri());
//
//        ApiResponse repsonse = client.send(request).await();
//
//        StringBuilder bodyContent = new StringBuilder();
//
//        try(BufferedReader bodyReader = repsonse.body().reader()) {
//            String line;
//            int i = 0;
//            while((line = bodyReader.readLine()) != null) {
//                bodyContent.append(line);
//                ++i;
//            }
//        }

        //

//        ARecord Arr = new ARecord(new RecordData.Domain("test.oneorg.cc."), 300, RecordClass.IN, new ARecord.Data("127.0.0.1"));
//        ARecord Arr2 = new ARecord(new RecordData.Domain("test.oneorg.cc."), 300, RecordClass.IN, new ARecord.Data("127.0.0.1"));
//        CAARecord CAArr = new CAARecord(new RecordData.Domain("test.oneorg.cc."), 300, RecordClass.IN, new CAARecord.Data((byte) 0, "test", "123"));
//
//        ZoneConfiguration zone = new ZoneConfiguration("oneorg.cc.");
//        zone.setRecord(Arr);
//        zone.setRecord(CAArr);
//
//        //
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new Jdk8Module());
//        objectMapper.setDefaultPrettyPrinter(new MinimalPrettyPrinter());
//
//        //
//
//        System.out.println(
//            objectMapper.writeValueAsString(
//                objectMapper.readValue(
//                    objectMapper.writeValueAsString(
//                        RecordQuery.from(Arr, Optional.of(-1), Optional.empty(), Optional.of(true))
//                    ),
//                    RecordQuery.class
//                )
//            )
//        );
//
//        //
//
//        String json = objectMapper.writeValueAsString(zone);
//        System.out.println(json);
//
//        //
//
//        System.out.println( objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString( objectMapper.readValue(json, ZoneConfiguration.class) ) );
//
//        //
//
//        zone.setRecord(Arr2);
//        System.out.println(objectMapper.writeValueAsString(zone));

        //

        ZoneRegistry registry = new ZoneRegistry(new File("./data/registry/zones/"));

        PowerDNSBackend powerDNSBackend = new PowerDNSBackend(registry);
        powerDNSBackend.setRootPath("/powerdns");
        powerDNSBackend.enableLogging();

        RecordData.Domain testZoneOrigin = new RecordData.Domain("oneorg.cc");

        ApiRouter testRouter = new ApiRouter();
        testRouter.enableLogging();
        testRouter.registerRouteListener(new ApiRouteListener() {
            @ApiRouteHandler(
                    methods = ApiRequest.Method.GET,
                    endpoint = "/test"
            )
            private void testEndpoint(ApiRequest request, Map<String, String> parameters, ApiRoute.ResponseSender sender) throws ApiRouter.ResponseAlreadySentException, IOException, RecordData.Domain.InvalidFormatException {
                try {
                    ZoneConfiguration testZone = registry.getZone(testZoneOrigin);
                    if (testZone == null)
                        registry.setZone(new ZoneConfiguration(testZoneOrigin));

                    RecordData.Domain name = new RecordData.Domain(request.queries().get("name"));
                    int ttl = Integer.parseInt(request.queries().get("ttl"));
                    RecordType type = RecordType.valueOf(request.queries().get("type"));
                    GenericRecord.Data data = new GenericRecord.Data(new ObjectMapper().readValue(request.queries().get("data"), Map.class));

                    testZone.setRecord(new GenericRecord(name, ttl, type, data));
                    registry.setZone(testZone);
                } catch(Exception e) {
                    e.printStackTrace();
                }

                ApiResponse response = new ApiResponse();
                sender.send(response).await();

                try(BufferedWriter bodyWriter = response.body().writer()) {
                    bodyWriter.write("test");
                }
            }
        });

        ApiServer apiServer = new ApiServer();
        apiServer.registerRouter(powerDNSBackend);
        apiServer.registerRouter(testRouter);
        apiServer.bind(1130);
        apiServer.open();
    }

}
