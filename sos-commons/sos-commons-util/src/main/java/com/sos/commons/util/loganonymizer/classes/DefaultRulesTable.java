package com.sos.commons.util.loganonymizer.classes;

public abstract class DefaultRulesTable {

    protected abstract void add(String item, String search, String... replace);

    public DefaultRulesTable() {
        super();
        init();
    }

    private void init() {

        add( "url-component",             // any log              ://apmacwin:4244
                "://(.*):(\\d{3,5})", 
                "<host>","<port>" );

        add( "ip-address",                // any log              192.168.2.1
                "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))",
                "<ip-address>" );

        add( "host-install",              // Install*.log         host                 = apmacwin
                "host[\\s]*=[\\s]*(.*)",
                "<host>" );

        add( "host-http-install",         // Install*.log         http_host            = localhost
                "http_host[\\s]*=[\\s]*(.*)",
                "<host>" );

        add( "port-http-install",         // Install*.log         http_host            = localhost
                "http_port[\\s]*=[\\s]*(.*)",
                "<port>" );

        add( "host-status-install",       // Install*.log         "hostname": "APMACWIN"
                "\\\"hostname\\\"[\\s]*:[\\s]*\\\"(.*)\\\"",
                "<host>" );

        add( "user-name",                 // Install*.log         user_name            = ap
                "user_name[\\s]*=[\\s](.*)$", 
                "<user>" );
        
        add( "user-home",                 // Install*.log         user_home            = C:\Users\ap
                "user_home[\\s]*=[\\s]*(.*)$", 
                "<user-home>" );

        add( "jetty-port",                // Install*.log         jetty_port            = 4446
                "jetty_port[\\s]*=[\\s]*(.*)$", 
                "<jetty-port>" );

        add( "jetty-stop-port",           // Install*.log         jetty_port            = 4446
                "jetty_stop_port[\\s]*=[\\s]*(.*)$", 
                "<jetty-port>" );

        add( "jetty-user",                // Install*.log         jetty_user            = ap
                "jetty_user[\\s]*=[\\s](.*)$", 
                "<jetty-user>" );
        
        add( "jetty-user-home",           // Install*.log         jetty_user_home       = C:\Users\ap
                "jetty_user_home[\\s]*=[\\s]*(.*)$", 
                "<jetty-user-home>" );

        add( "db-user",                   // Install*.log         user                  = jobscheduler
                "user[\\s]*=[\\s](.*)$", 
                "<db-user>" );
        
        add( "connect-component",         // joc.log              Connect(apmacwin:4344   Connect apmacwin:4344
                "Connect[\\s\\(]*(.*):(\\d{3,5})", 
                "<host>:<port>" );

        add( "user-request",              // joc.log              USER: gauss@example.com
                "USER:[ ]*(.*)[\\s]*", 
                "<user-account>" );
        
        add( "user-login",                // controller.log       User:jobscheduler
                "User:(.*)[\\s]*",
                "<user-id>" );
        
        add( "agent-id",                  // controller.log
                "agent[\\s]*=[\\s]*(.*)", 
                "<agent-id>" );
        
    }

}
