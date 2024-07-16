package com.name.dex.utils.rest.api;

import java.io.IOException;
import java.util.Set;

public class ApiResponse {

    public interface StatusCode {

        int value();

        //

        static StatusCode from(int code) {
            StatusCode result = null;
            StatusCode[][] categories = new StatusCode[][] { INFORMATION.values(), SUCCESS.values(), REDIRECTION.values(), CLIENT_ERROR.values(), SERVER_ERROR.values() };

            boolean found = false;

            for(int categoryIndex = 0; categoryIndex < categories.length && !found; categoryIndex++) {
                StatusCode[] category = categories[categoryIndex];

                for(int statusCodeIndex = 0; statusCodeIndex < category.length && !found; statusCodeIndex++) {
                    result = category[statusCodeIndex];
                    found = result.value() == code;
                }
            }

            return found ? result : null;
        }

        //

        static boolean is(int code, Class<?> statusCodeClass) {
            return is(from(code), statusCodeClass);
        }
        static boolean is(StatusCode code, Class<?> statusCodeClass) {
            return statusCodeClass.equals(code.getClass());
        }

        //

        enum INFORMATION implements StatusCode {

            CONTINUE(100),
            SWITCHING_PROTOCOLS(101),
            PROCESSING(102),
            EARLY_HINTS(103);

            private final int value;
            INFORMATION(int value) { this.value = value; }
            public int value() { return this.value; }

        }

        //

        enum SUCCESS implements StatusCode {

            OK(200),
            CREATED(201),
            ACCEPTED(202),
            NON_AUTHORITATIVE_INFORMATION(203),
            NO_CONTENT(204),
            RESET_CONTENT(205),
            PARTIAL_CONTENT(206),
            MULTI_STATUS(207),
            ALREADY_REPORTED(208),

            CONTENT_DIFFERENT(210),

            IM_USED(226);

            private final int value;
            SUCCESS(int value) { this.value = value; }
            public int value() { return this.value; }

        }

        // 

        enum REDIRECTION implements StatusCode {

            MULTPILE_CHOICES(300),
            MOVED_PERMANENTLY(301),
            FOUND(302),
            SEE_OTHER(303),
            NOT_MODIFIED(304),
            USE_PROXY(305),
            USELESS(306),
            TEMPORARY_REDIRECT(307),
            PERMANENT_REDIRECT(308),

            TOO_MANY_REDIRECTS(310);

            private final int value;
            REDIRECTION(int value) { this.value = value; }
            public int value() { return this.value; }

        }

        // 

        enum CLIENT_ERROR implements StatusCode {
            BAD_REQUEST(400),
            UNAUTHORIZED(401),
            PAYMENT_REQUIRED(402),
            FORBIDDEN(403),
            NOT_FOUND(404),
            METHOD_NOT_ALLOWED(405),
            NOT_ACCEPTABLE(406),
            PROXY_AUTHENTICATION_REQUIRED(407),
            REQUEST_TIMEOUT(408),
            CONFLICT(409),
            GONE(410),
            LENGTH_REQUIRED(411),
            PRECONDITION_FAILED(412),
            REQUEST_ENTITY_TOO_LARGE(413),
            REQUEST_URI_TOO_LONG(414),
            UNSUPPORTED_MEDIA_TYPE(415),
            REQUESTED_RANGE_UNSATISFIABLE(416),
            EXPECTATION_FAILED(417),
            IM_A_TEAPOT(418),
            PAGE_EXPIRED(419),

            MISDIRECTED_REQUEST(421),
            UNPROCESSABLE_ENTITY(422),
            LOCKED(423),
            METHOD_FAILURE(424),
            TOO_EARLY(425),
            UPGRADE_REQUIRED(426),
            PRECONDITION_REQUIRED(428),
            TOO_MANY_REQUESTS(429),

            REQUEST_HEADER_FIELDS_TOO_LARGE(431),

            RETRY_WITH(449),
            BLOCKED_BY_WINDOWS_PARENTAL_CONTROLS(450),
            UNAVAILABLE_FOR_LEGAL_REASONS(451),

            UNRECOVERABLE_ERROR(456);

            private final int value;
            CLIENT_ERROR(int value) { this.value = value; }
            public int value() { return this.value; }

        }

        // 

        enum SERVER_ERROR implements StatusCode {
            INTERNAL_SERVER_ERROR(500),
            NOT_IMPLEMENTED(501),
            BAD_GATEWAY(502),
            SERVICE_UNAVAILABLE(503),
            GATEWAY_TIMEOUT(504),
            HTTP_VERSION_NOT_SUPPORTED(505),
            VARIANT_ALSO_NEGOTIATES(506),
            INSUFFICIENT_STORAGE(507),
            LOOP_DETECTED(508),
            BANDWIDTH_LIMIT_EXCEEDED(509),
            NOT_EXTENDED(510),
            NETWORK_AUTHENTICATION_REQUIRED(511),

            UNKNOWN_ERROR(520),
            WEB_SERVER_IS_DOWN(521),
            CONNECTION_TIMED_OUT(522),
            ORIGIN_IS_UNREACHABLE(523),
            A_TIMEOUT_OCCURRED(524),
            SSL_HANDSHAKE_FAILED(525),
            INVALID_SSL_CERTIFICATE(526),
            RAILGUN_ERROR(527);

            private final int value;
            SERVER_ERROR(int value) { this.value = value; }
            public int value() { return this.value; }

        }
    }

    //

    public static ApiResponse unmodifiable(ApiResponse response, Body.Usage usage) throws IOException {
        return new ApiResponse(
            response.code(),
            response.status(),
            ApiHeaders.unmodifiable(response.headers()),
            Body.wrap(response.body(), Set.of(usage)),
            response.chunked()
        );
    }

    public static ApiResponse readOnlyBody(ApiResponse response) {
        return wrapBody(response, Set.of(Body.Usage.READABLE));
    }

    public static ApiResponse writeOnlyBody(ApiResponse response) {
        return wrapBody(response, Set.of(Body.Usage.WRITEABLE));
    }

    public static ApiResponse wrapBody(ApiResponse response, Set<Body.Usage> usages) {
        response.body = Body.wrap(response.body(), usages);
        return response;
    }

    //

    private final StatusCode code;
    private final String status;
    private final ApiHeaders headers;

    private Body body;

    private final boolean chunked;

    //

    public ApiResponse() throws IOException {
        this(StatusCode.SUCCESS.OK, false);
    }

    public ApiResponse(boolean chunked) throws IOException {
        this(StatusCode.SUCCESS.OK, chunked);
    }

    public ApiResponse(StatusCode code) throws IOException {
        this(code, "", false);
    }

    public ApiResponse(StatusCode code, boolean chunked) throws IOException {
        this(code, "", chunked);
    }

    public ApiResponse(StatusCode code, String status) throws IOException {
        this(code, status, false);
    }

    public ApiResponse(StatusCode code, String status, boolean chunked) throws IOException {
        this(code, status, new ApiHeaders(), new Body(), chunked);
    }

    private ApiResponse(StatusCode code, String status, ApiHeaders headers) throws IOException {
        this(code, status, headers, false);
    }

    private ApiResponse(StatusCode code, String status, ApiHeaders headers, boolean chunked) throws IOException {
        this(code, status, headers, new Body(), chunked);
    }

    private ApiResponse(StatusCode code, String status, ApiHeaders headers, Body body) throws IOException {
        this(code, status, headers, body, false);
    }

    private ApiResponse(StatusCode code, String status, ApiHeaders headers, Body body, boolean chunked) throws IOException {
        this.code = code;
        this.status = status;
        this.headers = headers;

        this.body = body;

        this.chunked = chunked;
    }

    //

    public StatusCode code() { return this.code; }
    public String status() { return this.status(); }
    public ApiHeaders headers() { return this.headers; }

    public Body body() { return this.body; }

    public boolean chunked() { return this.chunked; }

}
