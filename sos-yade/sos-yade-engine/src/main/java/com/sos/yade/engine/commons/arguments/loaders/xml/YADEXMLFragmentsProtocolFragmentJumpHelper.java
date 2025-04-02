package com.sos.yade.engine.commons.arguments.loaders.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class YADEXMLFragmentsProtocolFragmentJumpHelper {

    protected static void parse(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = YADEXMLFragmentsProtocolFragmentHelper.getProtocolFragment(argsLoader, ref, isSource, "Jump");

        argsLoader.initializeJumpArgsIfNull();
        argsLoader.getJumpArgs().getConfiguredOnSource().setValue(isSource);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    YADEXMLFragmentsProtocolFragmentHelper.parseBasicConnection(argsLoader, argsLoader.getJumpArgs().getProvider(), n);
                    break;
                case "SSHAuthentication":
                    YADEXMLFragmentsProtocolFragmentHelper.parseSFTPSSHAuthentication(argsLoader, argsLoader.getJumpArgs().getProvider(), n);
                    break;
                case "JumpCommand":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArgs().getYADEClientCommand(), n);
                    break;
                case "JumpCommandBeforeFile":
                    argsLoader.getJumpArgs().getCommands().setCommandsBeforeFile(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterFileOnSuccess":
                    argsLoader.getJumpArgs().getCommands().setCommandsAfterFile(argsLoader.getValue(n));
                    break;
                case "JumpCommandBeforeOperation":
                    argsLoader.getJumpArgs().getCommands().setCommandsBeforeOperation(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnSuccess":
                    argsLoader.getJumpArgs().getCommands().setCommandsAfterOperationOnSuccess(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnError":
                    argsLoader.getJumpArgs().getCommands().setCommandsAfterOperationOnError(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationFinal":
                    argsLoader.getJumpArgs().getCommands().setCommandsAfterOperationFinal(argsLoader.getValue(n));
                    break;
                case "JumpCommandDelimiter":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArgs().getCommands().getCommandDelimiter(), n);
                    break;
                case "JumpDirectory":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArgs().getDirectory(), n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, argsLoader.getJumpArgs().getProvider());
                    break;
                case "ProxyForSFTP":
                    YADEXMLFragmentsProtocolFragmentHelper.parseProxy(argsLoader, argsLoader.getJumpArgs().getProvider(), n);
                    break;
                case "StrictHostkeyChecking":
                    argsLoader.setBooleanArgumentValue(argsLoader.getJumpArgs().getProvider().getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    YADEXMLFragmentsProtocolFragmentHelper.parseConfigurationFiles(argsLoader, argsLoader.getJumpArgs().getProvider(), n);
                    break;
                case "ServerAliveInterval":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArgs().getProvider().getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":
                    argsLoader.setIntegerArgumentValue(argsLoader.getJumpArgs().getProvider().getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArgs().getProvider().getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArgs().getProvider().getSocketTimeout(), n);
                    break;
                case "Platform":
                    argsLoader.getJumpArgs().setPlatform(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }
}
