package com.sos.yade.engine.commons.arguments.loaders.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.xml.SOSXML;

public class YADEXMLFragmentsCredentialStoreFragmentHelper {

    protected static void parse(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource, AProviderArguments providerArgs) throws Exception {
        String exp = "Fragments/CredentialStoreFragments/CredentialStoreFragment[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node fragment = argsLoader.getXPath().selectNode(argsLoader.getRoot(), exp);
        if (fragment == null) {
            throw new SOSMissingDataException("[profile=" + argsLoader.getArgs().getProfile().getValue() + "][" + (isSource ? "Source" : "Target")
                    + "][" + exp + "]referenced CredentialStore fragment not found");
        }

        NodeList nl = fragment.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            CredentialStoreArguments csArgs = new CredentialStoreArguments();
            csArgs.applyDefaultIfNullQuietly();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "CSFile":
                        argsLoader.setStringArgumentValue(csArgs.getFile(), n);
                        break;
                    case "CSAuthentication":
                        parseCSAuthentication(argsLoader, n, csArgs);
                        break;
                    case "CSEntryPath":
                        argsLoader.setStringArgumentValue(csArgs.getEntryPath(), n);
                        break;
                    case "CSExportAttachment":
                        // ignored
                        break;
                    case "CSStoreType":
                        // ignored
                        break;

                    }
                }
            }
            providerArgs.setCredentialStore(csArgs);
        }
    }

    private static void parseCSAuthentication(YADEXMLArgumentsLoader argsLoader, Node csAuthentication, CredentialStoreArguments csArgs) {
        NodeList nl = csAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "PasswordAuthentication":
                    parseCSAuthenticationPasswordAuthentication(argsLoader, n, csArgs);
                    break;
                case "KeyFileAuthentication":
                    parseCSAuthenticationKeyFileAuthentication(argsLoader, n, csArgs);
                    break;
                }
            }
        }
    }

    private static void parseCSAuthenticationPasswordAuthentication(YADEXMLArgumentsLoader argsLoader, Node passwordAuthentication,
            CredentialStoreArguments csArgs) {
        NodeList nl = passwordAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CSPassword":
                    argsLoader.setStringArgumentValue(csArgs.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseCSAuthenticationKeyFileAuthentication(YADEXMLArgumentsLoader argsLoader, Node keyFileAuthentication,
            CredentialStoreArguments csArgs) {
        NodeList nl = keyFileAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CSKeyFile":
                    argsLoader.setStringArgumentValue(csArgs.getKeyFile(), n);
                    break;
                case "CSPassword":
                    argsLoader.setStringArgumentValue(csArgs.getPassword(), n);
                    break;
                }
            }
        }
    }

}
