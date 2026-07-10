package com.sos.joc.classes.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class PublishSemaphore {
    
    private static PublishSemaphore instance;
    private Map<String, ReleaseDeploySemaphore> semaphores;
    private static final long WAIT_TIMEOUT = 30; // in seconds
    
    private PublishSemaphore() {
        if(semaphores == null) {
            semaphores = new HashMap<String, ReleaseDeploySemaphore>();
        }
    }
    
    public static synchronized PublishSemaphore getInstance() {
        if (instance == null) {
            instance = new PublishSemaphore();
        }
        return instance;
    }
    
    public static boolean tryAcquire(String transactionId, String initialCaller) throws InterruptedException {
        return getInstance()._tryAcquire(transactionId, initialCaller);
    }
    
    public static int getQueueLength(String accessToken) {
        return getInstance()._getQueueLength(accessToken);
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
            semaphores.putIfAbsent(transactionId, new ReleaseDeploySemaphore(1, initialCaller));
        }
        if(semaphores.get(transactionId) != null) {
            return semaphores.get(transactionId).tryAcquire(WAIT_TIMEOUT, TimeUnit.SECONDS);
        } else {
            return false;
        }
    }
    
    private void _release(String transactionId) {
        if(semaphores.containsKey(transactionId)) {
            semaphores.get(transactionId).release();
        }
    }
    
    private void _remove(String transactionId) {
        if(semaphores.containsKey(transactionId)) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    semaphores.remove(transactionId);
                }
            }, TimeUnit.SECONDS.toMillis(2));
        }
    }
    
    private int _availablePermits(String transactionId) {
        if (semaphores.containsKey(transactionId)) {
            return semaphores.get(transactionId).availablePermits();
        }
        return -1;
    }
    
    private int _getQueueLength(String transactionId) {
        if(semaphores.containsKey(transactionId)) {
            return semaphores.get(transactionId).getQueueLength();
        } else {
            return 0;
        }
    }

    
    public Map<String, ReleaseDeploySemaphore> getSemaphores() {
        return semaphores;
    }
    
    public Optional<ReleaseDeploySemaphore> getSemaphore(String transactionId) {
        return Optional.ofNullable(semaphores.get(transactionId));
    }
    
}
