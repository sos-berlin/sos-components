package com.sos.joc.notification.configuration.monitor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;

public class MonitorCommand extends AMonitor {

    private static final String ELEMENT_NAME_COMMAND = "Command";

    private String command = null;

    public MonitorCommand(Document doc, Node node) throws Exception {
        super(doc, node);

        setCommand();
    }

    private void setCommand() throws Exception {
        Node cmd = SOSXML.getChildNode(getRefElement(), ELEMENT_NAME_COMMAND);
        if (cmd == null) {
            throw new Exception(String.format("[%s name=%s]missing child element \"%s\"", getElement().getNodeName(), getElement().getAttribute(
                    "name"), ELEMENT_NAME_COMMAND));
        }
        command = SOSXML.getTrimmedValue(cmd);
        if (SOSString.isEmpty(command)) {
            throw new Exception(String.format("[%s name=%s][%s]missing value", getElement().getNodeName(), getElement().getAttribute("name"),
                    ELEMENT_NAME_COMMAND));
        }
        command = resolveMessage();
    }

    public String getCommand() {
        return command;
    }

    private String resolveMessage() {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor(true, "${", "}");
        ps.addKey("MESSAGE", getMessage());
        return ps.replace(command);
    }

}
