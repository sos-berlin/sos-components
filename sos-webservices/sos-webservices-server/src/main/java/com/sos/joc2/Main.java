package com.sos.joc2;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ServerProperties;

 
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    private static final String BASE_URI = "http://localhost:8090/";
    // init parameters
    private static final Map<String, String> initParams = Collections.unmodifiableMap(new HashMap<String, String>(3) {

        private static final long serialVersionUID = 1L;

        {
            put(ServerProperties.PROVIDER_PACKAGES, "com.sos.joc, com.sos.webservices.order, com.sos.auth.classes");
            put(ServerProperties.PROVIDER_CLASSNAMES, "org.glassfish.jersey.media.multipart.MultiPartFeature");
            put(ServerProperties.WADL_FEATURE_DISABLE, "true");
        }
    });
    

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     * @throws IOException 
     */
    public static HttpServer startServer() {
        
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        
        //return GrizzlyWebContainerFactory.create(URI.create(BASE_URI), JocServletContainer.class, initParams);
        
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI));
        
        // Initialize and register Jersey Servlet and Metrics
        WebappContext context = new WebappContext("JS7 JOC", "");
        
        ServletRegistration jocApi = context.addServlet("Jersey REST Service", JocServletContainer.class);
        jocApi.setInitParameters(initParams);
        jocApi.addMapping("/rest/*");
        jocApi.setLoadOnStartup(0);
        
//        ServletRegistration metrics = context.addServlet("Metrics Service", ProxyMetricsServlet.class);
//        metrics.addMapping("/metrics");
//        metrics.setLoadOnStartup(1);
        
        context.deploy(httpServer);
        
        return httpServer;
    }

    public static void setLogger() {
        Logger l = Logger.getLogger("org.glassfish.grizzly.http.server.HttpHandler");
        l.setLevel(Level.FINE);
        l.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        l.addHandler(ch);
    }
  
    public static void main(String[] args) throws IOException {
    	setLogger();
    	TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        final HttpServer server = startServer();
        //StaticHttpHandler staticHttpHandler = new StaticHttpHandler("src/main/resources");
        //server.getServerConfiguration().addHttpHandler(staticHttpHandler, "/");
        System.out.println("Hit enter to stop it...");
        System.in.read();
        server.shutdownNow();
    }
}


