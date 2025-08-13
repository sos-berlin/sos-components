package com.sos.joc2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sos.joc.model.common.JocSecurityLevel;

public class MapUrls {

    public static final Map<String, String> centosdev_secondary_urlMapper = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("http://controller-2-0-primary:4444", "http://centosdev_secondary.sos:5444");
            put("http://controller-2-0-backup:4444", "http://centosdev_secondary.sos:5544");
            put("http://controller-2-0-secondary:4444", "http://centosdev_secondary.sos:5544");
            put("http://controller-2-0-standalone:4444", "http://centosdev_secondary.sos:5344");
            put("https://controller-2-0-primary:4443", "http://centosdev_secondary.sos:5444");
            put("https://controller-2-0-backup:4443", "http://centosdev_secondary.sos:5544");
            put("https://controller-2-0-secondary:4443", "http://centosdev_secondary.sos:5544");
            put("https://controller-2-0-standalone:4443", "http://centosdev_secondary.sos:5344");
//            put("https://controller-2-0-primary:4443", "https://centosdev_secondary.sos:5443");
//            put("https://controller-2-0-backup:4443", "https://centosdev_secondary.sos:5543");
//            put("https://controller-2-0-standalone:4443", "https://centosdev_secondary.sos:5343");
        }
    });
    
    public static final Map<String, String> centostest_primary_urlMapper = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("http://controller-2-0-primary:4444", "http://192.11.0.147:5444"); //centostest_primary.sos
            put("http://controller-2-0-secondary:4444", "http://192.11.0.147:5544");
            put("http://controller-2-0-standalone:4444", "http://192.11.0.147:5344");
            put("https://controller-2-0-primary:4443", "http://192.11.0.147:5444");
            put("https://controller-2-0-secondary:4443", "http://192.11.0.147:5544");
            put("https://controller-2-0-standalone:4443", "http://192.11.0.147:5344");
        }
    });

    public static final Map<String, String> centosdev_third_urlMapper = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("http://controller-2-0-primary.sos:4444", "http://centosdev_third.sos:5444");
            put("http://controller-2-0-secondary.sos:4444", "http://centosdev_third.sos:5544");
            put("http://controller-2-0-standalone.sos:4444", "http://centosdev_third.sos:5344");
            put("https://controller-2-0-primary.sos:4443", "http://centosdev_third.sos:5444");
            put("https://controller-2-0-secondary.sos:4443", "http://centosdev_third.sos:5544");
            put("https://controller-2-0-standalone.sos:4443", "http://centosdev_third.sos:5344");
        }
    });
    // http://sp:5555
    public static final Map<String, String> sp_local_urlMapper = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
//            put("http://sp:11111", "http://sp:11111");
//            put("http://sp:11112", "http://sp:11112");
//            put("http://sp:1113", "http://sp:1113");
            put("http://sp.sos:1113", "http://sp.sos:1113");
        }
    });

    public static final Map<String, Map<String, String>> urlMapperByUser = Collections.unmodifiableMap(new HashMap<String, Map<String, String>>() {

        private static final long serialVersionUID = 1L;

        {
            //put("oh", centosdev_secondary_urlMapper);
            put("oh", centostest_primary_urlMapper);
            put("sp", sp_local_urlMapper);
        }
    });
    
    public static final Map<String, JocSecurityLevel> securityLevelByUser = Collections.unmodifiableMap(new HashMap<String, JocSecurityLevel>() {

        private static final long serialVersionUID = 1L;

        {
            //put("oh", JocSecurityLevel.LOW);
            put("oh", JocSecurityLevel.MEDIUM);
//            put("sp", JocSecurityLevel.MEDIUM);
            put("santiago", JocSecurityLevel.MEDIUM);
        }
    });
    
    public static Map<String, String> getUrlMapperByUser() {
        return urlMapperByUser.get(System.getProperty("user.name").toLowerCase());
    }
    
    public static JocSecurityLevel getSecurityLevelByUser() {
        return securityLevelByUser.get(System.getProperty("user.name").toLowerCase());
    }
    
    public static String getJocProperties() {
        Path resources = Paths.get("src/main/resources/joc");
        String userJocProperties = "joc." + System.getProperty("user.name").toLowerCase() + ".properties";
        if (Files.exists(resources.resolve(userJocProperties))) {
            return "/joc/" + userJocProperties;
        } else {
            return "/joc/joc.properties";
        }
    }
}
