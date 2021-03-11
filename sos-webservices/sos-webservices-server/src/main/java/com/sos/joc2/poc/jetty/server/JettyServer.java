package com.sos.joc2.poc.jetty.server;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.sos.joc2.poc.jetty.servlets.BlockingServletExample;


public class JettyServer {

    private static final Integer HTTP_PORT = 8900;
    private static final Integer CLIENT_AUTH_HTTPS_PORT = 8910;
    private static final Integer NO_AUTH_HTTPS_PORT = 8920;
    private Server server;
    
    public void init(String keyStorePath, String keyStoreType, String keystorePw, String keystoreManagerPw, String trustStorePath, String trustStoreType, String trustStorePw) throws Exception {
        server = new Server();
        
        // === HTTP Configuration ===
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSecureScheme("https");
        httpConfig.setSecurePort(CLIENT_AUTH_HTTPS_PORT);
        httpConfig.setOutputBufferSize(32768);
        httpConfig.setRequestHeaderSize(8192);
        httpConfig.setResponseHeaderSize(8192);
        httpConfig.setSendServerVersion(true);
        httpConfig.setSendDateHeader(false);

        // === Add HTTP Connector ===
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        http.setPort(HTTP_PORT);
        http.setIdleTimeout(30000);
        server.addConnector(http);

        // === Configure SSL KeyStore, TrustStore, and Ciphers ===
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keyStorePath);
        if (keystorePw != null && !keystorePw.isEmpty()) {
            sslContextFactory.setKeyStorePassword(keystorePw);
        }
        sslContextFactory.setKeyStoreType(keyStoreType);
        if (keystoreManagerPw != null && !keystoreManagerPw.isEmpty()) {
            sslContextFactory.setKeyManagerPassword(keystoreManagerPw);
        }
        sslContextFactory.setTrustStorePath(trustStorePath);
        sslContextFactory.setTrustStoreType(trustStoreType);
        sslContextFactory.setTrustStorePassword(trustStorePw);
        // OPTIONAL - for client certificate auth (both are not needed otherwise)
        sslContextFactory.setWantClientAuth(true);
        sslContextFactory.setNeedClientAuth(true);

        // === SSL HTTP Configuration ===
        HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
        httpsConfig.addCustomizer(new SecureRequestCustomizer()); // <-- HERE
        
        // == Add SSL Connector ===
        ServerConnector sslConnector = new ServerConnector(server, 
                new SslConnectionFactory(sslContextFactory,"http/1.1"), new HttpConnectionFactory(httpsConfig));
        sslConnector.setPort(CLIENT_AUTH_HTTPS_PORT);
        server.addConnector(sslConnector);

        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(BlockingServletExample.class, "/status80");
        server.setHandler(servletHandler);
    }

    public void start() throws Exception {
        server.start();
    }
    
    public void stop() throws Exception {
        server.stop();
    }
    
 }
