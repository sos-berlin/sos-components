package com.sos.joc.xmleditor.commons.standard.yade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.inventory.impl.StoreConfigurationResourceImpl;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.Err420;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.model.xmleditor.deploy.DeployConfiguration;
import com.sos.joc.publish.impl.ADeploy;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.impl.StandardYADEDeployResourceImpl;

public class StandardYADEJobResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardYADEJobResourceHandler.class);

    // XML already validated - so not empty etc
    public static void storeAndDeploy(final StandardYADEDeployResourceImpl impl, final String accessToken, final DeployConfiguration in, Document doc)
            throws Exception {
        StandardYADEJobResource yadeJobResource = store(accessToken, in, doc);
        deploy(impl, accessToken, in, yadeJobResource);
    }

    private static StandardYADEJobResource store(final String accessToken, final DeployConfiguration in, Document doc) throws Exception {
        StandardYADEJobResource yadeJobResource = StandardYADEJobResource.get(doc);
        if (yadeJobResource == null) {
            throw new SOSMissingDataException("[Configurations/JobResource]No JobResource configured for deployment");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[store]YADEJobResource=" + yadeJobResource);
        }

        // call JOC API inventory/store
        callJOCAPIInventoryStore(accessToken, yadeJobResource, doc, in.getConfiguration());
        return yadeJobResource;
    }

    private static void callJOCAPIInventoryStore(final String accessToken, final StandardYADEJobResource yadeJobResource, final Document doc,
            final String xml) throws Exception {

        // 1) Prepare JOC API call inventory/store - create filter
        JobResource jr = null;
        if (yadeJobResource.getInventoryItem() == null) {// JobResource not exists in the inventory
            yadeJobResource.createNewJobResourceInventoryItem();
        } else {
            jr = Globals.objectMapper.readValue(yadeJobResource.getInventoryItem().getContent(), JobResource.class);
        }
        if (jr == null) {
            jr = new JobResource();
            jr.setTitle(yadeJobResource.getInventoryItem().getTitle());
            Environment args = new Environment();
            args.setAdditionalProperty(yadeJobResource.getVariable(), "toFile( '" + StandardSchemaHandler.getYADEXMLForDeployment(doc, xml)
                    + "', '*.xml' )");
            jr.setArguments(args);

            Environment env = new Environment();
            env.setAdditionalProperty(yadeJobResource.getEnvironmentVariable(), "$" + yadeJobResource.getVariable());
            jr.setEnv(env);
        } else {
            // change only a variable not the entire jobresource
            Environment args = jr.getArguments();
            if (args == null || args.getAdditionalProperties() == null) {
                jr.setArguments(new Environment());
            }
            jr.getArguments().getAdditionalProperties().put(yadeJobResource.getVariable(), "toFile( '" + StandardSchemaHandler
                    .getYADEXMLForDeployment(doc, xml) + "', '*.xml' )");

            Environment env = jr.getEnv();
            if (env == null || env.getAdditionalProperties() == null) {
                jr.setEnv(new Environment());
            }
            jr.getEnv().getAdditionalProperties().put(yadeJobResource.getEnvironmentVariable(), "$" + yadeJobResource.getVariable());
        }

        ConfigurationObject co = new ConfigurationObject();
        co.setObjectType(ConfigurationType.JOBRESOURCE);
        if (yadeJobResource.getInventoryItem().getId() == null) {
            co.setPath(yadeJobResource.getInventoryItem().getPath());
        } else {
            co.setId(yadeJobResource.getInventoryItem().getId());
        }
        co.setValid(true);
        co.setConfiguration(jr);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[callJOCAPIInventoryStore]" + Globals.objectMapper.writeValueAsString(co));
        }

        // 2) JOC API call inventory/store
        JOCDefaultResponse response = new StoreConfigurationResourceImpl().store(accessToken, Globals.objectMapper.writeValueAsBytes(co));
        checkResponse(response);
    }

    private static void deploy(final StandardYADEDeployResourceImpl impl, final String accessToken, final DeployConfiguration in,
            StandardYADEJobResource yadeJobResource) throws Exception {

        Configuration configuration = new Configuration();
        configuration.setObjectType(ConfigurationType.JOBRESOURCE);
        configuration.setPath(yadeJobResource.getInventoryItem().getPath());

        Config config = new Config();
        config.setConfiguration(configuration);
        DeployablesValidFilter validFilter = new DeployablesValidFilter();
        validFilter.getDraftConfigurations().add(config);
        DeployFilter filter = new DeployFilter();
        filter.setStore(validFilter);
        filter.setControllerIds(in.getControllerIds());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[deploy]" + Globals.objectMapper.writeValueAsString(filter));
        }

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("deploy");
            session.beginTransaction();
            DBItemJocAuditLog dbAuditlog = impl.storeAuditLog(filter.getAuditLog());
            impl.deploy(accessToken, filter, session, dbAuditlog, impl.getAccount(), Globals.getJocSecurityLevel(), ADeploy.API_CALL);
            session.commit();
        } catch (Exception e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static void checkResponse(JOCDefaultResponse response) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[checkResponse]" + (response.getEntity() == null ? response : response.getEntity()));
        }

        if (response.getStatus() != 200) {
            StringBuilder msg = new StringBuilder();
            if (response.getEntity() == null) {
                msg.append(response);
            } else {
                if (response.getEntity() instanceof Err420) {
                    Err420 err = (Err420) response.getEntity();
                    msg.append("[").append(err.getClass().getSimpleName()).append("]");
                    msg.append("[").append(err.getError().getCode()).append("]");
                    msg.append(err.getError().getMessage());

                } else if (response.getEntity() instanceof Err) {
                    Err err = (Err) response.getEntity();
                    msg.append("[").append(err.getClass().getSimpleName()).append("]");
                    msg.append("[").append(err.getCode()).append("]");
                    msg.append(err.getMessage());
                } else {
                    msg.append(response.getEntity());
                }
            }
            throw new Exception("[" + StoreConfigurationResourceImpl.IMPL_PATH + "]" + msg);
        }
    }

}
