package com.sos.joc.classes.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class RemoveSemaphore {
    
    private static RemoveSemaphore instance;
    private Map<String, RecallRevokeSemaphore> semaphores;
    private static final long WAIT_TIMEOUT = 30; // in seconds
    
    private RemoveSemaphore() {
        if(semaphores == null) {
            semaphores = new HashMap<String, RecallRevokeSemaphore>();
        }
    }
    
    public static synchronized RemoveSemaphore getInstance() {
        if (instance == null) {
            instance = new RemoveSemaphore();
        }
        return instance;
    }
    
    public static boolean tryAcquire(String accessToken, String initialCaller) throws InterruptedException {
        return getInstance()._tryAcquire(accessToken, initialCaller);
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
    
    
    public static int availablePermits(String accessToken) {
        return getInstance()._availablePermits(accessToken);
    }
    
    private boolean _tryAcquire(String accessToken, String initialCaller) throws InterruptedException {
        semaphores.putIfAbsent(accessToken, new RecallRevokeSemaphore(1, initialCaller));
        return semaphores.get(accessToken).tryAcquire(WAIT_TIMEOUT, TimeUnit.SECONDS);
    }
    
    private void _release(String accessToken) {
        if(semaphores.containsKey(accessToken)) {
            semaphores.get(accessToken).release();
        }
    }
    
    private void _remove(String accessToken) {
        if(semaphores.containsKey(accessToken)) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    semaphores.remove(accessToken);
                }
            }, TimeUnit.SECONDS.toMillis(2));
        }
    }
    
    private int _availablePermits(String accessToken) {
        if (semaphores.containsKey(accessToken)) {
            return semaphores.get(accessToken).availablePermits();
        }
        return -1;
    }
    
    private int _getQueueLength(String accessToken) {
        if(semaphores.containsKey(accessToken)) {
            return semaphores.get(accessToken).getQueueLength();
        } else {
            return 0;
        }
    }

    
    public Map<String, RecallRevokeSemaphore> getSemaphores() {
        return semaphores;
    }
    
    public Optional<RecallRevokeSemaphore> getSemaphore(String accessToken) {
        return Optional.ofNullable(semaphores.get(accessToken));
    }
    
}
