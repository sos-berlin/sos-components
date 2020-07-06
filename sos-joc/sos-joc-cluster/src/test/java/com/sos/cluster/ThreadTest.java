package com.sos.cluster;

public class ThreadTest implements Runnable {

    Thread thread = null;

    public synchronized void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public synchronized void stop() {
        if (thread != null) {
            thread = null;
        }
    }

    public synchronized void interrupt() {
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        while(thread != null) {
            
        }
    }

}
