package com.sos.yade.engine.commons.arguments.loaders.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class YADEXMLFragmentsProtocolFragmentJumpHelper {

    protected static void parse(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource) throws Exception {
        Node fragment = YADEXMLFragmentsProtocolFragmentHelper.getProtocolFragment(argsLoader, ref, isSource, "Jump");

        argsLoader.initializeJumpArgsIfNull();
        argsLoader.getJumpArguments().getIsSource().setValue(isSource);

        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    YADEXMLFragmentsProtocolFragmentHelper.parseBasicConnection(argsLoader, argsLoader.getJumpArguments().getProvider(), n);
                    break;
                case "SSHAuthentication":
                    YADEXMLFragmentsProtocolFragmentHelper.parseSFTPSSHAuthentication(argsLoader, argsLoader.getJumpArguments().getProvider(), n);
                    break;
                case "JumpCommand":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArguments().getYADEClientCommand(), n);
                    break;
                case "JumpCommandBeforeFile":
                    argsLoader.getJumpArguments().getCommands().setCommandsBeforeFile(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterFileOnSuccess":
                    argsLoader.getJumpArguments().getCommands().setCommandsAfterFile(argsLoader.getValue(n));
                    break;
                case "JumpCommandBeforeOperation":
                    argsLoader.getJumpArguments().getCommands().setCommandsBeforeOperation(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnSuccess":
                    argsLoader.getJumpArguments().getCommands().setCommandsAfterOperationOnSuccess(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnError":
                    argsLoader.getJumpArguments().getCommands().setCommandsAfterOperationOnError(argsLoader.getValue(n));
                    break;
                case "JumpCommandAfterOperationFinal":
                    argsLoader.getJumpArguments().getCommands().setCommandsAfterOperationFinal(argsLoader.getValue(n));
                    break;
                case "JumpCommandDelimiter":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArguments().getCommands().getCommandDelimiter(), n);
                    break;
                case "JumpDirectory":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArguments().getDirectory(), n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsLoader, n, isSource, argsLoader.getJumpArguments().getProvider());
                    break;
                case "ProxyForSFTP":
                    YADEXMLFragmentsProtocolFragmentHelper.parseProxy(argsLoader, argsLoader.getJumpArguments().getProvider(), n);
                    break;
                case "StrictHostkeyChecking":
                    argsLoader.setBooleanArgumentValue(argsLoader.getJumpArguments().getProvider().getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    YADEXMLFragmentsProtocolFragmentHelper.parseConfigurationFiles(argsLoader, argsLoader.getJumpArguments().getProvider(), n);
                    break;
                case "ServerAliveInterval":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArguments().getProvider().getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":
                    argsLoader.setIntegerArgumentValue(argsLoader.getJumpArguments().getProvider().getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArguments().getProvider().getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":
                    argsLoader.setStringArgumentValue(argsLoader.getJumpArguments().getProvider().getSocketTimeout(), n);
                    break;
                case "Platform":
                    argsLoader.getJumpArguments().setPlatform(argsLoader.getValue(n));
                    break;
                }
            }
        }
    }
}
