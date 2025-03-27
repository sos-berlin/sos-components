package com.sos.yade.engine.commons.arguments.parsers.xml;

import java.nio.file.Path;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSEnvVariableReplacer;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.yade.engine.commons.arguments.parsers.AYADEParser;

public class YADEXMLParser extends AYADEParser {

    // private String jobResource;

    private SOSXMLXPath xpath;
    private Element root;
    private SOSEnvVariableReplacer envVarReplacer;

    public YADEXMLParser() {
        super();
    }

    @Override
    public void parse(Object... args) throws Exception {
        if (args == null || args.length != 2) {
            throw new SOSMissingDataException("settingsFile,profile");
        }
        if (args[0] == null || !(args[0] instanceof Path)) {
            throw new SOSMissingDataException("settingsFile");
        }
        if (args[1] == null || !(args[1] instanceof String)) {
            throw new SOSMissingDataException("profile");
        }

        getArgs().getSettings().setValue((Path) args[0]);
        getArgs().getProfile().setValue((String) args[1]);

        root = SOSXML.parse(getArgs().getSettings().getValue()).getDocumentElement();
        xpath = SOSXML.newXPath();

        Node profile = xpath.selectNode(root, "Profiles/Profile[@profile_id='" + getArgs().getProfile().getValue() + "']");
        if (profile == null) {
            throw new Exception("[profile=" + getArgs().getProfile().getValue() + "]not found");
        }

        envVarReplacer = new SOSEnvVariableReplacer(System.getenv(), false, true);
        YADEXMLProfileHelper.parse(this, profile);
    }

    protected Element getRoot() {
        return root;
    }

    protected SOSXMLXPath getXPath() {
        return xpath;
    }

    protected String getValue(Node node) {
        return envVarReplacer.replaceAllVars(SOSXML.getTrimmedValue(node));
    }

    protected void setIntegerArgumentValue(SOSArgument<Integer> arg, Node node) {
        arg.setValue(Integer.parseInt(getValue(node)));
    }

    protected void setBooleanArgumentValue(SOSArgument<Boolean> arg, Node node) {
        arg.setValue(Boolean.parseBoolean(getValue(node)));
    }

    protected void setOppositeBooleanArgumentValue(SOSArgument<Boolean> arg, Node node) {
        arg.setValue(!Boolean.parseBoolean(getValue(node)));
    }

    protected void setStringArgumentValue(SOSArgument<String> arg, Node node) {
        arg.setValue(getValue(node));
    }

    protected void setPathArgumentValue(SOSArgument<Path> arg, Node node) {
        arg.setValue(Path.of(getValue(node)));
    }
}
