package com.sos.joc2;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.server.ServerProperties;

 
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    private static final String BASE_URI = "http://localhost:8090/rest/";
    

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     * @throws IOException 
     */
    public static HttpServer startServer() throws IOException {
        
        Map<String, String> initParams = new HashMap<>();
        initParams.put(ServerProperties.PROVIDER_PACKAGES, "com.sos.joc, com.sos.webservices.order, com.sos.auth.classes");
        initParams.put(ServerProperties.PROVIDER_CLASSNAMES, "org.glassfish.jersey.media.multipart.MultiPartFeature");

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        
        HttpServer httpServer = GrizzlyWebContainerFactory.create(URI.create(BASE_URI), JocServletContainer.class, initParams);
        return httpServer;
    }

    public  static void setLogger() {
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
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.shutdownNow();
    }
}


