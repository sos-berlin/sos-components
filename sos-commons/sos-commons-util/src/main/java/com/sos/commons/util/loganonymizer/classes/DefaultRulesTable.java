package com.sos.commons.util.loganonymizer.classes;

public abstract class DefaultRulesTable {

    protected abstract void a(String item, String search, String... replace);

    public DefaultRulesTable() {
        super();
        init();
    }

    private void init() {

        a( "url-component",             // any log              ://apmacwin:4244
                "://(.*):(\\d{3,5})", 
                "<host>","<port>" );

        a( "ip-address",                // any log              192.168.2.1
                "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))",
                "<ip-address>" );

        a( "host-install",              // Install*.log         host                 = apmacwin
                "host[\\s]*=[\\s]*(.*)",
                "<host>" );

        a( "host-http-install",         // Install*.log         http_host            = localhost
                "http_host[\\s]*=[\\s]*(.*)",
                "<host>" );

        a( "port-http-install",         // Install*.log         http_host            = localhost
                "http_port[\\s]*=[\\s]*(.*)",
                "<port>" );

        a( "host-status-install",       // Install*.log         "hostname": "APMACWIN"
                "\\\"hostname\\\"[\\s]*:[\\s]*\\\"(.*)\\\"",
                "<host>" );

        a( "user-name",                 // Install*.log         user_name            = ap
                "user_name[\\s]*=[\\s](.*)$", 
                "<user>" );
        
        a( "user-home",                 // Install*.log         user_home            = C:\Users\ap
                "user_home[\\s]*=[\\s]*(.*)$", 
                "<user-home>" );

        a( "jetty-port",                // Install*.log         jetty_port            = 4446
                "jetty_port[\\s]*=[\\s]*(.*)$", 
                "<jetty-port>" );

        a( "jetty-stop-port",           // Install*.log         jetty_port            = 4446
                "jetty_stop_port[\\s]*=[\\s]*(.*)$", 
                "<jetty-port>" );

        a( "jetty-user",                // Install*.log         jetty_user            = ap
                "jetty_user[\\s]*=[\\s](.*)$", 
                "<jetty-user>" );
        
        a( "jetty-user-home",           // Install*.log         jetty_user_home       = C:\Users\ap
                "jetty_user_home[\\s]*=[\\s]*(.*)$", 
                "<jetty-user-home>" );

        a( "db-user",                   // Install*.log         user                  = jobscheduler
                "user[\\s]*=[\\s](.*)$", 
                "<db-user>" );
        
        a( "connect-component",         // joc.log              Connect(apmacwin:4344   Connect apmacwin:4344
                "Connect[\\s\\(]*(.*):(\\d{3,5})", 
                "<host>:<port>" );

        a( "user-request",              // joc.log              USER: gauss@example.com
                "USER:[ ]*(.*)[\\s]*", 
                "<user-account>" );
        
        a( "user-login",                // controller.log       User:jobscheduler
                "User:(.*)[\\s]*",
                "<user-id>" );
        
        a( "agent-id",                  // controller.log
                "agent[\\s]*=[\\s]*(.*)", 
                "<agent-id>" );
        
    }

}
