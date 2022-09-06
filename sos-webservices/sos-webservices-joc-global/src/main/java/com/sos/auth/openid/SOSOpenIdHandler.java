package com.sos.auth.openid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.openid.classes.SOSOpenIdAccountAccessToken;
import com.sos.commons.exception.SOSException;

public class SOSOpenIdHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdHandler.class);

    public SOSOpenIdHandler(SOSAuthCurrentAccount currentAccount) {
    }

    public SOSOpenIdAccountAccessToken login(SOSAuthCurrentAccount currentAccount) throws SOSException, JsonParseException, JsonMappingException, IOException {

        verifyAccessToken(currentAccount);
        
        return null;
    }

    private void verifyAccessToken(SOSAuthCurrentAccount currentAccount) {
        // TODO Auto-generated method stub
        
    }

    public boolean accountAccessTokenIsValid(SOSOpenIdAccountAccessToken accessToken) {
        // TODO Auto-generated method stub
        return false;
    }

    public SOSOpenIdAccountAccessToken renewAccountAccess(SOSOpenIdAccountAccessToken accessToken) {
        // TODO Auto-generated method stub
        return null;
    }

}
