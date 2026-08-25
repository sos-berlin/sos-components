package com.sos.joc.classes.logs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.model.log.JOCServiceId;

import js7.proxy.javaapi.JResource;
import js7.proxy.javaapi.log.JLogDirectoryIndex;

public class JOCLogProxyContext {
    
    private static JOCLogProxyContext instance;
    //private JProxyContext proxyContext;
    private static Path logDir = Paths.get("logs");
    private JResource<JLogDirectoryIndex> resource = null;
    public static ZoneId zoneId = ZoneId.of("UTC");
    private static Set<String> logPrefizes = EnumSet.allOf(JOCServiceId.class).stream().map(JOCServiceId::value).collect(Collectors.toSet());
    //for tests
    //private static Path logDir = Paths.get("C:\\ProgramData\\sos-berlin.com\\js7\\joc.2.9\\jetty_base\\logs");
    
    private JOCLogProxyContext() {
        //Config config = ConfigFactory.parseMap(Map.of("js7.log.index.recompress", "lz4")); //lz4/java, lz4, deflate
        //proxyContext = JProxyContext.start().join(); //JProxyContext.start(config).join();
        logDir = logDir.toAbsolutePath();
    }
    
    public static JOCLogProxyContext getInstance() {
        if (instance == null) {
            instance = new JOCLogProxyContext();
        }
        return instance;
    }
    
    public static JResource<JLogDirectoryIndex> getJResource() {
        return JOCLogProxyContext.getInstance()._getJResource();
    }
    
    private JResource<JLogDirectoryIndex> _getJResource() {
        // logging with js7.base.log.reader.LogDirectoryIndex
        if (resource == null) {
            resource = JLogDirectoryIndex.directory(logDir, logPrefizes, zoneId, Proxies.proxyContext);
        }
        return resource;
    }
}
