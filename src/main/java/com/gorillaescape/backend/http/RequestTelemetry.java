package com.gorillaescape.backend.http;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class RequestTelemetry extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(RequestTelemetry.class);
    private static final int MAX_BODY = 4096;

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        long start = System.nanoTime();
        var wrapped = new ContentCachingResponseWrapper(response);
        MDC.put("requestId", requestId);
        try {
            HttpServletRequest bounded = request;
            if ("POST".equals(request.getMethod()) && "/api/players".equals(request.getRequestURI())) {
                byte[] bytes = request.getInputStream().readNBytes(MAX_BODY + 1);
                if (bytes.length > MAX_BODY) {
                    wrapped.setStatus(413);
                    wrapped.setContentType("application/problem+json");
                    wrapped.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    wrapped.getWriter().write("{\"type\":\"urn:gorilla:problem:body-too-large\",\"title\":\"Payload Too Large\",\"status\":413,\"detail\":\"Máximo 4096 bytes.\",\"code\":\"BODY_TOO_LARGE\",\"requestId\":\"" + requestId + "\"}");
                    return;
                }
                bounded = new HttpServletRequestWrapper(request) {
                    @Override public ServletInputStream getInputStream() {
                        var input = new ByteArrayInputStream(bytes);
                        return new ServletInputStream() {
                            @Override public int read() { return input.read(); }
                            @Override public int read(byte[] b, int off, int len) { return input.read(b, off, len); }
                            @Override public boolean isFinished() { return input.available() == 0; }
                            @Override public boolean isReady() { return true; }
                            @Override public void setReadListener(ReadListener listener) { throw new UnsupportedOperationException("SYNCHRONOUS_API_ONLY"); }
                        };
                    }
                };
            }
            chain.doFilter(bounded, wrapped);
        } finally {
            double duration = (System.nanoTime() - start) / 1_000_000.0;
            wrapped.setHeader("X-Request-Id", requestId);
            wrapped.setHeader("Server-Timing", String.format(Locale.ROOT, "app;dur=%.3f", duration));
            wrapped.setHeader("Cache-Control", "no-store");
            Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            LOG.info("event=http_request requestId={} method={} route={} status={} durationMs={} jvmHeapUsedBytes={}",
                    requestId, request.getMethod(), route == null ? "unmapped" : route, wrapped.getStatus(),
                    String.format(Locale.ROOT, "%.3f", duration), ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
            MDC.remove("requestId");
            wrapped.copyBodyToResponse();
        }
    }
}
