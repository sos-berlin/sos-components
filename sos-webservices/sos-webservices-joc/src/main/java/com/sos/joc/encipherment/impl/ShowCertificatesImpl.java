package com.sos.joc.encipherment.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.encipherment.DBItemEncCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IShowCertificates;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.encipherment.EncCertificate;
import com.sos.joc.model.encipherment.ShowCertificateResponse;

@jakarta.ws.rs.Path("encipherment")
public class ShowCertificatesImpl extends JOCResourceImpl implements IShowCertificates {

    private static final String API_CALL = "./encipherment/certificates";

    @Override
    public JOCDefaultResponse postShowCertificates(String xAccessToken) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, null, xAccessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            List<DBItemEncCertificate> items = dbLayer.getAllEnciphermentCertificates();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(mapDBitemsToJson(items)));
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
    
    private ShowCertificateResponse mapDBitemsToJson (List<DBItemEncCertificate> dbCertificates) {
        ShowCertificateResponse response = new ShowCertificateResponse();
        dbCertificates.stream().filter(Objects::nonNull).map(item -> {
            EncCertificate jsonItem = new EncCertificate();
            jsonItem.setCertAlias(item.getAlias());
            jsonItem.setCertificate(item.getCertificate());
            jsonItem.setPrivateKeyPath(item.getPrivateKeyPath());
            return jsonItem;
        }).collect(Collectors.toList());
        return response;
    }
}
