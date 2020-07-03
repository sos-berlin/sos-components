package com.sos.joc.classes.proxy;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;

import js7.proxy.javaapi.JMasterProxy;

public class Proxies {

    private static Proxies proxies;
    private int connectionTimeout = 20;
    private volatile Map<ProxyCredentials, JMasterProxy> jMasterProxies = new ConcurrentHashMap<>();

    private Proxies() {
        //
    }

    public static synchronized Proxies getInstance() {
        if (proxies == null) {
            proxies = new Proxies();
        }
        return proxies;
    }
    
    public Map<ProxyCredentials, JMasterProxy> getProxies() {
        return jMasterProxies;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public static synchronized JMasterProxy connect(ProxyCredentialsBuilder credentials) throws JobSchedulerConnectionRefusedException, JobSchedulerConnectionResetException {
        return getInstance().start(credentials.build());
    }
    
    public static synchronized JMasterProxy connect(ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException, JobSchedulerConnectionResetException {
        return getInstance().start(credentials);
    }

    public static synchronized void close(JMasterProxy proxy) {
        Proxies proxies = getInstance();
        if (proxies.jMasterProxies.containsValue(proxy)) {
            try {
                proxy.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
            Optional<ProxyCredentials> keyOfProxy = proxies.jMasterProxies.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), proxy)).map(
                    Map.Entry::getKey).findFirst();
            if (keyOfProxy.isPresent()) {
                proxies.jMasterProxies.remove(keyOfProxy.get());
            }
        }
    }
    
    public static synchronized void close(ProxyCredentials proxy) {
        Proxies proxies = getInstance();
        if (proxies.jMasterProxies.containsKey(proxy)) {
            try {
                proxies.jMasterProxies.get(proxy).close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
            proxies.jMasterProxies.remove(proxy);
        }
    }

    public static synchronized void closeAll() {
        // TODO create Callable<Void> for close in parallel
        // What exceptions???
        Proxies proxies = getInstance();
        Collections.unmodifiableCollection(proxies.jMasterProxies.values()).stream().forEach(p -> {
            try {
                p.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
            }
        });
        proxies.jMasterProxies.clear();
    }
    
    private JMasterProxy start(ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException, JobSchedulerConnectionResetException {
        try {
            if (!jMasterProxies.containsKey(credentials)) {
                JMasterProxy masterProxy = JMasterProxy.start(credentials.getUrl(), credentials.getAccount(), credentials.getHttpsConfig()).get(connectionTimeout, TimeUnit.SECONDS);
                jMasterProxies.put(credentials, masterProxy);
            }
            return jMasterProxies.get(credentials);
        } catch (InterruptedException e) {
            throw new JobSchedulerConnectionResetException(credentials.getUrl());
        } catch (Exception e) {
            throw new JobSchedulerConnectionRefusedException(credentials.getUrl());
        }
    }

}
