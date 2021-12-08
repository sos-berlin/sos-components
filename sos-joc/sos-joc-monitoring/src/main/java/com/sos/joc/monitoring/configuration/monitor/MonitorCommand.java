package com.sos.joc.monitoring.configuration.monitor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.notification.notifier.NotifierCommand;
import com.sos.monitoring.MonitorType;

public class MonitorCommand extends AMonitor {

    private static final String ELEMENT_NAME_COMMAND = "Command";

    private final String command;

    public MonitorCommand(Document doc, Node node) throws Exception {
        super(doc, node);

        this.command = resolveCommand();
    }

    @Override
    public NotifierCommand createNotifier(int nr, Configuration conf) throws Exception {
        return new NotifierCommand(nr, this);
    }

    @Override
    public MonitorType getType() {
        return MonitorType.COMMAND;
    }

    private String resolveCommand() throws Exception {
        Node cmd = SOSXML.getChildNode(getRefElement(), ELEMENT_NAME_COMMAND);
        if (cmd == null) {
            throw new Exception(String.format("[%s name=%s]missing child element \"%s\"", getElement().getNodeName(), getElement().getAttribute(
                    "name"), ELEMENT_NAME_COMMAND));
        }
        String command = SOSXML.getTrimmedValue(cmd);
        if (SOSString.isEmpty(command)) {
            throw new Exception(String.format("[%s name=%s][%s]missing value", getElement().getNodeName(), getElement().getAttribute("name"),
                    ELEMENT_NAME_COMMAND));
        }
        return resolveMessage(command);
    }

    public String getCommand() {
        return command;
    }

    private String resolveMessage(String command) throws Exception {
        try {
            SOSParameterSubstitutor ps = new SOSParameterSubstitutor(false, "${", "}");
            ps.addKey("MESSAGE", getMessage());
            return ps.replace(command);
        } catch (Throwable e) {
            throw new Exception(String.format("[\"{$MESSAGE}\" in \"command\" can't be substituted][message=%s][command=%s]%s", getMessage(), command,
                    e.toString()), e);
        }
    }

}
