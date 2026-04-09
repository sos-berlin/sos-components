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
        semaphores.putIfAbsent(accessToken, new ReleaseDeploySemaphore(1, initialCaller));
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
            }, TimeUnit.MINUTES.toMillis(2));
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

    
    public Map<String, ReleaseDeploySemaphore> getSemaphores() {
        return semaphores;
    }
    
    public Optional<ReleaseDeploySemaphore> getSemaphore(String accessToken) {
        return Optional.ofNullable(semaphores.get(accessToken));
    }
    
}
