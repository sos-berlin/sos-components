package com.sos.yade.engine.commons.arguments.parsers.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class YADEXMLFragmentsProtocolFragmentJumpHelper {

    protected static void parse(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        Node fragment = YADEXMLFragmentsProtocolFragmentHelper.getProtocolFragment(argsSetter, ref, isSource, "Jump");

        argsSetter.initializeJumpArgsIfNull();
        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    YADEXMLFragmentsProtocolFragmentHelper.parseBasicConnection(argsSetter, argsSetter.getJumpArguments().getProvider(), n);
                    break;
                case "SSHAuthentication":
                    YADEXMLFragmentsProtocolFragmentHelper.parseSFTPSSHAuthentication(argsSetter, argsSetter.getJumpArguments().getProvider(), n);
                    break;
                case "JumpCommand":
                    argsSetter.setStringArgumentValue(argsSetter.getJumpArguments().getYADEClientCommand(), n);
                    break;
                case "JumpCommandBeforeFile":
                    argsSetter.getJumpArguments().getCommands().setCommandsBeforeFile(argsSetter.getValue(n));
                    break;
                case "JumpCommandAfterFileOnSuccess":
                    argsSetter.getJumpArguments().getCommands().setCommandsAfterFile(argsSetter.getValue(n));
                    break;
                case "JumpCommandBeforeOperation":
                    argsSetter.getJumpArguments().getCommands().setCommandsBeforeOperation(argsSetter.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnSuccess":
                    argsSetter.getJumpArguments().getCommands().setCommandsAfterOperationOnSuccess(argsSetter.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnError":
                    argsSetter.getJumpArguments().getCommands().setCommandsAfterOperationOnError(argsSetter.getValue(n));
                    break;
                case "JumpCommandAfterOperationFinal":
                    argsSetter.getJumpArguments().getCommands().setCommandsAfterOperationFinal(argsSetter.getValue(n));
                    break;
                case "JumpCommandDelimiter":
                    argsSetter.setStringArgumentValue(argsSetter.getJumpArguments().getCommands().getCommandDelimiter(), n);
                    break;
                case "JumpDirectory":
                    argsSetter.setStringArgumentValue(argsSetter.getJumpArguments().getDirectory(), n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, argsSetter.getJumpArguments().getProvider());
                    break;
                case "ProxyForSFTP":
                    YADEXMLFragmentsProtocolFragmentHelper.parseProxy(argsSetter, argsSetter.getJumpArguments().getProvider(), n);
                    break;
                case "StrictHostkeyChecking":
                    argsSetter.setBooleanArgumentValue(argsSetter.getJumpArguments().getProvider().getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    YADEXMLFragmentsProtocolFragmentHelper.parseConfigurationFiles(argsSetter, argsSetter.getJumpArguments().getProvider(), n);
                    break;
                case "ServerAliveInterval":
                    argsSetter.setStringArgumentValue(argsSetter.getJumpArguments().getProvider().getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":
                    argsSetter.setIntegerArgumentValue(argsSetter.getJumpArguments().getProvider().getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":
                    argsSetter.setStringArgumentValue(argsSetter.getJumpArguments().getProvider().getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":
                    argsSetter.setStringArgumentValue(argsSetter.getJumpArguments().getProvider().getSocketTimeout(), n);
                    break;
                case "Platform":
                    argsSetter.getJumpArguments().setPlatform(argsSetter.getValue(n));
                    break;
                }
            }
        }
    }
}
