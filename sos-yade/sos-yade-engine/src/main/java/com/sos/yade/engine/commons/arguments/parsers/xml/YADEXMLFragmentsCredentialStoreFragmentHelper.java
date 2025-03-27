package com.sos.yade.engine.commons.arguments.parsers.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.xml.SOSXML;

public class YADEXMLFragmentsCredentialStoreFragmentHelper {

    protected static void parse(YADEXMLParser impl, Node ref, boolean isSource, AProviderArguments providerArgs) throws Exception {
        String exp = "Fragments/CredentialStoreFragments[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node fragment = impl.getXPath().selectNode(impl.getRoot(), exp);
        if (fragment == null) {
            throw new SOSMissingDataException("[profile=" + impl.getArgs().getProfile().getValue() + "][" + (isSource ? "Source" : "Target") + "]["
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
                        impl.setStringArgumentValue(csArgs.getFile(), n);
                        break;
                    case "CSAuthentication":
                        parseCSAuthentication(impl, n, csArgs);
                        break;
                    case "CSEntryPath":
                        impl.setStringArgumentValue(csArgs.getEntryPath(), n);
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

    private static void parseCSAuthentication(YADEXMLParser impl, Node csAuthentication, CredentialStoreArguments csArgs) {
        NodeList nl = csAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "PasswordAuthentication":
                    parseCSAuthenticationPasswordAuthentication(impl, n, csArgs);
                    break;
                case "KeyFileAuthentication":
                    parseCSAuthenticationKeyFileAuthentication(impl, n, csArgs);
                    break;
                }
            }
        }
    }

    private static void parseCSAuthenticationPasswordAuthentication(YADEXMLParser impl, Node passwordAuthentication,
            CredentialStoreArguments csArgs) {
        NodeList nl = passwordAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CSPassword":
                    impl.setStringArgumentValue(csArgs.getPassword(), n);
                    break;
                }
            }
        }
    }

    private static void parseCSAuthenticationKeyFileAuthentication(YADEXMLParser impl, Node keyFileAuthentication, CredentialStoreArguments csArgs) {
        NodeList nl = keyFileAuthentication.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CSKeyFile":
                    impl.setStringArgumentValue(csArgs.getKeyFile(), n);
                    break;
                case "CSPassword":
                    impl.setStringArgumentValue(csArgs.getPassword(), n);
                    break;
                }
            }
        }
    }

}
