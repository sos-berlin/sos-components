package com.sos.commons.util.loganonymizer.classes;

public abstract class DefaultRulesTable {

    protected abstract void add(String item, String search, String... replace);
    private static final String portCapture = "(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[1-9][0-9]{0,3})";

    public DefaultRulesTable() {
        super();
        init();
    }

    private void init() {

        add( "url-component",             // any log              ://apmacwin:4244
                "://([^:]*):" + portCapture,
                "<host>","<port>" );

        add( "ip-address",                // any log              192.168.2.1
                "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))",
                "<ip-address>" );

        add( "host-install",              // Install*.log         host                 = apmacwin
                "host\\s*=\\s*(\\S*)",
                "<host>" );

        add( "port-install",              // Install*.log         port                 = 4244
                "port\\s*=\\s*" + portCapture,
                "<port>" );

        add( "host-status-install",       // Install*.log         "hostname": "APMACWIN"
                "\\\"hostname\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"",
                "<host>" );

        add( "user-name",                 // Install*.log         user_name            = ap
                "user(?:_name)?\\s*=\\s*(\\S*)", 
                "<user>" );
        
        add( "user-home",                 // Install*.log         user_home            = C:\Users\ap
                "user_home\\s*=\\s*(\\S*)",
                "<user-home>" );

        add( "service-account",           // Install*.log         service_account      = .\ap
                "service_acc?ount\\s*=\\s*([^\\\\]*)\\\\(\\S*)", 
                "<host>", "<service-user>" );

//        add( "jetty-port",                // Install*.log         jetty_port            = 4446
//                "jetty(?:_stop)?_port\\s*=\\s*" + portCapture, 
//                "<jetty-port>" );

//        add( "jetty-user",                // Install*.log         jetty_user            = ap
//                "jetty_user\\s*=\\s(.*)", 
//                "<jetty-user>" );
        
//        add( "jetty-user-home",           // Install*.log         jetty_user_home       = C:\Users\ap
//                "jetty_user_home\\s*=\\s*(.*)", 
//                "<jetty-user-home>" );

//        add( "db-user",                   // Install*.log         user                  = jobscheduler
//                "user\\s*=\\s(\\S*)", 
//                "<db-user>" );
        
        add( "db-url",                    // joc Install*.log, joc.log?                   jdbc:mysql://db_host:db_port/jobscheduler?serverTimezone=UTC
                "jdbc:.*?:(?:@//|@|//)([^/;]*)", 
                "<db-host-port>" );
        
        add( "connect-component",         // joc.log              Connect(apmacwin:4344   Connect apmacwin:4344
                "Connect\\s*\\(?([^:]*):" + portCapture,
                "<host>:<port>" );

        add( "user-request",              // joc.log              USER: gauss@example.com
                "USER:\\s*(\\S*)", 
                "<user-account>" );
        
        add( "user-login",                // controller.log       User:jobscheduler
                "User:([^: ]*)",
                "<user-id>" );
        
        add( "agent-id",                  // controller.log
                "agent\\s*=\\s*(.*)", 
                "<agent-id>" );
        
    }

}
