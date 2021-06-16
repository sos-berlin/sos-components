package com.sos.auth.shiro;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SOSSSLSocketFactory extends SocketFactory {
    private static final AtomicReference<SOSSSLSocketFactory> defaultFactory = new AtomicReference<>();

    private SSLSocketFactory sf;

    public SOSSSLSocketFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        
        com.sos.joc.classes.SSLContext sslContext = com.sos.joc.classes.SSLContext.getInstance();
        sslContext.loadTrustStore();
        KeyStore keyStore = sslContext.getTrustStore(); 
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        sf = ctx.getSocketFactory();
    }

    public static SocketFactory getDefault()  {
        final SOSSSLSocketFactory value = defaultFactory.get();
        if (value == null) {
            try {
                defaultFactory.compareAndSet(null, new SOSSSLSocketFactory());
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                e.printStackTrace();
            }
            return defaultFactory.get();
        }
        return value;
    }

    @Override
    public Socket createSocket(final String s, final int i) throws IOException {
        return sf.createSocket(s, i);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return sf.createSocket(host,port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return sf.createSocket(host,port,localHost,localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return sf.createSocket(address,port,localAddress,localPort);
    }


}
