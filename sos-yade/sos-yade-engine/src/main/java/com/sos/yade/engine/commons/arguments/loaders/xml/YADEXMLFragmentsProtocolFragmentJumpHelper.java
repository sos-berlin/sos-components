package com.sos.yade.engine.commons.arguments.loaders.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class YADEXMLFragmentsProtocolFragmentJumpHelper {

    protected static void parse(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = YADEXMLFragmentsProtocolFragmentHelper.getProtocolFragment(argsLoader, ref, isSource, "Jump");

        argsLoader.initializeJumpHostArgsIfNull();
        argsLoader.getJumpHostArgs().getConfiguredOnSource().setValue(isSource);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, argsLoader.getJumpHostArgs().getProvider());
                    break;

                case "BasicConnection":
                    YADEXMLFragmentsProtocolFragmentHelper.parseBasicConnection(argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;
                case "SSHAuthentication":
                    YADEXMLFragmentsProtocolFragmentHelper.parseSFTPSSHAuthentication(argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;
                case "ProxyForSFTP":
                    YADEXMLFragmentsProtocolFragmentHelper.parseProxy(argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;

                case "SocketTimeout": // JS7 - YADE-626
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getProvider().getSocketTimeout(), n);
                    break;
                case "KeepAlive": // JS7 - YADE-626
                    YADEXMLFragmentsProtocolFragmentHelper.parseSFTPKeepAlive(argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;
                case "StrictHostkeyChecking":
                    argsLoader.setBooleanArgumentValue(argsLoader.getJumpHostArgs().getProvider().getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    YADEXMLFragmentsProtocolFragmentHelper.parseConfigurationFiles(argsLoader, argsLoader.getJumpHostArgs().getProvider(), n);
                    break;

                case "JumpDirectory":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getDirectory(), n);
                    break;
                case "JumpYADEClientCommand": // JS7 - YADE-626
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getYADEClientCommand(), n);
                    break;

                case "SFTPPreProcessing":
                    YADEXMLProfileHelper.parsePreProcessing(argsLoader, argsLoader.getJumpHostArgs().getCommands(), n);
                    break;
                case "SFTPPostProcessing":
                    YADEXMLProfileHelper.parsePostProcessing(argsLoader, argsLoader.getJumpHostArgs().getCommands(), n);
                    break;
                case "ProcessingCommandDelimiter":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getYADEClientCommand(), n);
                    break;
                case "Platform":
                    argsLoader.getJumpHostArgs().setPlatform(argsLoader.getValue(n));
                    break;

                case "JumpCommand": // YADE1 - compatibility
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getYADEClientCommand(), n);
                    break;
                case "JumpCommandBeforeFile":  // YADE1 - compatibility
                    argsLoader.getJumpHostArgs().getCommands().setCommandsBeforeFile(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterFileOnSuccess":  // YADE1 - compatibility
                    argsLoader.getJumpHostArgs().getCommands().setCommandsAfterFile(argsLoader.getValue(n));
                    break;
                case "JumpCommandBeforeOperation":  // YADE1 - compatibility
                    argsLoader.getJumpHostArgs().getCommands().setCommandsBeforeOperation(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnSuccess":  // YADE1 - compatibility
                    argsLoader.getJumpHostArgs().getCommands().setCommandsAfterOperationOnSuccess(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnError":  // YADE1 - compatibility
                    argsLoader.getJumpHostArgs().getCommands().setCommandsAfterOperationOnError(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationFinal":  // YADE1 - compatibility
                    argsLoader.getJumpHostArgs().getCommands().setCommandsAfterOperationFinal(argsLoader.getValue(n));
                    break;
                case "JumpCommandDelimiter":  // YADE1 - compatibility
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getCommands().getCommandDelimiter(), n);
                    break;
                case "ServerAliveInterval":  // YADE1 - compatibility
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getProvider().getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":  // YADE1 - compatibility
                    argsLoader.setIntegerArgumentValue(argsLoader.getJumpHostArgs().getProvider().getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":  // YADE1 - compatibility
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getProvider().getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":  // YADE1 - compatibility
                    argsLoader.setStringArgumentValue(argsLoader.getJumpHostArgs().getProvider().getSocketTimeout(), n);
                    break;

                }
            }
        }
    }
}
