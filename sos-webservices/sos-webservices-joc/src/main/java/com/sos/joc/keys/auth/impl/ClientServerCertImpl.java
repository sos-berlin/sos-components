package com.sos.joc.keys.auth.impl;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.auth.resource.ICreateClientServerCert;
import com.sos.joc.keys.auth.token.OnetimeTokens;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.auth.token.OnetimeToken;
import com.sos.joc.model.publish.CreateCSRFilter;
import com.sos.joc.model.publish.RolloutResponse;
import com.sos.joc.model.publish.rollout.items.JocConf;
import com.sos.joc.publish.util.ClientServerCertificateUtil;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("authentication")
public class ClientServerCertImpl extends JOCResourceImpl implements ICreateClientServerCert {

    private static String API_CALL = "./authentication/certificate/create";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientServerCertImpl.class);

    @Override
    public JOCDefaultResponse postCreateClientServerCert(String token, byte[] filter) {
        SOSHibernateSession hibernateSession = null;
        try {
            filter = initLogging(API_CALL, filter, CategoryType.CERTIFICATES);
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
                    LOGGER.debug("agentId: " + onetimeToken.getAgentId());
                    LOGGER.debug("controllerId: " + onetimeToken.getControllerId());
                    if (createCsrFilter.getDnOnly()) {
                        response.setDNs(getDnsForControllerAgents(onetimeToken, controllerDbLayer, agentDbLayer));
                        response.setJocConfs(getJocConfs(new JocInstancesDBLayer(hibernateSession)));
                    } else {
                        response = ClientServerCertificateUtil.createClientServerAuthKeyPair(hibernateSession, createCsrFilter);
                        if (onetimeToken.getAgentId() != null) {
                            DBItemInventoryAgentInstance agent = agentDbLayer.getAgentInstance(onetimeToken.getAgentId());
                            agent.setCertificate(response.getJocKeyPair().getCertificate());
                            agent.setModified(Date.from(Instant.now()));
                            hibernateSession.update(agent);
                            response.setControllerId(agent.getControllerId());
                            response.setAgentId(onetimeToken.getAgentId());
                        } else if (onetimeToken.getControllerId() != null) {
                            List<DBItemInventoryJSInstance> controllers = controllerDbLayer.getInventoryInstancesByControllerId(onetimeToken.getControllerId());
                            if (controllers.size() == 1) { //standalone
                                controllers.get(0).setCertificate(response.getJocKeyPair().getCertificate());
                                controllers.get(0).setModified(Date.from(Instant.now()));
                                hibernateSession.update(controllers.get(0));
                            } else { // cluster
                                for (DBItemInventoryJSInstance controller : controllers) {
                                    if(controller.getClusterUri() != null && controller.getClusterUri().equals(onetimeToken.getURI())) {
                                        controller.setCertificate(response.getJocKeyPair().getCertificate());
                                        controller.setModified(Date.from(Instant.now()));
                                        hibernateSession.update(controller);
                                    }
                                }
                            }
                            response.setControllerId(onetimeToken.getControllerId());
                            response.setAgentId(null);
                        }
                        response.setDNs(getDnsForControllerAgents(onetimeToken, controllerDbLayer, agentDbLayer));
                        response.setJocConfs(getJocConfs(new JocInstancesDBLayer(hibernateSession)));
                    }
                } else {
                    throw new JocAuthenticationException(String.format("One-time token %1$s couldn't find or has expired and was removed from the system.", token));
                }
            } else {
                throw new JocAuthenticationException("No valid one-time token(s) found!");
            }
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(response));
        } catch (Exception e) {
            return responseStatusJSError(e);
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

    private List<String> getDnsForControllerAgents(OnetimeToken token, InventoryInstancesDBLayer controllerDbLayer, InventoryAgentInstancesDBLayer agentDbLayer)
            throws SOSHibernateException {
        List<String> dNs = new ArrayList<String>();
        if (token.getAgentId() != null) {
            DBItemInventoryAgentInstance agent = agentDbLayer.getAgentInstance(token.getAgentId());
            List<DBItemInventoryJSInstance> controllers = controllerDbLayer.getInventoryInstancesByControllerId(agent.getControllerId());
            dNs =  controllers.stream().map(controller -> {
                try {
                    return KeyUtil.getX509Certificate(controller.getCertificate());
                } catch (CertificateException | UnsupportedEncodingException e) {
                    return null;
                }
            }).filter(Objects::nonNull).map(cert -> cert.getSubjectX500Principal().getName()).collect(Collectors.toList());
        } else if (token.getControllerId() != null) {
            List<DBItemInventoryJSInstance> controllers = controllerDbLayer.getInventoryInstancesByControllerId(token.getControllerId());
            for (DBItemInventoryJSInstance controller : controllers) {
                if(!controller.getUri().equals(token.getURI())) {
                    try {
                        if(controller.getCertificate() != null && !controller.getCertificate().isEmpty()) {
                            X509Certificate cert = KeyUtil.getX509Certificate(controller.getCertificate());
                            dNs.add(cert.getSubjectX500Principal().getName());
                        }
                    } catch (CertificateException | UnsupportedEncodingException e) {}
                }
            }
        }
        return dNs;
    }
    
    private List<JocConf> getJocConfs(JocInstancesDBLayer dbLayer) throws CertificateEncodingException, CertificateException, UnsupportedEncodingException {
        return dbLayer.getInstances().stream().map(jocInstance -> {
            JocConf conf = new JocConf();
                conf.setJocId(jocInstance.getClusterId());
                conf.setUrl(jocInstance.getUri());
                try {
                    X509Certificate cert = KeyUtil.getX509Certificate(jocInstance.getCertificate());
                    String dn = CertificateUtils.getDistinguishedName(cert);
                    conf.setDN(dn);
                } catch (CertificateException | UnsupportedEncodingException e) {
                    throw new JocException(e);
                }
            return conf;
        }).collect(Collectors.toList());
    }
    
}
