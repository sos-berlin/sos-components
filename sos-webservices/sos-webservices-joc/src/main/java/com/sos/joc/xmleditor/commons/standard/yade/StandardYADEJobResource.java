package com.sos.joc.xmleditor.commons.standard.yade;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;

public class StandardYADEJobResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardYADEJobResource.class);

    private final String name;
    private final String variable;
    private final String environmentVariable;
    private final DBItemInventoryConfiguration inventoryItem;

    private StandardYADEJobResource(String name, String variable, String environmentVariable, DBItemInventoryConfiguration inventoryItem) {
        this.name = name;
        this.variable = variable;
        this.environmentVariable = environmentVariable;
        this.inventoryItem = inventoryItem;
    }

    public static StandardYADEJobResource get(Document doc) throws Exception {
        // 1) XML - find JobResource Node (schema - JobResource - optional)
        Node jobResourceNode = SOSXML.getChildNode(doc.getDocumentElement(), "JobResource");
        if (jobResourceNode == null) {
            return null;
        }

        // 1.1) XML - evaluate JobResource attributes (schema - JobResource - all attributes are required)
        String name = SOSXML.getAttributeValue(jobResourceNode, "name");
        String variable = SOSString.trimStart(SOSXML.getAttributeValue(jobResourceNode, "variable"), "$");
        String environmentVariable = SOSXML.getAttributeValue(jobResourceNode, "environment_variable");

        // 2) Database - find JobResource in the Inventory table
        return new StandardYADEJobResource(name, variable, environmentVariable, getYADEJobResourceFromInventory(name));
    }

    public String getDeployedXMLFromInventory() {
        if (inventoryItem == null || !inventoryItem.getDeployed()) {
            return null;
        }
        String inventoryXML = null;
        try {
            JobResource jr = Globals.objectMapper.readValue(inventoryItem.getContent(), JobResource.class);
            if (jr == null) {
                return null;
            }
            String variableValue = jr.getArguments().getAdditionalProperties().get(variable);
            if (variableValue == null) {
                return null;
            }
            inventoryXML = extractXml(variableValue);
            return StandardSchemaHandler.getXML(inventoryXML, true);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn("[getDeployedXMLFromInventory][inventoryXML]" + inventoryXML);
                LOGGER.warn("[getDeployedXMLFromInventory]" + e, e);
            }
            return null;
        }
    }

    public void tryUpdateReleasedConfigurationIfJobResourceDeployed(DBItemXmlEditorConfiguration item) {
        String inventoryXML = getDeployedXMLFromInventory();
        if (inventoryXML == null) {
            return;
        }

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("tryUpdateReleasedConfigurationIfJobResourceDeployed-" + item.getId());
            session.beginTransaction();

            item.setConfigurationDraft(null);
            item.setConfigurationDraftJson(null);
            item.setConfigurationReleased(inventoryXML);
            item.setConfigurationReleasedJson(null);
            item.setAuditLogId(inventoryItem.getAuditLogId());
            // item.setAccount(account);
            item.setModified(new Date());
            item.setReleased(inventoryItem.getModified());

            session.update(item);
            session.commit();
        } catch (Throwable e) {
            Globals.rollback(session);
        } finally {
            Globals.disconnect(session);
        }
    }

    // String variableValue = "toFile( '<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n<Configurations>...</Configurations>', '*.xml' )";
    private static String extractXml(String variableValue) {
        int start = variableValue.indexOf('<'); // first <
        int quoteEnd = variableValue.lastIndexOf('\''); // last '
        int end = variableValue.lastIndexOf('>', quoteEnd); // last > before '
        if (start == -1 || end == -1 || end <= start) {
            return null;
        }
        String xml = variableValue.substring(start, end + 1);
        return xml.replace("\\r\\n", "\r\n").replace("\\\"", "\"").trim();
    }

    public String getName() {
        return name;
    }

    public String getVariable() {
        return variable;
    }

    public String getEnvironmentVariable() {
        return environmentVariable;
    }

    public DBItemInventoryConfiguration getInventoryItem() {
        return inventoryItem;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(StandardYADEJobResource.class.getSimpleName());
        sb.append(" name=").append(name);
        sb.append(", variable=").append(variable);
        sb.append(", environmentVariable=").append(environmentVariable);
        if (inventoryItem != null) {
            sb.append(", inventoryItem.id=").append(inventoryItem.getId());
        }
        return sb.toString();
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
