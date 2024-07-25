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
    
    private DatagramSocket ds = null;
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
        UDPServer instance = UDPServer.getInstance();
        
        if (instance.thread == null) {
            Thread thread = new Thread(instance);
            thread.setName(UDPServer.class.getSimpleName());
            
            instance.thread = thread;
            thread.start();
        }
    }

    public synchronized static final void shutdown() {
        UDPServer instance = UDPServer.getInstance();
        instance.stop();
        instance = null;
    }
    
    private void setPort() {
        ConfigurationGlobalsJoc jocSettings = Globals.getConfigurationGlobalsJoc();
        this.port = jocSettings.getUDPPort();
    }
    
    private void stop() {
        this.running = false;
        this.sleep();
        
        if (this.ds != null && !this.ds.isClosed()) {
            this.ds.close();
            this.sleep();
            //System.out.print(" Thread alive? " + this.thread.isAlive());
            this.thread = null;
        }
        
    }
    
    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
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
            this.ds = createDatagramSocket();
            this.running = true;
            
        } catch (SocketException se) {
            se.printStackTrace(System.err);
            
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace(System.err);
        }
            
        byte[] receiveData = new byte[BUFFER_SIZE];
                    
        while(this.running) {
            try {
                DatagramPacket dp = new DatagramPacket(receiveData, receiveData.length);
                
                this.ds.receive(dp);
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


