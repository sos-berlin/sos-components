package com.sos.joc.encipherment.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.encipherment.resource.IEncrypt;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.encipherment.EncryptRequestFilter;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment")
public class EncryptImpl extends JOCResourceImpl implements IEncrypt {

    private static final String API_CALL = "./encipherment/encrypt";

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] encryptFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        initLogging(API_CALL, encryptFilter, xAccessToken);
        JsonValidator.validateFailFast(encryptFilter, EncryptRequestFilter.class);
        EncryptRequestFilter filter = Globals.objectMapper.readValue(encryptFilter, EncryptRequestFilter.class);
        JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getEncipherment().getEncrypt());
        if (jocDefaultResponse != null) {
            return jocDefaultResponse;
        }
        try {
        // TODO Auto-generated method stub
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocConcurrentAccessException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), xAccessToken, getJocError(), null);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
