package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSMapVariableReplacer;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXML.SOSXMLXPath;
import com.sos.commons.xml.exception.SOSXMLDoctypeException;
import com.sos.yade.engine.commons.YADEReturnCode;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.exceptions.YADEEngineSettingsLoadException;

public class YADEXMLArgumentsLoader extends AYADEArgumentsLoader {

    // optional internal attribute to set a logging label on Source/Target when used as a Jump Host
    // e.g. LocalSource label="Jump"
    public static final String INTERNAL_ATTRIBUTE_LABEL = "label";

    private SOSXMLXPath xpath;
    private Element root;

    public YADEXMLArgumentsLoader() {
        super();
    }

    @Override
    public YADEXMLArgumentsLoader load(ISOSLogger logger, Path settings, String profile, String alternativeProfile, Map<String, String> replacerMap,
            boolean replaceCaseSensitive, boolean replacerKeepUnresolvedVariables) throws YADEEngineSettingsLoadException {
        if (settings == null) {
            throw new YADEEngineSettingsLoadException("missing settings file", YADEReturnCode.MISSING_REQUIRED_ARGUMENT);
        }
        if (SOSString.isEmpty(profile)) {
            throw new YADEEngineSettingsLoadException("missing profile", YADEReturnCode.MISSING_REQUIRED_ARGUMENT);
        }

        try {
            getArgs().programStart();

            getArgs().getSettings().setValue(settings);
            getArgs().getProfile().setValue(profile);
            setVisitedProfile(getArgs().getProfile().getValue());

            root = SOSXML.parse(getArgs().getSettings().getValue()).getDocumentElement();
            xpath = SOSXML.newXPath();

            Node profileNode = xpath.selectNode(root, "Profiles/Profile[@profile_id='" + getArgs().getProfile().getValue() + "']");
            if (profile == null) {
                throw new YADEEngineSettingsLoadException("[" + getArgs().getSettings().getValue() + "][profile=" + getArgs().getProfile().getValue()
                        + "]not found", YADEReturnCode.PROFILE_NOT_FOUND);
            }

            setVarReplacer(new SOSMapVariableReplacer(replacerMap, replaceCaseSensitive, replacerKeepUnresolvedVariables));
            YADEXMLGeneralHelper.parse(logger, this, xpath.selectNode(root, "General"));
            YADEXMLProfileHelper.parse(logger, this, profileNode);

            // overrides the xml alternative profile if enables
            if (!SOSString.isEmpty(alternativeProfile)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[%s][load][alternative profile][configured=%s]use from argument=%s", YADEXMLArgumentsLoader.class.getSimpleName(),
                            getArgs().getAlternativeProfile().getValue(), alternativeProfile);
                }
                getArgs().getAlternativeProfile().setValue(alternativeProfile);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("[%s][load][duration]%s", YADEXMLArgumentsLoader.class.getSimpleName(), SOSDate.getDuration(getArgs().getProgramStart(),
                        Instant.now()));
            }
        } catch (YADEEngineSettingsLoadException e) {
            throw e;
        } catch (FileNotFoundException e) {
            throw new YADEEngineSettingsLoadException(e.toString(), e, YADEReturnCode.CONFIGURATION_FILE_NOT_FOUND);
        } catch (SOSXMLDoctypeException | SAXException | IOException e) {
            throw new YADEEngineSettingsLoadException(e.toString(), e, YADEReturnCode.CONFIGURATION_FILE_PARSE_ERROR);
        } catch (Exception e) {
            throw new YADEEngineSettingsLoadException(e.toString(), e, YADEReturnCode.CONFIGURATION_ERROR);
        } finally {
            clear();
        }
        return this;
    }

    /** Re-parses the XML configuration to load alternative profile. <br />
     * Reasons:
     * <ul>
     * <li>The DOM tree is released after the initial parsing to minimize memory usage.</li>
     * <li>Alternative profile(s) is only required if a connection error occurs for the current source or target profile.</li>
     * <li>Re-parsing is inexpensive because the XML file is local and remains small (even with around 1.000 profiles it is approximately 1 MB).</li>
     * <li>This avoids keeping the complete DOM tree in memory during normal execution.</li>
     * </ul>
     */
    public static YADEXMLArgumentsLoader loadAlternativeProfile(ISOSLogger logger, AYADEArgumentsLoader previousArgsLoader)
            throws YADEEngineSettingsLoadException {
        if (previousArgsLoader.getArgs().getAlternativeProfile().isEmpty()) {
            throw new YADEEngineSettingsLoadException("Missing " + YADEArguments.STARTUP_ARG_ALTERNATIVE_PROFILE,
                    YADEReturnCode.MISSING_REQUIRED_ARGUMENT);
        }
        if (previousArgsLoader.profileEqualsAlternativeProfile()) {
            previousArgsLoader.getArgs().getAlternativeProfile().setValue(null);
            throw new YADEEngineSettingsLoadException("The profile and the alternative profile are identical - " + previousArgsLoader.getArgs()
                    .getProfile().getValue(), YADEReturnCode.CONFIGURATION_ERROR);
        }

        YADEXMLArgumentsLoader argsLoader = new YADEXMLArgumentsLoader();

        argsLoader.getArgs().getSettings().setValue(previousArgsLoader.getArgs().getSettings().getValue());
        argsLoader.getArgs().getProfile().setValue(previousArgsLoader.getArgs().getAlternativeProfile().getValue());
        argsLoader.getArgs().getAlternativeProfile().setValue(null);
        argsLoader.setVisitedProfile(previousArgsLoader, argsLoader.getArgs().getProfile().getValue());

        try {
            argsLoader.getArgs().programStart();

            argsLoader.root = SOSXML.parse(argsLoader.getArgs().getSettings().getValue()).getDocumentElement();
            argsLoader.xpath = SOSXML.newXPath();

            Node profile = argsLoader.xpath.selectNode(argsLoader.root, "Profiles/Profile[@profile_id='" + argsLoader.getArgs().getProfile()
                    .getValue() + "']");
            if (profile == null) {
                throw new YADEEngineSettingsLoadException("[" + argsLoader.getArgs().getSettings().getValue() + "][profile=" + argsLoader.getArgs()
                        .getProfile().getValue() + "]not found", YADEReturnCode.PROFILE_NOT_FOUND);
            }

            argsLoader.setVarReplacer(previousArgsLoader.getVarReplacer());
            YADEXMLGeneralHelper.parse(logger, argsLoader, argsLoader.xpath.selectNode(argsLoader.root, "General"));
            YADEXMLProfileHelper.parse(logger, argsLoader, profile);

            if (logger.isDebugEnabled()) {
                logger.debug("[%s][load][duration]%s", YADEXMLArgumentsLoader.class.getSimpleName(), SOSDate.getDuration(argsLoader.getArgs()
                        .getProgramStart(), Instant.now()));
            }

        } catch (YADEEngineSettingsLoadException e) {
            throw e;
        } catch (FileNotFoundException e) {
            throw new YADEEngineSettingsLoadException(e.toString(), e, YADEReturnCode.CONFIGURATION_FILE_NOT_FOUND);
        } catch (SOSXMLDoctypeException | SAXException | IOException e) {
            throw new YADEEngineSettingsLoadException(e.toString(), e, YADEReturnCode.CONFIGURATION_FILE_PARSE_ERROR);
        } catch (Exception e) {
            throw new YADEEngineSettingsLoadException(e.toString(), e, YADEReturnCode.CONFIGURATION_ERROR);
        } finally {
            argsLoader.clear();
        }
        return argsLoader;
    }

    protected Element getRoot() {
        return root;
    }

    protected SOSXMLXPath getXPath() {
        return xpath;
    }

    protected String getValue(Node node, String attrName) {
        if (node == null) {
            return null;
        }
        String attrValue = SOSXML.getAttributeValue(node, attrName);
        if (attrValue == null) {
            return null;
        }
        return getVarReplacer().replaceAllVars(attrValue);
    }

    protected String getValue(Node node) {
        if (node == null) {
            return null;
        }
        return getVarReplacer().replaceAllVars(SOSXML.getTrimmedValue(node));
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

    private void clear() {
        root = null;
        xpath = null;
        if (getArgs().getAlternativeProfile().isEmpty()) {
            setVarReplacer(null);
            clearVisitedProfiles();
        }
    }
}
