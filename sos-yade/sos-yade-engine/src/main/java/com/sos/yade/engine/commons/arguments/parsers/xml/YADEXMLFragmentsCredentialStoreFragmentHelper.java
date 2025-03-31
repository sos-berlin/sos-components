package com.sos.yade.engine.commons.arguments.parsers.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.xml.SOSXML;

public class YADEXMLFragmentsCredentialStoreFragmentHelper {

    protected static void parse(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource, AProviderArguments providerArgs) throws Exception {
        String exp = "Fragments/CredentialStoreFragments[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node fragment = argsSetter.getXPath().selectNode(argsSetter.getRoot(), exp);
        if (fragment == null) {
            throw new SOSMissingDataException("[profile=" + argsSetter.getArgs().getProfile().getValue() + "][" + (isSource ? "Source" : "Target") + "]["
                    + exp + "]referenced CredentialStore fragment not found");
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
                        argsSetter.setStringArgumentValue(csArgs.getFile(), n);
                        break;
                    case "CSAuthentication":
                        parseCSAuthentication(argsSetter, n, csArgs);
                        break;
                    case "CSEntryPath":
                        argsSetter.setStringArgumentValue(csArgs.getEntryPath(), n);
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

    private static void parseCSAuthentication(YADEXMLArgumentsSetter argsSetter, Node csAuthentication, CredentialStoreArguments csArgs) {
        NodeList nl = csAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "PasswordAuthentication":
                    parseCSAuthenticationPasswordAuthentication(argsSetter, n, csArgs);
                    break;
                case "KeyFileAuthentication":
                    parseCSAuthenticationKeyFileAuthentication(argsSetter, n, csArgs);
                    break;
                }
            }
        }
    }

    private static void parseCSAuthenticationPasswordAuthentication(YADEXMLArgumentsSetter argsSetter, Node passwordAuthentication,
            CredentialStoreArguments csArgs) {
        NodeList nl = passwordAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CSPassword":
                    argsSetter.setStringArgumentValue(csArgs.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseCSAuthenticationKeyFileAuthentication(YADEXMLArgumentsSetter argsSetter, Node keyFileAuthentication, CredentialStoreArguments csArgs) {
        NodeList nl = keyFileAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CSKeyFile":
                    argsSetter.setStringArgumentValue(csArgs.getKeyFile(), n);
                    break;
                case "CSPassword":
                    argsSetter.setStringArgumentValue(csArgs.getPassword(), n);
                    break;
                }
            }
        }
    }

}
