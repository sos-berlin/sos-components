package com.sos.joc2;

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
            put("http://controller-2-0-primary:4444", "http://centostest_primary.sos:5444");
            put("http://controller-2-0-secondary:4444", "http://centostest_primary.sos:5544");
            put("http://controller-2-0-standalone:4444", "http://centostest_primary.sos:5344");
            put("https://controller-2-0-primary:4443", "http://centostest_primary.sos:5444");
            put("https://controller-2-0-secondary:4443", "http://centostest_primary.sos:5544");
            put("https://controller-2-0-standalone:4443", "http://centostest_primary.sos:5344");
        }
    });

    public static final Map<String, String> centosdev_third_urlMapper = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("http://controller-2-0-primary:4444", "http://centosdev_third.sos:5444");
            put("http://controller-2-0-secondary:4444", "http://centosdev_third.sos:5544");
            put("http://controller-2-0-standalone:4444", "http://centosdev_third.sos:5344");
            put("https://controller-2-0-primary:4443", "http://centosdev_third.sos:5444");
            put("https://controller-2-0-secondary:4443", "http://centosdev_third.sos:5544");
            put("https://controller-2-0-standalone:4443", "http://centosdev_third.sos:5344");
        }
    });

    public static final Map<String, Map<String, String>> urlMapperByUser = Collections.unmodifiableMap(new HashMap<String, Map<String, String>>() {

        private static final long serialVersionUID = 1L;

        {
            put("oh", centosdev_secondary_urlMapper);
            put("sp", centosdev_third_urlMapper);
        }
    });
    
    public static final Map<String, JocSecurityLevel> securityLevelByUser = Collections.unmodifiableMap(new HashMap<String, JocSecurityLevel>() {

        private static final long serialVersionUID = 1L;

        {
            put("oh", JocSecurityLevel.LOW);
            put("santiago", JocSecurityLevel.MEDIUM);
        }
    });
    
    public static Map<String, String> getUrlMapperByUser() {
        return urlMapperByUser.get(System.getProperty("user.name").toLowerCase());
    }
    
    public static JocSecurityLevel getSecurityLevelByUser() {
        return securityLevelByUser.get(System.getProperty("user.name").toLowerCase());
    }
}
