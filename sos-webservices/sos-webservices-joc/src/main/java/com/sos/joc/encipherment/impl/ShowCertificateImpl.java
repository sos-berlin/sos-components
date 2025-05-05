package com.sos.joc.encipherment.impl;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.encipherment.DBItemEncCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IShowCertificate;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.encipherment.EncCertificate;
import com.sos.joc.model.encipherment.ShowCertificateRequestFilter;
import com.sos.joc.model.encipherment.ShowCertificateResponse;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment/certificate")
public class ShowCertificateImpl extends JOCResourceImpl implements IShowCertificate {

    private static final String API_CALL = "./encipherment/certificate";

    @Override
    public JOCDefaultResponse postShowCertificate(String xAccessToken, byte[] showCertificateFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, showCertificateFilter, xAccessToken);
            ShowCertificateRequestFilter filter = null;
            if (showCertificateFilter != null && showCertificateFilter.length > 0) {
                JsonValidator.validateFailFast(showCertificateFilter, ShowCertificateRequestFilter.class);
                filter = Globals.objectMapper.readValue(showCertificateFilter, ShowCertificateRequestFilter.class);
            } else {
                filter = new ShowCertificateRequestFilter();
            }
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            
            Function<DBItemEncCertificate, EncCertificate> mapper = item -> {
                EncCertificate jsonItem = new EncCertificate();
                jsonItem.setCertAlias(item.getAlias());
                jsonItem.setCertificate(item.getCertificate());
                jsonItem.setPrivateKeyPath(item.getPrivateKeyPath());
                return jsonItem;
            };
            
            ShowCertificateResponse response = new ShowCertificateResponse();
            response.setCertificates(dbLayer.getEnciphermentCertificates(filter.getCertAliases()).stream().filter(Objects::nonNull).map(mapper)
                    .collect(Collectors.toList()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
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
