package com.sos.joc.classes.publish;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class GitSemaphore {
    
    private static GitSemaphore instance;
//    private static final Logger LOGGER = LoggerFactory.getLogger(GitSemaphore.class);
    private Semaphore semaphore;
    private static final long waitTimeOut = 30; // in seconds
//    private Semaphore semaphoreAdd;
//    private Semaphore semaphoreCommit;
//    private Semaphore semaphorePush;
//    private Semaphore semaphorePull;
//    private Semaphore semaphoreCheckout;
//    private Semaphore semaphoreUpdate;
//    private Semaphore semaphoreDelete;
//    private Semaphore semaphoreStore;
//    private Semaphore semaphoreRead;
//    private String accessToken;
//    private String account;
//    private String apiCall;
    
//    private final Map<GitRepoCommand, Semaphore> semaphores = Collections.unmodifiableMap(new HashMap<GitRepoCommand, Semaphore>() {
//
//        private static final long serialVersionUID = 1L;
//
//        {
//            put(GitRepoCommand.ADD, semaphoreAdd);
//            put(GitRepoCommand.COMMIT, semaphoreCommit);
//            put(GitRepoCommand.PUSH, semaphorePush);
//            put(GitRepoCommand.PULL, semaphorePull);
//            put(GitRepoCommand.CHECKOUT, semaphoreCheckout);
//            put(GitRepoCommand.STORE, semaphoreStore);
//            put(GitRepoCommand.READ, semaphoreRead);
//            put(GitRepoCommand.UPDATE, semaphoreUpdate);
//            put(GitRepoCommand.DELETE, semaphoreDelete);
//            
//        }
//    });
    
    private GitSemaphore() {
//        semaphores.values().forEach(s -> s = new Semaphore(1));
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
    
//    public static boolean tryAcquire(String accessToken, GitRepoCommand repoCommand) {
//        return getInstance()._tryAcquire(accessToken, repoCommand);
//    }
//    
//    public static boolean tryAcquireAdd(String accessToken) {
//        return getInstance()._tryAcquire(accessToken, GitRepoCommand.ADD);
//    }
    
    public static void release() {
        getInstance()._release();
    }
    
//    public static void release(String accessToken, GitRepoCommand repoCommand) {
//        getInstance()._release(repoCommand);
//    }
//
//    public static void releaseAdd() {
//        getInstance()._release(GitRepoCommand.ADD);
//    }
    
    private boolean _tryAcquire() throws InterruptedException {
        return semaphore.tryAcquire(waitTimeOut, TimeUnit.SECONDS);
    }

//    private boolean _tryAcquire(String accessToken, GitRepoCommand repoCommand) {
//        //this.accessToken = accessToken;
//        boolean permitted = semaphores.get(repoCommand).tryAcquire();
////        if (!permitted && accessToken.equals(this.accessToken)) {
////            return true;
////        }
//        return permitted;
//    }
    
    private void _release() {
        semaphore.release();
    }

//    private void _release(GitRepoCommand repoCommand) {
//        //if (accessToken.equals(this.accessToken)) {
//            semaphores.get(repoCommand).release();
//        //}
//    }
    
    private int _getQueueLength() {
        return semaphore.getQueueLength();
    }

}
