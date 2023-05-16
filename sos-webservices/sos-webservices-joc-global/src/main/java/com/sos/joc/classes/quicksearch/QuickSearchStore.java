package com.sos.joc.classes.quicksearch;

import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.sos.joc.model.inventory.search.RequestQuickSearchFilter;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;

public class QuickSearchStore {
    
    private static QuickSearchStore INSTANCE;
    private volatile ConcurrentMap<String, QuickSearchRequest> results = new ConcurrentHashMap<>();
    private Timer timer;
    private TimerTask timerTask;
    private final static long cleanupPeriodInMillis = TimeUnit.SECONDS.toMillis(10);
    
    private QuickSearchStore() {
        startTimer();
    }
    
    private static QuickSearchStore getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QuickSearchStore();
        }
        return INSTANCE;
    }
    
    public static void close() {
        getInstance()._close();
    }
    
    public static RequestQuickSearchFilter checkToken(RequestQuickSearchFilter request, String accessToken) {
        return getInstance()._checkToken(request, accessToken);
    }
    
    public static void putResult(String token, RequestQuickSearchFilter request, List<ResponseBaseSearchItem> result) {
        getInstance()._putResult(token, request, result);
    }
    
    public static void putResult(String token, QuickSearchRequest result) {
        getInstance()._putResult(token, result);
    }
    
    public static void updateTimeStamp(String token, long timestamp) {
        getInstance()._updateTimeStamp(token, timestamp);
    }
    
    public static void updateTimeStamp(String token) {
        getInstance()._updateTimeStamp(token, Instant.now().toEpochMilli());
    }
    
    public static List<ResponseBaseSearchItem> getResult(RequestQuickSearchFilter request) {
        return getInstance()._getResult(request);
    }
    
    public static void deleteResult(String token) {
        getInstance()._deleteResult(token);
    }
    
    private RequestQuickSearchFilter _checkToken(RequestQuickSearchFilter request, String accessToken) {
        if (request == null) {
            return null;
        }
        if (request.getToken() != null) {
            if (!results.containsKey(request.getToken())) {
                request.setToken(null);
            }
        } else {
            String token = QuickSearchRequest.createToken(request.getSearch(), request.getReturnTypes(), accessToken);
            if (results.containsKey(token)) {
                request.setToken(token);
            }
        }
        return request;
    }
    
    private void _putResult(String token, RequestQuickSearchFilter request, List<ResponseBaseSearchItem> result) {
        if (result != null) {
            results.put(token, new QuickSearchRequest(request.getSearch(), request.getReturnTypes(), result));
        }
    }
    
    private void _putResult(String token, QuickSearchRequest result) {
        if (result != null && !result.isEmpty() ) {
            results.put(token, result);
        }
    }
    
    private void _updateTimeStamp(String token, long timestamp) {
        QuickSearchRequest result = results.get(token);
        if (result != null) {
            result.setTimestamp(timestamp);
        }
    }
    
    private List<ResponseBaseSearchItem> _getResult(RequestQuickSearchFilter request) {
        if (request != null && request.getToken() != null) {
            QuickSearchRequest result = results.get(request.getToken());
            if (result != null) {
                List<ResponseBaseSearchItem> newResult =  result.getNewResult(request);
                if (newResult == null) {
                    results.remove(request.getToken());
                    request.setToken(null);
                    return null;
                }
                return newResult;
            }
        }
        return null;
    }
    
    private void _deleteResult(String token) {
        if (token != null) {
            results.remove(token);
        }
    }
    
    private void _close() {
        try {
            if (timerTask != null) {
                timerTask.cancel(); 
            }
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        } catch (Throwable e) {
            //
        }
        results.clear();
    }
    
    private void startTimer() {
        if (timer == null) {
            timer = new Timer("Timer-CleanupQuickSearch");
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    Long oldTimestamp = Instant.now().toEpochMilli() - cleanupPeriodInMillis - cleanupPeriodInMillis;
                    results.values().removeIf(e -> e.getTimestamp() < oldTimestamp);
                }

            };
            timer.scheduleAtFixedRate(timerTask, cleanupPeriodInMillis, cleanupPeriodInMillis);
        }
    }

}
