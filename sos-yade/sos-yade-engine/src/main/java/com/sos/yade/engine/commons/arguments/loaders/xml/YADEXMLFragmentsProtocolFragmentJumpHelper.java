package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;

public class YADEXMLFragmentsProtocolFragmentJumpHelper {

    private static Set<String> VISITED_ALTERNATIVES = new HashSet<>();

    protected static void parse(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = YADEXMLFragmentsProtocolFragmentHelper.getProtocolFragment(logger, argsLoader, ref, isSource, YADEJumpHostArguments.LABEL);

        argsLoader.initializeJumpHostArgsIfNull();
        argsLoader.getJumpHostArgs().getConfiguredOnSource().setValue(isSource);
        setSourceOrTargetLabel(logger, argsLoader, isSource);

        // YADE1 - compatibility
        // Parse before Pre/Post-Processing because this value is used to split commands
        YADEXMLProfileHelper.parseProcessingCommandDelimiter(argsLoader, fragment, argsLoader.getJumpHostArgs().getCommands().getCommandDelimiter(),
                "JumpCommandDelimiter");

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                // YADE1 - compatibility
                case "JumpDirectory":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getTempDirectoryParent(), n);
                    break;
                case "JumpCommand":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getYADEClientCommand(), n);
                    break;
                case "JumpCommandBeforeFile":
                    argsLoader.getJumpHostArgs().getCommands().setCommandsBeforeFile(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterFileOnSuccess":
                    argsLoader.getJumpHostArgs().getCommands().setCommandsAfterFile(argsLoader.getValue(n));
                    break;
                case "JumpCommandBeforeOperation":
                    argsLoader.getJumpHostArgs().getCommands().setCommandsBeforeOperation(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnSuccess":
                    argsLoader.getJumpHostArgs().getCommands().setCommandsAfterOperationOnSuccess(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnError":
                    argsLoader.getJumpHostArgs().getCommands().setCommandsAfterOperationOnError(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationFinal":
                    argsLoader.getJumpHostArgs().getCommands().setCommandsAfterOperationFinal(argsLoader.getValue(n));
                    break;
                case "ServerAliveInterval":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getProvider().getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":
                    argsLoader.setIntegerArgumentValue(argsLoader.getJumpHostArgs().getProvider().getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getProvider().getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getProvider().getSocketTimeout(), n);
                    break;

                // YADE JS7
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(logger, argsLoader, n, isSource, argsLoader.getJumpHostArgs().getProvider());
                    break;
                case "DecryptionFragmentRef":
                    YADEXMLFragmentsDecryptionFragmentHelper.parse(logger, argsLoader, n, isSource, argsLoader.getJumpHostArgs().getProvider());
                    break;

                case "BasicConnection":
                    YADEXMLFragmentsProtocolFragmentHelper.parseBasicConnection(logger, argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;
                case "SSHAuthentication":
                    YADEXMLFragmentsProtocolFragmentHelper.parseSFTPSSHAuthentication(logger, argsLoader, argsLoader.getJumpHostArgs().getProvider(),
                            n);
                    break;

                case "YADEClientCommand": // JS7 - YADE-626
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getYADEClientCommand(), n);
                    break;
                case "TempDirectoryParent": // JS7 - YADE-626
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getTempDirectoryParent(), n);
                    break;
                case "SFTPProcessing": // JS7 - YADE-626
                    parseSFTPProcessing(logger, argsLoader, argsLoader.getJumpHostArgs(), n);
                    break;

                case "ProxyForSFTP":
                    YADEXMLFragmentsProtocolFragmentHelper.parseProxy(logger, argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;
                case "SocketTimeout": // JS7 - YADE-626
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getProvider().getSocketTimeout(), n);
                    break;
                case "KeepAlive": // JS7 - YADE-626
                    YADEXMLFragmentsProtocolFragmentHelper.parseSFTPKeepAlive(logger, argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;
                case "StrictHostkeyChecking":
                    argsLoader.setBooleanArgumentValue(argsLoader.getJumpHostArgs().getProvider().getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    YADEXMLFragmentsProtocolFragmentHelper.parseConfigurationFiles(logger, argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;
                case "SFTPFragmentAlternatives":
                    parseAlternativeSFTPFragments(logger, argsLoader, argsLoader.getJumpHostArgs(), fragment, n, isSource);
                    break;
                }
            }
        }
    }

    protected static void clear() {
        VISITED_ALTERNATIVES.clear();
    }

    private static void parseSFTPProcessing(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, YADEJumpHostArguments args, Node sftpProcessing)
            throws Exception {
        // Parse before Pre/Post-Processing because this value is used to split commands
        YADEXMLProfileHelper.parseProcessingCommandDelimiter(argsLoader, sftpProcessing, args.getCommands().getCommandDelimiter(),
                "ProcessingCommandDelimiter");

        NodeList nl = sftpProcessing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SFTPPreProcessing":
                    YADEXMLProfileHelper.parsePreProcessing(logger, argsLoader, args.getCommands(), n);
                    break;
                case "SFTPPostProcessing":
                    YADEXMLProfileHelper.parsePostProcessing(logger, argsLoader, args.getCommands(), n);
                    break;
                case "Platform":
                    args.setPlatform(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }

    private static void setSourceOrTargetLabel(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, boolean isSource) {
        YADESourceTargetArguments args = isSource ? argsLoader.getSourceArgs() : argsLoader.getTargetArgs();
        if (args != null) {
            args.getLabel().setValue(args.getLabel().getValue() + " (via " + YADEJumpHostArguments.LABEL + ")");
        }
    }

    private static void parseAlternativeSFTPFragments(ISOSLogger logger, YADEXMLArgumentsLoader argsLoader, YADEJumpHostArguments args, Node fragment,
            Node alternativeFragmentRef, boolean isSource) throws Exception {

        NodeList nl = alternativeFragmentRef.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node ref = nl.item(i);
            if (ref.getNodeType() == Node.ELEMENT_NODE) {
                String refNodeName = ref.getNodeName();
                String refId = YADEXMLFragmentsProtocolFragmentHelper.getFragmentKeyFromRef(refNodeName, ref);

                if (args.getProvider().keyEquals(refId)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[jump][parseAlternativeSFTPFragments][" + args.getProvider().getKey().getValue() + "][skip " + refId
                                + "]same fragment/alternative");
                    }
                    continue;
                }
                if (!VISITED_ALTERNATIVES.add(refId)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[jump][parseAlternativeSFTPFragments][" + args.getProvider().getKey().getValue() + "][skip " + refId
                                + "][already visited]" + args.getProvider().getAlternativesAsString());
                    }
                    continue;
                }

                SSHProviderArguments alternative = null;
                switch (refNodeName) {
                case "SFTPFragmentRef":
                    alternative = YADEXMLFragmentsProtocolFragmentHelper.parseSFTP(logger, argsLoader, ref, isSource, true, VISITED_ALTERNATIVES);
                    break;
                }

                if (alternative != null) {
                    alternative.getKey().setValue(refId);
                    args.getProvider().mergeNestedAlternatives(alternative);

                    if (logger.isDebugEnabled()) {
                        logger.debug("[jump][parseAlternativeSFTPFragments][" + args.getProvider().getKey().getValue() + "][merge " + alternative
                                .getKey().getValue() + "]" + args.getProvider().getAlternativesAsString());
                    }
                }
            }
        }
    }

}
