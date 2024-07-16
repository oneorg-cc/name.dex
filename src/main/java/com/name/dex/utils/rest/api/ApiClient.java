package com.name.dex.utils.rest.api;

import com.name.dex.utils.Promise;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ApiClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private String root;

    //

    public ApiClient(String root) {
        this.root = root;
    }

    //

    public String getRoot() { return this.root; }
    public void setRoot(String root) { this.root = root; }

    //

    public ApiRequest request(ApiRequest.Method method, String endpoint, Map<String, String> parameters) throws URISyntaxException, IOException {
        return this.request(method, new ApiEndpoint(endpoint), parameters);
    }

    public ApiRequest request(ApiRequest.Method method, ApiEndpoint endpoint, Map<String, String> parameters) throws IOException, URISyntaxException {
        return this.request(method, endpoint, parameters, Map.of());
    }

    public ApiRequest request(ApiRequest.Method method, String endpoint, Map<String, String> parameters, Map<String, String> queries) throws URISyntaxException, IOException {
        return this.request(method, new ApiEndpoint(endpoint), parameters, queries);
    }

    public ApiRequest request(ApiRequest.Method method, ApiEndpoint endpoint, Map<String, String> parameters, Map<String, String> queries) throws IOException, URISyntaxException {
        return this.request(method, endpoint, parameters, queries, new ApiHeaders());
    }

    public ApiRequest request(ApiRequest.Method method, String endpoint, Map<String, String> parameters, ApiHeaders headers) throws URISyntaxException, IOException {
        return this.request(method, new ApiEndpoint(endpoint), parameters, headers);
    }

    public ApiRequest request(ApiRequest.Method method, ApiEndpoint endpoint, Map<String, String> parameters, ApiHeaders headers) throws IOException, URISyntaxException {
        return this.request(method, endpoint, parameters, Map.of(), headers);
    }

    public ApiRequest request(ApiRequest.Method method, String endpoint, Map<String, String> parameters, Map<String, String> queries, ApiHeaders headers) throws IOException, URISyntaxException {
        return this.request(method, new ApiEndpoint(endpoint), parameters, queries, headers);
    }

    public ApiRequest request(ApiRequest.Method method, ApiEndpoint endpoint, Map<String, String> parameters, Map<String, String> queries, ApiHeaders headers) throws IOException, URISyntaxException {
        return ApiRequest.writeOnlyBody(new ApiRequest(method, endpoint.parse(this.getRoot(), parameters), queries, headers));
    }

    //

    public Promise<ApiResponse> send(ApiRequest request) {
        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();

        HttpRequest.BodyPublisher bodyPublisher = (
            Set.of(ApiRequest.Method.POST, ApiRequest.Method.PUT, ApiRequest.Method.PATCH).contains(request.method())
            ? HttpRequest.BodyPublishers.ofInputStream(() -> request.body().in())
            : HttpRequest.BodyPublishers.noBody()
        );
        httpRequestBuilder.method(request.method().name(), bodyPublisher);
        httpRequestBuilder.uri(request.uri());

        for(Map.Entry<String, List<String>> header : request.headers().map().entrySet()) {
            String key = header.getKey();

            for(String value : header.getValue())
                httpRequestBuilder.header(key, value);
        }

        HttpRequest httpRequest = httpRequestBuilder.build();

        return new Promise<>((resolver, rejector) -> {
            HttpResponse<InputStream> httpResponse = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

            try {
                Optional<String> transferEncodingHeaderValue = httpResponse.headers().firstValue(ApiHeader.Response.TRANSFER_ENCODING.key());
                boolean isChunkedResposne = transferEncodingHeaderValue.isPresent() && transferEncodingHeaderValue.get().equals("chunked");

                ApiResponse response = new ApiResponse(ApiResponse.StatusCode.from(httpResponse.statusCode()), isChunkedResposne);

                response.headers().setFrom(httpResponse.headers().map());
                response.body().pipeFrom(httpResponse.body());

                resolver.resolve(response);
            } catch (IOException | Body.InputAlreadyPipedWithBodyException e) {
                rejector.reject(e);
            }
        });
    }

}
