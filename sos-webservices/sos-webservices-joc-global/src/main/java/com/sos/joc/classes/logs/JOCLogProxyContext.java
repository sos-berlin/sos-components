package com.sos.joc.classes.logs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import js7.base.log.LogLevel;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.JResource;
import js7.proxy.javaapi.log.JLogDirectoryIndex;

public class JOCLogProxyContext {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCLogProxyContext.class);
    private static JOCLogProxyContext instance;
    private JProxyContext proxyContext;
    private static Path logDir = Paths.get("logs");
    public static ZoneId zoneId = ZoneId.of("UTC");
    //for tests
    //private static Path logDir = Paths.get("C:\\ProgramData\\sos-berlin.com\\js7\\joc.2.9\\jetty_base\\logs");
    
    private JOCLogProxyContext() {
        Config config = ConfigFactory.parseMap(Map.of("js7.log.index.recompress", "lz4/java")); //lz4/java, lz4, deflate
        proxyContext = JProxyContext.start(config).join();
    }
    
    public static JOCLogProxyContext getInstance() {
        if (instance == null) {
            instance = new JOCLogProxyContext();
        }
        return instance;
    }
    
    public static void release() {
        try {
            JOCLogProxyContext.getInstance()._release();
        } catch (InterruptedException e) {
            //
        } catch (Exception e) {
            LOGGER.warn("", e);
        }
    }
    
    public void _release() throws InterruptedException, ExecutionException, TimeoutException {
        if (proxyContext != null) {
            proxyContext.release().get(3, TimeUnit.SECONDS);
        }
    }
    
    public static JResource<JLogDirectoryIndex> getJResource(String logPrefix, LogLevel logLevel) {
        return JOCLogProxyContext.getInstance()._getJResource(logPrefix, logLevel);
    }
    
    private JResource<JLogDirectoryIndex> _getJResource(String logPrefix, LogLevel logLevel) {
        // logging with js7.base.log.reader.LogDirectoryIndex
        return JLogDirectoryIndex.directory(logDir, logPrefix, logLevel, true, zoneId, proxyContext);
    }
}
