package com.sos.joc.keys.auth.impl;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.auth.resource.ICreateClientServerCert;
import com.sos.joc.keys.auth.token.OnetimeTokens;
import com.sos.joc.model.auth.token.OnetimeToken;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.RolloutResponse;
import com.sos.joc.publish.util.ClientServerCertificateUtil;
import com.sos.schema.JsonValidator;

@Path("authentication")
public class ClientServerCertImpl extends JOCResourceImpl implements ICreateClientServerCert {

    private static String API_CALL = "./authentication/certificate/create";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientServerCertImpl.class);

    @Override
    public JOCDefaultResponse postCreateClientServerCert(String token, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter);
            JsonValidator.validateFailFast(filter, CreateCSRFilter.class);
            CreateCSRFilter createCsrFilter = Globals.objectMapper.readValue(filter, CreateCSRFilter.class);
            ClientServerCertificateUtil.cleanupInvalidatedTokens();
            OnetimeTokens onetimeTokens = OnetimeTokens.getInstance();
            OnetimeToken onetimeToken = null;
            RolloutResponse response = new RolloutResponse();
            if (onetimeTokens != null && !onetimeTokens.getTokens().isEmpty()) {
                Optional<OnetimeToken> optional = onetimeTokens.getTokens().stream().filter(item -> item.getUUID().equals(token)).filter(Objects::nonNull)
                        .findFirst();
                if (optional.isPresent()) {
                    onetimeToken = optional.get();
                }
                if (onetimeToken != null) {
                    hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                    InventoryInstancesDBLayer controllerDbLayer = new InventoryInstancesDBLayer(hibernateSession);
                    InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
                    LOGGER.info("agentId: " + onetimeToken.getAgentId());
                    LOGGER.info("controllerId: " + onetimeToken.getControllerId());
                    if (createCsrFilter.getDnOnly()) {
                        response.setDNs(getDns(onetimeToken, controllerDbLayer, agentDbLayer));
                    } else {
                        response = ClientServerCertificateUtil.createClientServerAuthKeyPair(hibernateSession, createCsrFilter);
                        if (onetimeToken.getAgentId() != null) {
                            DBItemInventoryAgentInstance agent = agentDbLayer.getAgentInstance(onetimeToken.getAgentId());
                            agent.setCertificate(response.getJocKeyPair().getCertificate());
                            hibernateSession.update(agent);
                        } else if (onetimeToken.getControllerId() != null) {
                            List<DBItemInventoryJSInstance> controllers = controllerDbLayer.getInventoryInstancesByControllerId(onetimeToken.getControllerId());
                            if (controllers.size() == 1) { //standalone
                                controllers.get(0).setCertificate(response.getJocKeyPair().getCertificate());
                                hibernateSession.update(controllers.get(0));
                            } else { // cluster
                                for (DBItemInventoryJSInstance controller : controllers) {
                                    if(controller.getClusterUri() != null && controller.getClusterUri().equals(onetimeToken.getURI())) {
                                        controller.setCertificate(response.getJocKeyPair().getCertificate());
                                        hibernateSession.update(controller);
                                    }
                                }
                            }
                            response.setDNs(getDns(onetimeToken, controllerDbLayer, agentDbLayer));
                        }
                    }
                } else {
                    throw new JocAuthenticationException(String.format("One-time token %1$s couldn't find or has expired and was removed from the system.", token));
                }
            } else {
                throw new JocAuthenticationException("No valid one-time token(s) found!");
            }
            response.setControllerId(onetimeToken.getControllerId());
            response.setAgentId(onetimeToken.getAgentId());
            return JOCDefaultResponse.responseStatus200(response);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            OnetimeTokens onetimeTokens = OnetimeTokens.getInstance();
            Optional<OnetimeToken> optional = onetimeTokens.getTokens().stream().filter(item -> item.getUUID().equals(token)).filter(Objects::nonNull)
                    .findFirst();
            if (optional.isPresent()) {
                OnetimeToken onetimeToken = optional.get();
                onetimeTokens.getTokens().remove(onetimeToken);
            }
            Globals.disconnect(hibernateSession);
        }
    }

    private List<String> getDns(OnetimeToken token, InventoryInstancesDBLayer controllerDbLayer, InventoryAgentInstancesDBLayer agentDbLayer)
            throws SOSHibernateException {
        List<String> dNs = new ArrayList<String>();
        if (token.getAgentId() != null) {
            DBItemInventoryAgentInstance agent = agentDbLayer.getAgentInstance(token.getAgentId());
            List<DBItemInventoryJSInstance> controllers = controllerDbLayer.getInventoryInstancesByControllerId(agent.getControllerId());
            List<X509Certificate> certs = controllers.stream().map(controller -> {
                try {
                    return KeyUtil.getX509Certificate(controller.getCertificate());
                } catch (CertificateException | UnsupportedEncodingException e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            dNs = certs.stream().map(cert -> cert.getSubjectDN().getName()).collect(Collectors.toList());
        } else if (token.getControllerId() != null) {
            List<DBItemInventoryJSInstance> controllers = controllerDbLayer.getInventoryInstancesByControllerId(token.getControllerId());
            for (DBItemInventoryJSInstance controller : controllers) {
                if(!controller.getUri().equals(token.getURI())) {
                    try {
                        if(controller.getCertificate() != null && !controller.getCertificate().isEmpty()) {
                            X509Certificate cert = KeyUtil.getX509Certificate(controller.getCertificate());
                            dNs.add(cert.getSubjectDN().getName());
                        }
                    } catch (CertificateException | UnsupportedEncodingException e) {}
                }
            }
        }
        return dNs;
    }
}
