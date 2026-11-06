package com.sos.joc.servlet;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class HostOriginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        try {
            HttpServletRequest req = (HttpServletRequest) request;
            
            Optional<String> originHeader = Optional.ofNullable(req.getHeader("Origin")).filter(s -> !s.isBlank());
            
            if (originHeader.isPresent()) {
                
                Optional<String> hostHeader = Optional.ofNullable(req.getHeader("Host")).filter(s -> !s.isBlank());
                Optional<String> forwardedHeader = Optional.ofNullable(req.getHeader("Forwarded")).filter(s -> !s.isBlank());

                Optional<String> hostHost = hostHeader.map(s -> s.replace('_', '-')).map(URI::create).map(this::resolveHostname);
                Optional<String> originHost = originHeader.map(s -> s.replace('_', '-')).map(URI::create).map(this::resolveHostname);
                Optional<String> forwardedHost = forwardedHeader.flatMap(this::extractForwardedHostname).map(s -> s.replace('_', '-')).map(URI::create)
                        .map(this::resolveHostname);
                if (forwardedHost.isEmpty()) {
                    forwardedHeader = Optional.ofNullable(req.getHeader("X-Forwarded-Host")).filter(s -> !s.isBlank());
                    forwardedHost = forwardedHeader.map(s -> s.replace('_', '-')).map(URI::create).map(this::resolveHostname);
                }
                // Validate that if Origin is present, it targets your trusted host environment
                if (originHost.isPresent()) {
                    if (forwardedHost.isPresent()) {
                        if (!originHost.equals(forwardedHost)) {
                            System.out.println("Warning: Request rejected because mismatch occurred comparing Origin Header: " + originHeader.orElse("")
                                    + " against Forwarded Header: " + forwardedHeader.orElse("") + ".");
                            return;
                        }
                    } else {
                        if (hostHost.isPresent() && !originHost.equals(hostHost)) {
                            System.out.println("Warning: Request rejected because mismatch occurred comparing Host Header: " + hostHeader.orElse("")
                                    + " against Origin Header: " + originHeader.orElse("") + ".");
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        chain.doFilter(request, response);
    }

    private Optional<String> extractForwardedHostname(String forwarded) {
        return Arrays.asList(forwarded.split(";")).stream().filter(part -> part.startsWith("host=")).findAny().map(host -> host.substring(5)
                .replaceAll("\"", ""));
    }

    private String resolveHostname(URI uri) {
        if (uri.getHost() != null) {
            return uri.getHost();
        } else if (uri.getScheme() != null) {
            return uri.getScheme();
        } else {
            return uri.getSchemeSpecificPart();
        }
    }
}
