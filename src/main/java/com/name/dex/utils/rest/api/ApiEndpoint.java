package com.name.dex.utils.rest.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiEndpoint {

    public static String extractPath(String uri) throws URISyntaxException {
        String[] uriSplit = uri.split("\\?");
        String p = uriSplit[0].replaceAll("\\{", "%7B").replaceAll("}", "%7D");
        return new URI(p).getPath();
    }

    public static String extractQuery(String uri) throws URISyntaxException {
        String[] uriSplit = uri.split("\\?");
        return uriSplit.length > 1 ? uriSplit[1] : "";
    }

    //

    public static class URINotMatchingException extends Exception {}

    //

    private final String uri;

    private final Pattern pathPattern;

    private final List<String> pathParameterKeys;

    //

    public ApiEndpoint(String uri) throws URISyntaxException {
        this.uri = uri;

        //

        List<String> parameterKeys = new ArrayList<>();

        String parsedUriPath = ApiEndpoint.extractPath(this.uri()).replaceAll("/", "\\\\/");

//        if(parsedUri.length() > 1 && parsedUri.endsWith("/"))
//            parsedUri = parsedUri.substring(0, parsedUri.length()-1);

        Matcher endpointParameterKeysMatcher = Pattern.compile("\\{([^}]+)}").matcher(parsedUriPath);

        while(endpointParameterKeysMatcher.find())
            parameterKeys.add(parsedUriPath.substring(endpointParameterKeysMatcher.start()+1, endpointParameterKeysMatcher.end()-1));

        this.pathParameterKeys = Collections.unmodifiableList(parameterKeys);

        //

        this.pathPattern = Pattern.compile("^" + endpointParameterKeysMatcher.replaceAll("([^/]+|)") + "$");
    }

    //

    public String uri() { return this.uri; }

    public Pattern pathPattern() { return this.pathPattern; }

    public List<String> getPathParameterKeys() { return this.pathParameterKeys; }

    //

    public boolean isPathMatching(String uri) throws URISyntaxException {
        return this.isPathMatching(new URI(uri));
    }

    public boolean isPathMatching(URI uri) {
        return this.pathPattern().matcher(uri.getPath()).matches();
    }

    //

    public Map<String, String> extractPathParameterValues(String uri) throws URINotMatchingException, URISyntaxException {
        return this.extractPathParameterValues(new URI(uri));
    }

    public Map<String, String> extractPathParameterValues(URI uri) throws URINotMatchingException {
        if(!this.isPathMatching(uri))
            throw new URINotMatchingException();

        Map<String, String> pathParameters = new HashMap<>();

        List<String> pathParameterKeys = this.getPathParameterKeys();
        int pathParameterCount = pathParameterKeys.size();

        Matcher pathParameterValuesMatcher = this.pathPattern().matcher(uri.getPath());

        if(pathParameterValuesMatcher.find()) {
            for(int pathParameterIndex = 0; pathParameterIndex < pathParameterCount; pathParameterIndex++) {
                String key = pathParameterKeys.get(pathParameterIndex);
                String value = pathParameterValuesMatcher.group(pathParameterIndex + 1);
                pathParameters.put(key, value);
            }
        }

        return Collections.unmodifiableMap(pathParameters);
    }

    //

    public String parse(String root, Map<String, String> pathParameters) {
        String parsed = this.uri;

        for(Map.Entry<String, String> pathParameter : pathParameters.entrySet())
            parsed = parsed.replaceAll("\\{" + pathParameter.getKey() + "}", pathParameter.getValue());

        return root + (root.endsWith("/") ? parsed.substring(1) : parsed);
    }

}
