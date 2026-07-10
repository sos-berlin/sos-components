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
    
    public static boolean tryAcquire(String transactionId, String initialCaller) throws InterruptedException {
        return getInstance()._tryAcquire(transactionId, initialCaller);
    }
    
    public static int getQueueLength(String transactionId) {
        return getInstance()._getQueueLength(transactionId);
    }
    
    public static void release(String transactionId) {
        getInstance()._release(transactionId);
    }
    
    public static void remove (String transactionId) {
        getInstance()._remove(transactionId);
    }
    
    
    public static int availablePermits(String transactionId) {
        return getInstance()._availablePermits(transactionId);
    }
    
    private boolean _tryAcquire(String transactionId, String initialCaller) throws InterruptedException {
        // only one semaphore allowed in map
        if(semaphores.keySet().size() == 0) {
            semaphores.putIfAbsent(transactionId, new RecallRevokeSemaphore(1, initialCaller));
        }
        if(semaphores.get(transactionId) != null) {
            return semaphores.get(transactionId).tryAcquire(WAIT_TIMEOUT, TimeUnit.SECONDS);
        } else {
            return false;
        }
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
