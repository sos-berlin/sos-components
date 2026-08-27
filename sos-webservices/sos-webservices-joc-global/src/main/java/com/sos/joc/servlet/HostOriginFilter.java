package com.sos.joc.servlet;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HostOriginFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String origin = req.getHeader("Origin");
        String host = req.getHeader("Host"); // Or use req.getServerName()

        // Validate that if Origin is present, it targets your trusted host environment
        if (origin != null && !origin.contains(host)) {
            System.out.println("Warning: Request rejected because mismatch occurred comparing Host Header " + host + " against Origin Header "
                    + origin + ".");
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Origin Mismatch");
            return;
        }
        chain.doFilter(request, response);
    }
}
