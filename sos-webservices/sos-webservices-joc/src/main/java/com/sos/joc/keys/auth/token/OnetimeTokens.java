package com.sos.joc.keys.auth.token;

import java.util.concurrent.CopyOnWriteArraySet;

import com.sos.joc.model.auth.token.OnetimeToken;

public class OnetimeTokens {
    
    private static OnetimeTokens onetimeTokens;
    private CopyOnWriteArraySet<OnetimeToken> tokens = new CopyOnWriteArraySet<>();

    private OnetimeTokens() {}
    
    public static synchronized OnetimeTokens getInstance() {
        if (onetimeTokens == null) {
            onetimeTokens = new OnetimeTokens(); 
        }
        return onetimeTokens;
    }
    
    public synchronized CopyOnWriteArraySet<OnetimeToken> getTokens () {
        return tokens;
    }

}
