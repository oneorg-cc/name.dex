package com.name.dex.utils.rest.api;

import com.name.dex.utils.Promise;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;

public class ApiRouter {

    public static class RouteAlreadyRegisteredException extends Exception {

        public RouteAlreadyRegisteredException(String message) { super(message); }

    }

    public static class RouteNotRegisteredException extends Exception {

        public RouteNotRegisteredException(String message) { super(message); }

    }

    //

    public static class RouteListenerAlreadyRegisteredException extends Exception {}

    public static class RouteListenerNotRegisteredException extends Exception {}

    //

    public static class ResponseAlreadySentException extends Exception {}

    //

    public interface RequestMapper {

        ApiRequest map(ApiRequest response) throws IOException, URISyntaxException;

    }

    public interface ResponseMapper {

        ApiResponse map(ApiResponse response);

    }

    //

    private final Map<ApiRouteListener, List<ApiRoute>> listenersRoutes = new HashMap<>();

    private RequestMapper requestMapper;
    private ResponseMapper responseMapper;

    private boolean loggingEnabled = false;

    private final ApiRouteListener internalRouteListener = new ApiRouteListener() {};

    //

    public ApiRouter() {
        this.listenersRoutes.put(this.internalRouteListener, new ArrayList<>());
    }

    //

    public RequestMapper getRequestMapper() { return this.requestMapper; }
    public void setRequestMapper(RequestMapper requestMapper) { this.requestMapper = requestMapper; }

    public ResponseMapper getResponseMapper() { return this.responseMapper; }
    public void setResponseMapper(ResponseMapper responseMapper) { this.responseMapper = responseMapper; }

    public void enableLogging() { this.loggingEnabled = true; }
    public void disableLogging() { this.loggingEnabled = false; }
    public boolean isLoggingEnabled() { return this.loggingEnabled; }

    //

    public void registerRoute(ApiRoute route) throws RouteAlreadyRegisteredException {
        if(route == null) return;

        if(this.listenersRoutes.get(this.internalRouteListener).contains(route))
            throw new RouteAlreadyRegisteredException("A route with the same endpoint already exists.");

        this.listenersRoutes.get(this.internalRouteListener).add(route);
    }

    public void unregisterRoute(ApiRoute route) throws RouteNotRegisteredException {
        if(route == null) return;

        if(!this.listenersRoutes.get(this.internalRouteListener).contains(route))
            throw new RouteNotRegisteredException("No route with this endpoint were found.");

        this.listenersRoutes.get(this.internalRouteListener).remove(route);
    }

    //

    public void registerRouteListener(ApiRouteListener listener) throws RouteListenerAlreadyRegisteredException, URISyntaxException {
        if(this.listenersRoutes.containsKey(listener))
            throw new RouteListenerAlreadyRegisteredException();

        List<ApiRoute> listenerRoutes = new ArrayList<>();

        List<Method> handlerMethods = new ArrayList<>();

        Class<?> apiRouteListenerClass = listener.getClass();
        while(!Set.of(apiRouteListenerClass.getInterfaces()).contains(ApiRouteListener.class) && !Object.class.equals(apiRouteListenerClass)) {
            apiRouteListenerClass = apiRouteListenerClass.getSuperclass();
        }

        for(Method handlerMehtod : apiRouteListenerClass.getMethods()) {
            handlerMethods.add(handlerMehtod);
        }
        for(Method handlerMehtod : apiRouteListenerClass.getDeclaredMethods()) {
            if(!handlerMethods.contains(handlerMehtod)) {
                handlerMethods.add(handlerMehtod);
            }
        }

        for(Method handlerMethod : handlerMethods) {
            ApiRouteHandler annotation = handlerMethod.getAnnotation(ApiRouteHandler.class);

            Class<?>[] parameterTypes = handlerMethod.getParameterTypes();

            boolean isValidHandler = (
                handlerMethod.getParameterCount() == 3
                && parameterTypes[0].equals(ApiRequest.class)
                && parameterTypes[1].equals(Map.class)
                && parameterTypes[2].equals(ApiRoute.ResponseSender.class)
            );

            if(isValidHandler) {
                handlerMethod.setAccessible(true);

                ApiRoute route = new ApiRoute(Set.of(annotation.methods()), annotation.endpoint(), (request, parameters, sender) -> {
                    try {
                        handlerMethod.invoke(listener, request, parameters, sender);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });

                listenerRoutes.add(route);
            }
        }

        this.listenersRoutes.put(listener, listenerRoutes);
    }

    public void unregisterRouteListener(ApiRouteListener listener) throws RouteListenerNotRegisteredException {
        if(!this.listenersRoutes.containsKey(listener))
            throw new RouteListenerNotRegisteredException();

        this.listenersRoutes.remove(listener);
    }

    //

    public ApiResponse route(ApiRequest request) throws URISyntaxException, IOException {
        return this.route(request, this.isLoggingEnabled());
    }

    public ApiResponse route(ApiRequest request, boolean logging) throws URISyntaxException, IOException {
        StringBuilder requestLogMessageBuilder = logging ? new StringBuilder() : null;

        RequestMapper requestMapper = this.getRequestMapper();
        final ApiRequest finalRequest = requestMapper == null ? request : requestMapper.map(request);

        ApiResponse response = null;

        if(finalRequest != null) {
            String uriPath = finalRequest.uri().getPath();

            boolean shouldTry = true;

            while(shouldTry) {
                ApiRoute route = null;
                boolean routeFound = false;

                for (Map.Entry<ApiRouteListener, List<ApiRoute>> entry : this.listenersRoutes.entrySet()) {
                    List<ApiRoute> routes = entry.getValue();

                    int routeCount = routes.size();

                    for (int routeIndex = 0; routeIndex < routeCount && !routeFound; routeIndex++) {
                        route = routes.get(routeIndex);
                        routeFound = route.methods().contains(finalRequest.method()) && route.endpoint().isPathMatching(uriPath);
                    }

                    if (routeFound) break;
                }

                if(logging) requestLogMessageBuilder.append(finalRequest.toString());

                if(routeFound) {
                    try {
                        Map<String, String> parameters = route.endpoint().extractPathParameterValues(uriPath);

                        if(logging) {
                            requestLogMessageBuilder.append(" parameters=" + parameters);
                            System.out.println(requestLogMessageBuilder.toString());
                        }

                        ApiRoute currentRoute = route;
                        response = new Promise<ApiResponse>((resolver, rejector) -> {
                            currentRoute.handler().handle(finalRequest, parameters, senderResponse -> {
                                try {
                                    senderResponse.body().lock();
                                } catch (Body.AlreadyLockedException e) {}

                                resolver.resolve(senderResponse);

                                return new Promise<>((backResolver, backRejector) -> {
                                    senderResponse.body().waitUnlock();
                                    backResolver.resolve(true);
                                });
                            });
                        }).await();
                    } catch (ApiEndpoint.URINotMatchingException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (
                        !routeFound
                                && (uriPath.length() > 1 && uriPath.endsWith("/"))
                ) {
                    uriPath = uriPath.substring(0, uriPath.length() - 1);
                } else
                    shouldTry = false;
            }
        }

        ResponseMapper responseMapper = this.getResponseMapper();
        return responseMapper == null ? response : responseMapper.map(response);
    }

}
