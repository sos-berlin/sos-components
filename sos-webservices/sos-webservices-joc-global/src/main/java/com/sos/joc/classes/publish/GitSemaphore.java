package com.sos.joc.classes.publish;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GitSemaphore {
    
    private static GitSemaphore instance;
    private Semaphore semaphore;
    private static final long waitTimeOut = 30; // in seconds
    
    private GitSemaphore() {
        semaphore = new Semaphore(1); 
    }
    
    public static synchronized GitSemaphore getInstance() {
        if (instance == null) {
            instance = new GitSemaphore();
        }
        return instance;
    }
    
    public static boolean tryAcquire() throws InterruptedException {
        return getInstance()._tryAcquire();
    }
    
    public static int getQueueLength() {
        return getInstance()._getQueueLength();
    }
    
    public static void release() {
        getInstance()._release();
    }
    
    private boolean _tryAcquire() throws InterruptedException {
        return semaphore.tryAcquire(waitTimeOut, TimeUnit.SECONDS);
    }
    
    private void _release() {
        semaphore.release();
    }
    
    private int _getQueueLength() {
        return semaphore.getQueueLength();
    }
    
}
