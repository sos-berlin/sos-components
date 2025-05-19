package com.sos.joc.xmleditor.commons.standard;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.inventory.impl.StoreConfigurationResourceImpl;
import com.sos.joc.model.audit.CategoryType;
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
import com.sos.joc.xmleditor.impl.StandardYADEDeployResourceImpl;

public class StandardYADEJobResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardYADEJobResourceHandler.class);

    // XML already validated - so not empty etc
    public static void storeAndDeploy(final StandardYADEDeployResourceImpl impl, final String accessToken, final DeployConfiguration in, Document doc)
            throws Exception {
        DBItemInventoryConfiguration item = store(accessToken, in, doc);
        deploy(impl, accessToken, in, item);
    }

    private static DBItemInventoryConfiguration store(final String accessToken, final DeployConfiguration in, Document doc) throws Exception {
        // 1) XML - find JobResource Node (schema - JobResource - optional)
        Node jobResourceNode = SOSXML.getChildNode(doc.getDocumentElement(), "JobResource");
        if (jobResourceNode == null) {
            throw new SOSMissingDataException("[Configurations/JobResource]No JobResource configured for deployment");
        }

        // 1.1) XML - evaluate JobResource attributes (schema - JobResource - all attributes are required)
        String name = SOSXML.getAttributeValue(jobResourceNode, "name");
        String variable = SOSString.trimStart(SOSXML.getAttributeValue(jobResourceNode, "variable"), "$");
        String environmentVariable = SOSXML.getAttributeValue(jobResourceNode, "environment_variable");

        // 2) Database - find JobResource in the Inventory table
        // TODO check if it should be created if not exists ...
        DBItemInventoryConfiguration item = getYADEJobResourceFromInventory(name);
        if (item == null) {
            throw new SOSMissingDataException("[JobResource=" + name + "]JobResource not found in the Inventory");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[store]JobResource=" + name + ", variable=" + variable + ", environmentVariable=" + environmentVariable);
        }

        // 3) call JOC API inventory/store
        callJOCAPIInventoryStore(accessToken, item.getId(), variable, environmentVariable, in.getConfiguration());
        return item;
    }

    private static void deploy(final StandardYADEDeployResourceImpl impl, final String accessToken, final DeployConfiguration in,
            DBItemInventoryConfiguration item) throws Exception {

        Configuration configuration = new Configuration();
        configuration.setObjectType(ConfigurationType.JOBRESOURCE);
        configuration.setPath(item.getPath());

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
            DBItemJocAuditLog dbAuditlog = impl.storeAuditLog(filter.getAuditLog(), CategoryType.DEPLOYMENT);
            impl.deploy(accessToken, filter, session, dbAuditlog, impl.getAccount(), Globals.getJocSecurityLevel(), ADeploy.API_CALL);
            session.commit();
        } catch (Exception e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static void callJOCAPIInventoryStore(final String accessToken, final Long inventoryId, final String variable,
            final String environmentVariable, final String xml) throws Exception {

        // 1) Prepare JOC API call inventory/store - create filter
        JobResource jr = new JobResource();
        Environment args = new Environment();
        args.setAdditionalProperty(variable, "toFile( '" + StandardSchemaHandler.getYADEXMLForDeployment(xml) + "', '*.xml' )");
        jr.setArguments(args);
        args = new Environment();
        args.setAdditionalProperty(environmentVariable, "$" + variable);
        jr.setEnv(args);

        ConfigurationObject co = new ConfigurationObject();
        co.setObjectType(ConfigurationType.JOBRESOURCE);
        co.setId(inventoryId);
        co.setValid(true);
        co.setConfiguration(jr);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[callJOCAPIInventoryStore]" + Globals.objectMapper.writeValueAsString(co));
        }

        // 2) JOC API call inventory/store
        JOCDefaultResponse response = new StoreConfigurationResourceImpl().store(accessToken, Globals.objectMapper.writeValueAsBytes(co));
        checkResponse(response);
    }

    private static void checkResponse(JOCDefaultResponse response) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[checkResponse]" + response);
        }

        if (response.getStatus() != 200) {
            StringBuilder msg = new StringBuilder();
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
            throw new Exception("[" + StoreConfigurationResourceImpl.IMPL_PATH + "]" + msg);
        }
    }

    private static DBItemInventoryConfiguration getYADEJobResourceFromInventory(String jobResourceName) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("getYADEJobResourceFromInventory-" + jobResourceName);
            InventoryDBLayer inv = new InventoryDBLayer(session);

            session.beginTransaction();
            List<DBItemInventoryConfiguration> items = inv.getConfigurationByName(jobResourceName, ConfigurationType.JOBRESOURCE.intValue());
            Globals.commit(session);

            if (items.size() == 0) {
                return null;
            }
            return items.get(0);
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

}
