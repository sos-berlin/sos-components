package com.sos.yade.engine.commons.arguments.loaders.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.encryption.arguments.EncryptionDecryptArguments;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.xml.SOSXML;

public class YADEXMLFragmentsDecryptionFragmentHelper {

    protected static void parse(YADEXMLArgumentsLoader argsLoader, Node ref, boolean isSource, AProviderArguments providerArgs) throws Exception {
        String exp = "Fragments/DecryptionFragments/DecryptionFragment[@name='" + SOSXML.getAttributeValue(ref, "ref") + "']";
        Node fragment = argsLoader.getXPath().selectNode(argsLoader.getRoot(), exp);
        if (fragment == null) {
            throw new SOSMissingDataException("[Profile=" + argsLoader.getArgs().getProfile().getValue() + "][" + (isSource ? "Source" : "Target")
                    + "][" + exp + "]referenced DecryptionFragment not found");
        }

        NodeList nl = fragment.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            EncryptionDecryptArguments args = new EncryptionDecryptArguments();
            args.applyDefaultIfNullQuietly();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "EnciphermentPrivateKey":
                        argsLoader.setStringArgumentValue(args.getPrivateKeyPath(), n);
                        break;
                    }
                }
            }
            providerArgs.setEncryptionDecrypt(args);
        }
    }

}
