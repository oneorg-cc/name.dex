package com.name.dex.utils.rest.api;

import com.name.dex.utils.Promise;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

public class ApiRoute {

    public interface ResponseSender {

        Promise<Boolean> send(ApiResponse response) throws ApiRouter.ResponseAlreadySentException;

    }

    //

    public interface Handler {

        void handle(ApiRequest request, Map<String, String> parameters, ResponseSender sender) throws ApiRouter.ResponseAlreadySentException, IOException;

    }

    //

    private final Set<ApiRequest.Method> methods;
    private final ApiEndpoint endpoint;
    private final Handler handler;

    //

    public ApiRoute(Set<ApiRequest.Method> methods, String endpoint, Handler handler) throws URISyntaxException {
        this.methods = methods;
        this.endpoint = new ApiEndpoint(endpoint);
        this.handler = handler;
    }

    public ApiRoute(Set<ApiRequest.Method> methods, ApiEndpoint endpoint, Handler handler) {
        this.methods = methods;
        this.endpoint = endpoint;
        this.handler = handler;
    }

    //

    public Set<ApiRequest.Method> methods() { return this.methods; }
    public ApiEndpoint endpoint() { return this.endpoint; }
    public Handler handler() { return this.handler; }

}
