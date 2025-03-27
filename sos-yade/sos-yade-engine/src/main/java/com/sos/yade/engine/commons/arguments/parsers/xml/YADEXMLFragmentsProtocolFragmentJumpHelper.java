package com.sos.yade.engine.commons.arguments.parsers.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class YADEXMLFragmentsProtocolFragmentJumpHelper {

    protected static void parse(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        Node fragment = YADEXMLFragmentsProtocolFragmentHelper.getProtocolFragment(impl, ref, isSource, "Jump");

        impl.initializeJumpArgsIfNull();
        NodeList nl = fragment.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BasicConnection":
                    YADEXMLFragmentsProtocolFragmentHelper.parseBasicConnection(impl, impl.getJumpArguments().getProvider(), n);
                    break;
                case "SSHAuthentication":
                    YADEXMLFragmentsProtocolFragmentHelper.parseSFTPSSHAuthentication(impl, impl.getJumpArguments().getProvider(), n);
                    break;
                case "JumpCommand":
                    impl.setStringArgumentValue(impl.getJumpArguments().getYADEClientCommand(), n);
                    break;
                case "JumpCommandBeforeFile":
                    impl.getJumpArguments().getCommands().setCommandsBeforeFile(impl.getValue(n));
                    break;
                case "JumpCommandAfterFileOnSuccess":
                    impl.getJumpArguments().getCommands().setCommandsAfterFile(impl.getValue(n));
                    break;
                case "JumpCommandBeforeOperation":
                    impl.getJumpArguments().getCommands().setCommandsBeforeOperation(impl.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnSuccess":
                    impl.getJumpArguments().getCommands().setCommandsAfterOperationOnSuccess(impl.getValue(n));
                    break;
                case "JumpCommandAfterOperationOnError":
                    impl.getJumpArguments().getCommands().setCommandsAfterOperationOnError(impl.getValue(n));
                    break;
                case "JumpCommandAfterOperationFinal":
                    impl.getJumpArguments().getCommands().setCommandsAfterOperationFinal(impl.getValue(n));
                    break;
                case "JumpCommandDelimiter":
                    impl.setStringArgumentValue(impl.getJumpArguments().getCommands().getCommandDelimiter(), n);
                    break;
                case "JumpDirectory":
                    impl.setStringArgumentValue(impl.getJumpArguments().getDirectory(), n);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, impl.getJumpArguments().getProvider());
                    break;
                case "ProxyForSFTP":
                    YADEXMLFragmentsProtocolFragmentHelper.parseProxy(impl, impl.getJumpArguments().getProvider(), n);
                    break;
                case "StrictHostkeyChecking":
                    impl.setBooleanArgumentValue(impl.getJumpArguments().getProvider().getStrictHostkeyChecking(), n);
                    break;
                case "ConfigurationFiles":
                    YADEXMLFragmentsProtocolFragmentHelper.parseConfigurationFiles(impl, impl.getJumpArguments().getProvider(), n);
                    break;
                case "ServerAliveInterval":
                    impl.setStringArgumentValue(impl.getJumpArguments().getProvider().getServerAliveInterval(), n);
                    break;
                case "ServerAliveCountMax":
                    impl.setIntegerArgumentValue(impl.getJumpArguments().getProvider().getServerAliveCountMax(), n);
                    break;
                case "ConnectTimeout":
                    impl.setStringArgumentValue(impl.getJumpArguments().getProvider().getConnectTimeout(), n);
                    break;
                case "ChannelConnectTimeout":
                    impl.setStringArgumentValue(impl.getJumpArguments().getProvider().getSocketTimeout(), n);
                    break;
                case "Platform":
                    impl.getJumpArguments().setPlatform(impl.getValue(n));
                    break;
                }
            }
        }
    }
}
