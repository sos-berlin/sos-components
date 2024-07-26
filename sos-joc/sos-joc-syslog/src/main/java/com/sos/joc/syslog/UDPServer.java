package com.sos.joc.syslog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import com.sos.joc.Globals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;

public class UDPServer implements Runnable {
    
    private static final int BUFFER_SIZE = 1024;
    private static UDPServer instance = null;
    
    private DatagramSocket socket = null;
    private Thread thread = null;
    private boolean running = false;
    private String host = null;
    private int port = 4514;
    
    private UDPServer() {
        //
    }
    
    private UDPServer(String host, Integer port) {
        this.host = host;
        if (port != null) {
            this.port = port;
        } else {
            setPort();
        }
    }
    
    private static UDPServer getInstance(String host, Integer port) {
        if (instance == null) {
            instance = new UDPServer(host, port);
        }
        return instance;
    }
    
    private static UDPServer getInstance(Integer port) {
        return getInstance(null, port);
    }
    
    private static UDPServer getInstance() {
        return getInstance(null);
    }
    
    public synchronized static final void start() {
        UDPServer.start((Integer) null);
    }
    
    public synchronized static final void start(Integer port) {
        UDPServer instance = UDPServer.getInstance(port);
        startThread(instance);
    }
    
    public synchronized static final void start(ConfigurationGlobalsJoc conf) {
        if (conf != null) {
            UDPServer.start(conf.getUDPPort());
        } else {
            UDPServer.start();
        }
    }

    public synchronized static final void shutdown() {
        UDPServer instance = UDPServer.getInstance();
        instance.stop();
        instance = null;
    }
    
    private static final void startThread(UDPServer instance) {
        if (instance.thread == null) {
            Thread thread = new Thread(instance, UDPServer.class.getSimpleName());
            
            instance.thread = thread;
            thread.start();
        }
    }
    
    private void setPort() {
        ConfigurationGlobalsJoc jocSettings = Globals.getConfigurationGlobalsJoc();
        if (jocSettings != null) {
            this.port = jocSettings.getUDPPort();
        }
    }
    
    public static int getPort() {
        return UDPServer.instance != null ? UDPServer.instance.port : 0;
    }
    
    private void stop() {
        this.running = false;
        this.sleep(1);
        
        if (this.socket != null && !this.socket.isClosed()) {
            this.socket.close();
            this.sleep(1);
            //System.out.print(" Thread alive? " + this.thread.isAlive());
            this.thread = null;
        }
        
        System.out.println("UDPServer is stopped");
    }
    
    private void sleep(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            //
        }
    }
    
    private DatagramSocket createDatagramSocket() throws SocketException, UnknownHostException {
        DatagramSocket datagramSocket = null;
        
        if (this.host != null && !this.host.isBlank()) {
            InetAddress inetAddress = InetAddress.getByName(this.host);
            datagramSocket = new DatagramSocket(this.port,inetAddress);
        } else {
            datagramSocket = new DatagramSocket(this.port);
        }
        
        return datagramSocket;
    }
    
    public void run() {
        try {
            this.socket = createDatagramSocket();
            this.running = true;
            System.out.println("UDPServer is started");
            
        } catch (SocketException se) {
            se.printStackTrace(System.err);
            
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace(System.err);
        }
            
        byte[] receiveData = new byte[BUFFER_SIZE];
                    
        while(this.running) {
            try {
                DatagramPacket dp = new DatagramPacket(receiveData, receiveData.length);
                
                this.socket.receive(dp);
                new EventHandler(dp.getData(), dp.getLength());
                //System.out.println(dp.getAddress().getHostName());

            } catch (SocketException se) {
                //
                
            } catch (IOException ioe) {
                ioe.printStackTrace(System.err);
            }
        }
    }
}


