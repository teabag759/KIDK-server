package com.kidk.api.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final int MAX_LOG_BODY_LENGTH = 2000;
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "refresh-token");
    private static final List<Pattern> SENSITIVE_JSON_PATTERNS = List.of(
            Pattern.compile("(\"password\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\"token\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\"accessToken\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\"refreshToken\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\"firebaseToken\"\\s*:\\s*\")[^\"]*(\")", Pattern.CASE_INSENSITIVE)
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long durationMs = System.currentTimeMillis() - start;

            String requestBody = readBody(requestWrapper.getContentAsByteArray(), requestWrapper.getContentType());
            String responseBody = readBody(responseWrapper.getContentAsByteArray(), responseWrapper.getContentType());

            requestBody = maskSensitive(requestBody);
            responseBody = maskSensitive(responseBody);

            Map<String, String> headers = extractHeaders(requestWrapper);

            log.info("HTTP {} {} status={} durationMs={} ip={} ua=\"{}\" headers={} reqBody={} resBody={}",
                    requestWrapper.getMethod(),
                    requestWrapper.getRequestURI(),
                    responseWrapper.getStatus(),
                    durationMs,
                    requestWrapper.getRemoteAddr(),
                    Optional.ofNullable(requestWrapper.getHeader("User-Agent")).orElse("-"),
                    headers,
                    requestBody,
                    responseBody);

            responseWrapper.copyBodyToResponse();
        }
    }

    private String readBody(byte[] bytes, String contentType) {
        if (bytes == null || bytes.length == 0) {
            return "-";
        }
        if (contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/")) {
            return "[multipart]";
        }
        String body = new String(bytes, StandardCharsets.UTF_8);
        if (body.length() > MAX_LOG_BODY_LENGTH) {
            return body.substring(0, MAX_LOG_BODY_LENGTH) + "...(truncated)";
        }
        return body;
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getHeader(name);
            if (SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                headers.put(name, "***");
            } else {
                headers.put(name, value);
            }
        }
        return headers;
    }

    private String maskSensitive(String input) {
        if (input == null || input.equals("-")) {
            return input;
        }
        String masked = input;
        for (Pattern pattern : SENSITIVE_JSON_PATTERNS) {
            masked = pattern.matcher(masked).replaceAll("$1***$2");
        }
        return masked;
    }
}
