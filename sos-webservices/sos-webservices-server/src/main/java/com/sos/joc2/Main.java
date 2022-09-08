package com.sos.joc2;

import java.io.IOException;
import java.net.URI;
import java.util.TimeZone;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.servlet.ServletRegistration;

 
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    private static final String BASE_URI = "http://localhost:8090/rest/";
    

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.sos.webservices package
        final ResourceConfig rc = new ResourceConfig().packages("com.sos.webservices", "com.sos.joc", "com.sos.auth").register(
                MultiPartFeature.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        WebappContext context = new WebappContext("WebappContext", "");
        ServletRegistration registration = context.addServlet("ServletContainer", new JocServletContainer());
        registration.addMapping("/*");
        context.deploy(httpServer);
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


