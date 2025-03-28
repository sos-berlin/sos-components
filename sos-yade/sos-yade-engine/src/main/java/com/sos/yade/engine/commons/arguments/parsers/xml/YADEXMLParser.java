package com.sos.yade.engine.commons.arguments.parsers.xml;

import java.nio.file.Path;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSMapVariableReplacer;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.yade.engine.commons.arguments.parsers.AYADEParser;

public class YADEXMLParser extends AYADEParser {

    // private String jobResource;

    private SOSXMLXPath xpath;
    private Element root;
    private SOSMapVariableReplacer varReplacer;

    public YADEXMLParser() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    /** Parameters:<br/>
     * - 1) Path settings<br/>
     * - 2) String profile<br/>
     * - 3) Map<String,String> replacerMap<br/>
     * - 4) boolean replacerCaseSensitive<br/>
     * - 5) boolean replacerKeepUnresolved<br/>
     */
    public YADEXMLParser parse(Object... args) throws Exception {
        if (args == null || args.length != 5) {
            throw new SOSMissingDataException("settingsFile,profile,replacerMap,replaceCaseSensitive,replacerKeepUnresolvedVariables");
        }
        if (args[0] == null || !(args[0] instanceof Path)) {
            throw new SOSMissingDataException("settingsFile");
        }
        if (args[1] == null || !(args[1] instanceof String)) {
            throw new SOSMissingDataException("profile");
        }
        // args[2],args[3],args[4] see below varReplacer

        getArgs().getSettings().setValue((Path) args[0]);
        getArgs().getProfile().setValue((String) args[1]);

        root = SOSXML.parse(getArgs().getSettings().getValue()).getDocumentElement();
        xpath = SOSXML.newXPath();

        Node profile = xpath.selectNode(root, "Profiles/Profile[@profile_id='" + getArgs().getProfile().getValue() + "']");
        if (profile == null) {
            throw new Exception("[profile=" + getArgs().getProfile().getValue() + "]not found");
        }

        // Map<String, String> map = System.getenv();
        varReplacer = new SOSMapVariableReplacer((Map<String, String>) args[2], (Boolean) args[3], (Boolean) args[4]);
        YADEXMLProfileHelper.parse(this, profile);
        return this;
    }

    protected Element getRoot() {
        return root;
    }

    protected SOSXMLXPath getXPath() {
        return xpath;
    }

    protected String getValue(Node node) {
        return varReplacer.replaceAllVars(SOSXML.getTrimmedValue(node));
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
