package com.sos.joc.classes.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


public class PublishSemaphore {
    
    private static PublishSemaphore instance;
    private Semaphore semaphore;
    private Map<String, Semaphore> semaphores;
    private static final long WAIT_TIMEOUT = 30; // in seconds
    
    private PublishSemaphore() {
        if(semaphores == null) {
            semaphores = new HashMap<String, Semaphore>();
        }
    }
    
    public static synchronized PublishSemaphore getInstance() {
        if (instance == null) {
            instance = new PublishSemaphore();
        }
        return instance;
    }
    
    public static boolean tryAcquire(String accessToken) throws InterruptedException {
        return getInstance()._tryAcquire(accessToken);
    }
    
    public static int getQueueLength(String accessToken) {
        return getInstance()._getQueueLength(accessToken);
    }
    
    public static void release(String accessToken) {
        getInstance()._release(accessToken);
    }
    
    public static void remove (String accessToken) {
        getInstance()._remove(accessToken);
    }
    
    private boolean _tryAcquire(String accessToken) throws InterruptedException {
        semaphores.putIfAbsent(accessToken, new Semaphore(1));
        return semaphores.get(accessToken).tryAcquire(WAIT_TIMEOUT, TimeUnit.SECONDS);
    }
    
    private void _release(String accessToken) {
        semaphores.get(accessToken).release();
    }
    
    private void _remove(String accessToken) {
        if(semaphores.containsKey(accessToken)) {
            semaphores.remove(accessToken);
        }
    }
    
    private int _getQueueLength(String accessToken) {
        if(semaphores.get(accessToken) != null) {
            return semaphores.get(accessToken).getQueueLength();
        } else {
            return 0;
        }
    }

    
    public Map<String, Semaphore> getSemaphores() {
        return semaphores;
    }
    
}
