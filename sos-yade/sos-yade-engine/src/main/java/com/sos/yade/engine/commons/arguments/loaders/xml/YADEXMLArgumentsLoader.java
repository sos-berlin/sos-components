package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSMapVariableReplacer;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public class YADEXMLArgumentsLoader extends AYADEArgumentsLoader {

    // optional internal attribute to set a logging label on Source/Target when used as a Jump Host
    // e.g. LocalSource label="Jump"
    public static final String INTERNAL_ATTRIBUTE_LABEL = "label";

    private SOSXMLXPath xpath;
    private Element root;
    private SOSMapVariableReplacer varReplacer;

    public YADEXMLArgumentsLoader() {
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
    public YADEXMLArgumentsLoader load(ISOSLogger logger, Object... params) throws YADEEngineSettingsLoadException {
        if (params == null || params.length != 5) {
            throw new YADEEngineSettingsLoadException(
                    "missing settingsFile,profile,replacerMap,replaceCaseSensitive,replacerKeepUnresolvedVariables");
        }
        if (params[0] == null || !(params[0] instanceof Path)) {
            throw new YADEEngineSettingsLoadException("missing settingsFile");
        }
        if (params[1] == null || !(params[1] instanceof String)) {
            throw new YADEEngineSettingsLoadException("missing profile");
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
                throw new YADEEngineSettingsLoadException("[" + getArgs().getSettings().getValue() + "][profile=" + getArgs().getProfile().getValue()
                        + "]not found");
            }

            // Map<String, String> map = System.getenv();
            varReplacer = new SOSMapVariableReplacer((Map<String, String>) params[2], (Boolean) params[3], (Boolean) params[4]);
            YADEXMLGeneralHelper.parse(this, xpath.selectNode(root, "General"));
            YADEXMLProfileHelper.parse(this, profile);
            if (logger.isDebugEnabled()) {
                logger.debug("[%s][set][duration]%s", YADEEngineSettingsLoadException.class.getSimpleName(), SOSDate.getDuration(getArgs().getStart()
                        .getValue(), Instant.now()));
            }
        } catch (YADEEngineSettingsLoadException e) {
            throw e;
        } catch (Throwable e) {
            throw new YADEEngineSettingsLoadException(e.toString(), e);
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
        if (node == null) {
            return null;
        }
        return varReplacer.replaceAllVars(SOSXML.getTrimmedValue(node));
    }

    protected void setIntegerArgumentValue(SOSArgument<Integer> arg, Node node) {
        if (arg == null || node == null) {
            return;
        }
        arg.setValue(Integer.parseInt(getValue(node)));
    }

    protected void setBooleanArgumentValue(SOSArgument<Boolean> arg, Node node) {
        if (arg == null || node == null) {
            return;
        }
        arg.setValue(Boolean.parseBoolean(getValue(node)));
    }

    protected void setOppositeBooleanArgumentValue(SOSArgument<Boolean> arg, Node node) {
        if (arg == null || node == null) {
            return;
        }
        arg.setValue(!Boolean.parseBoolean(getValue(node)));
    }

    protected void setStringArgumentValue(SOSArgument<String> arg, Node node) {
        if (arg == null || node == null) {
            return;
        }
        arg.setValue(getValue(node));
    }

    protected void setListStringArgumentValue(SOSArgument<List<String>> arg, Node node) {
        if (arg == null || node == null) {
            return;
        }
        SOSArgumentHelper.setListStringArgumentValue(arg, getValue(node));
    }

    protected void setListPathArgumentValue(SOSArgument<List<Path>> arg, Node node) {
        if (arg == null || node == null) {
            return;
        }
        SOSArgumentHelper.setListPathArgumentValue(arg, getValue(node));
    }

    protected void setPathArgumentValue(SOSArgument<Path> arg, Node node) {
        if (arg == null || node == null) {
            return;
        }
        arg.setValue(Path.of(getValue(node)));
    }
}
