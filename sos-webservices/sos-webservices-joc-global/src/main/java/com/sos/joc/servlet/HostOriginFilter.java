package com.sos.joc.servlet;

import java.io.IOException;
import java.net.URI;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class HostOriginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String originHeader = req.getHeader("Origin");
        String hostHeader = req.getHeader("Host");
        String forwardedHeader = req.getHeader("Forwarded");
        if(forwardedHeader == null) {
            forwardedHeader = req.getHeader("X-Forwarded-Host");
        }
        URI originUri = null;
        URI hostUri = null;
        URI forwardedUri = null;
        String host = "";
        Integer port = -1;
        if(originHeader != null && !originHeader.isEmpty()) {
            originUri = URI.create(originHeader);
        }
        if(hostHeader != null && !hostHeader.isEmpty()) {
            hostUri = URI.create(hostHeader);
        }
        if(forwardedHeader != null && !forwardedHeader.isEmpty()) {
            forwardedUri = URI.create(forwardedHeader);
        }
        if(hostUri != null) {
            if(hostUri.getHost() != null) {
                host = hostUri.getHost();
                port = hostUri.getPort();
            } else {
                // if Host Header is not an Uri hostUri.getHost() is null, use scheme && schemeSpecificPart instead
                if(hostUri.getScheme() != null) {
                    host = hostUri.getScheme();
                    if(hostUri.getSchemeSpecificPart() != null && !hostUri.getSchemeSpecificPart().isEmpty()) {
                        try {
                            port = Integer.parseInt(hostUri.getSchemeSpecificPart());
                        } catch (NumberFormatException e) {}
                    }
                } else {
                    if(hostUri.getSchemeSpecificPart() != null) {
                        host = hostUri.getSchemeSpecificPart();
                    }
                }
            }
        }
        // Validate that if Origin is present, it targets your trusted host environment
        if(forwardedUri != null) {
            if(forwardedUri != null && host != null && !forwardedUri.getHost().equals(host)) {
                System.out.println("Warning: Request rejected because mismatch occurred comparing Host Header: " + hostHeader + " against Forwarded Header: "
                        + forwardedHeader + ".");
                return;
            }
        } else {
            if(originUri != null && host != null && !originUri.getHost().equals(host)) {
                System.out.println("Warning: Request rejected because mismatch occurred comparing Host Header: " + hostHeader + " against Origin Header: "
                        + originHeader + ".");
                return;
            }
        }
        chain.doFilter(request, response);
    }
    
    private void resolveHostname() {
        
    }
}
