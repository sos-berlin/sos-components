package com.sos.js7.joc.poc.jetty.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

import com.sos.joc2.poc.jetty.servlets.BlockingServletExample;
import com.sos.js7.joc.poc.jetty.servlets.AsyncServletExample;

public class JettyTestServer {

    private Server server;

    public void start() throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(8010);
        server.setConnectors(new Connector[] {connector});
        
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(BlockingServletExample.class, "/status");
        servletHandler.addServletWithMapping(AsyncServletExample.class, "/heavy/async");
        server.setHandler(servletHandler);
        
        server.start();
    }
    
    public void stop() throws Exception {
        server.stop();
    }
    
}

/*
 Server server = new Server();

 */