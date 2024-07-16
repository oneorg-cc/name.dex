package com.name.dex.utils.rest.api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApiRequest {

    public enum Method {
        GET, HEAD, POST, PUT, DELETE, CONNECT, OPTIONS, TRACE, PATCH
    }

    //

    public static class Queries {

        public static Map<String, String> from(String uri) throws URISyntaxException {
            String query = ApiEndpoint.extractQuery(uri);
            return from(new URI("/" + (query.length() > 0 ? "?" + query : "")));
        }

        public static Map<String, String> from(URI uri) {
            Map<String, String> queries = new HashMap<>();

            String uriQuery = uri.getQuery();

            if(uriQuery != null) {
                String[] uriQuerySplit = uriQuery.split("&");

                for(String rawEntry : uriQuerySplit) {
                    String[] rawEntrySplit = rawEntry.split("=");

                    String key = rawEntrySplit[0], value = rawEntrySplit.length == 2 ? rawEntrySplit[1] : null;

                    queries.put(key, value);
                }
            }

            return queries;
        }

        //

        public static String serialize(Map<String, String> queries) {
            StringBuilder serializedBuilder = new StringBuilder();

            Set<Map.Entry<String, String>> entries = queries.entrySet();

            for(Map.Entry<String, String> entry : entries) {
                serializedBuilder.append(entry.getKey());

                if(entry.getValue() != null)
                    serializedBuilder.append("=").append(entry.getValue());

                serializedBuilder.append("&");
            }

            if(!entries.isEmpty())
                serializedBuilder.deleteCharAt(serializedBuilder.length()-1);

            return serializedBuilder.toString();
        }

    }

    //

    public static ApiRequest from(HttpExchange httpExchange) throws IOException, Body.InputAlreadyPipedWithBodyException, URISyntaxException {
        URI uri = httpExchange.getRequestURI();

        ApiRequest request = new ApiRequest(
            ApiRequest.Method.valueOf(httpExchange.getRequestMethod()),
            uri,
            Queries.from(uri),
            new ApiHeaders().setFrom(httpExchange.getRequestHeaders())
        );

        request.body().pipeFrom(httpExchange.getRequestBody());

        return request;
    }

    public static ApiRequest unmodifiable(ApiRequest request, Body.Usage usage) throws URISyntaxException {
        return new ApiRequest(
            request.method(),
            request.uri(),
            Collections.unmodifiableMap(request.queries()),
            ApiHeaders.unmodifiable(request.headers()),
            Body.wrap(request.body(), Set.of(usage))
        );
    }

    public static ApiRequest readOnlyBody(ApiRequest request) {
        return wrapBody(request, Set.of(Body.Usage.READABLE));
    }

    public static ApiRequest writeOnlyBody(ApiRequest request) {
        return wrapBody(request, Set.of(Body.Usage.WRITEABLE));
    }

    public static ApiRequest wrapBody(ApiRequest request, Set<Body.Usage> usages) {
        request.body = Body.wrap(request.body(), usages);
        return request;
    }

    //

    private final Method method;
    private final URI uri;
    private final Map<String, String> queries;
    private final ApiHeaders headers;
    private Body body;

    //

    public ApiRequest(Method method, String uri) throws IOException, URISyntaxException {
        this(method, new URI(uri));
    }

    public ApiRequest(Method method, URI uri) throws IOException, URISyntaxException {
        this(method, uri, Map.of());
    }

    public ApiRequest(Method method, String uri, Map<String, String> queries) throws IOException, URISyntaxException {
        this(method, new URI(uri), queries);
    }

    public ApiRequest(Method method, URI uri, Map<String, String> queries) throws IOException, URISyntaxException {
        this(method, uri, queries, new ApiHeaders());
    }

    public ApiRequest(Method method, String uri, ApiHeaders headers) throws IOException, URISyntaxException {
        this(method, new URI(uri), headers);
    }

    public ApiRequest(Method method, URI uri, ApiHeaders headers) throws IOException, URISyntaxException {
        this(method, uri, Map.of(), headers);
    }

    public ApiRequest(Method method, String uri, Map<String, String> queries, ApiHeaders headers) throws IOException, URISyntaxException {
        this(method, new URI(uri), queries, headers);
    }

    public ApiRequest(Method method, URI uri, Map<String, String> queries, ApiHeaders headers) throws IOException, URISyntaxException {
        this(method, uri, queries, headers, new Body());
    }

    private ApiRequest(Method method, String uri, Map<String, String> queries, ApiHeaders headers, Body body) throws URISyntaxException {
        this(method, new URI(uri), queries, headers, body);
    }

    private ApiRequest(Method method, URI uri, Map<String, String> queries, ApiHeaders headers, Body body) throws URISyntaxException {
        this.method = method;
        this.uri = queries.isEmpty() ? uri : new URI(
            uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
            Queries.serialize(queries), uri.getFragment()
        );
        this.queries = queries;

        this.headers = headers;

        this.body = body;
    }

    //

    public Method method() { return this.method; }

    public URI uri() { return this.uri; }

    public Map<String, String> queries() { return this.queries; }

    public ApiHeaders headers() { return this.headers; }

    public Body body() { return this.body; }

    //

    @Override
    public String toString() {
        return this.method() + " " + this.uri() + " queries=" + this.queries();
    }

}
