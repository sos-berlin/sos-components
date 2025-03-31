package com.sos.yade.engine.commons.arguments.parsers.xml;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSMapVariableReplacer;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.yade.engine.commons.arguments.parsers.AYADEArgumentsSetter;
import com.sos.yade.engine.exceptions.YADEEngineSettingsParserException;

public class YADEXMLArgumentsSetter extends AYADEArgumentsSetter {

    // private String jobResource;

    private SOSXMLXPath xpath;
    private Element root;
    private SOSMapVariableReplacer varReplacer;

    public YADEXMLArgumentsSetter() {
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
    public YADEXMLArgumentsSetter set(ISOSLogger logger, Object... params) throws YADEEngineSettingsParserException {
        if (params == null || params.length != 5) {
            throw new YADEEngineSettingsParserException(
                    "missing settingsFile,profile,replacerMap,replaceCaseSensitive,replacerKeepUnresolvedVariables");
        }
        if (params[0] == null || !(params[0] instanceof Path)) {
            throw new YADEEngineSettingsParserException("missing settingsFile");
        }
        if (params[1] == null || !(params[1] instanceof String)) {
            throw new YADEEngineSettingsParserException("missing profile");
        }
        try {
            getArgs().getStart().setValue(Instant.now());

            getArgs().getSettings().setValue((Path) params[0]);
            getArgs().getProfile().setValue((String) params[1]);
            // params[2],params[3],params[4] see below varReplacer

            root = SOSXML.parse(getArgs().getSettings().getValue()).getDocumentElement();
            xpath = SOSXML.newXPath();

            Node profile = xpath.selectNode(root, "Profiles/Profile[@profile_id='" + getArgs().getProfile().getValue() + "']");
            if (profile == null) {
                throw new Exception("[profile=" + getArgs().getProfile().getValue() + "]not found");
            }

            // Map<String, String> map = System.getenv();
            varReplacer = new SOSMapVariableReplacer((Map<String, String>) params[2], (Boolean) params[3], (Boolean) params[4]);
            YADEXMLProfileHelper.parse(this, profile);
            if (logger.isDebugEnabled()) {
                logger.debug("[%s][set][duration]%s", YADEXMLArgumentsSetter.class.getSimpleName(), SOSDate.getDuration(getArgs().getStart()
                        .getValue(), Instant.now()));
            }
        } catch (Throwable e) {
            throw new YADEEngineSettingsParserException(e.toString(), e);
        } finally {
            root = null;
            xpath = null;
            varReplacer = null;
        }
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
