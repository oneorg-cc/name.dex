package com.name.dex.utils.rest.api;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiServer {

    public static class RouterAlreadyRegisteredException extends Exception {

        public RouterAlreadyRegisteredException(String message) { super(message); }

    }

    public static class RouterNotRegisteredException extends Exception {

        public RouterNotRegisteredException(String message) { super(message); }

    }

    //

    private final HttpServer httpServer;

    private List<ApiRouter> routers = new ArrayList<>();

    //

    public ApiServer() throws IOException {
        this.httpServer = HttpServer.create();
        this.httpServer.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());

        httpServer.createContext("/", (httpExchange) -> {
            try {
                ApiRequest request = ApiRequest.unmodifiable(ApiRequest.from(httpExchange), Body.Usage.READABLE);
                ApiResponse response = null;

                //

                List<ApiRouter> routers = this.getRouters();
                for(int i = 0; i < routers.size() && response == null; i++) {
                    ApiRouter router = routers.get(i);
                    response = router.route(request);
                }

                //

                if(response == null) {

                }
                else {
                    try(
                        InputStream responseBodyIn = response.body().in()
                    ) {
                        httpExchange.getResponseHeaders().putAll(response.headers().map());
                        httpExchange.sendResponseHeaders(
                            response.code().value(),
                            response.chunked() ? 0 : responseBodyIn.available()
                        );

                        //

                        Body responseBody = response.body();

                        if(responseBody.locked()) {
                            responseBody.unlock();
                        }
                        responseBody.pipeSyncInto(httpExchange.getResponseBody());
                        httpExchange.close();
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    //

    public List<ApiRouter> getRouters() { return Collections.unmodifiableList(this.routers); }

    public void registerRouter(ApiRouter router) throws RouterAlreadyRegisteredException {
        if(this.routers.contains(router)) {
            throw new RouterAlreadyRegisteredException("This router is already registered.");
        }

        this.routers.add(router);
    }

    public void unregisterRouter(ApiRouter router) throws RouterNotRegisteredException {
        if(!this.routers.contains(router)) {
            throw new RouterNotRegisteredException("This router isn't registered.");
        }

        this.routers.remove(router);
    }

    //

    public void bind(int port) throws IOException {
        bind("0.0.0.0", port);
    }

    public void bind(String address, int port) throws IOException {
        bind(new InetSocketAddress(address, port));
    }

    public void bind(InetSocketAddress address) throws IOException {
        this.httpServer.bind(address, 0);
    }

    //

    public void open(int port) throws IOException {
        bind(port);
        open();
    }

    public void open(String address, int port) throws IOException {
        bind(address, port);
        open();
    }

    public void open(InetSocketAddress address) throws IOException {
        bind(address);
        open();
    }

    public void open() {
        this.httpServer.start();
    }

    private void close() {
        this.httpServer.stop(0);
    }

}
