package com.sos.commons.credentialstore.keepass.extensions.credentials;

import java.io.InputStream;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;

import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassKeyFileException;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassKeyFileParseException;

public class SOSKdbxKeyFile {

     private static XPath xpath = XPathFactory.newInstance().newXPath();

    /** Load a key from an InputStream with a KDBX XML key file.
     * 
     * @param inputStream the input stream holding the key
     * @return they key or null if there was a problem */
    public static byte[] load(InputStream inputStream) throws SOSKeePassKeyFileException {
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
        } catch (Throwable e) {
            throw new SOSKeePassKeyFileParseException(e.toString(), e);
        }

        int version = getVersion(doc);
        String keyData = getKeyData(doc);

        switch (version) {
        case 1:
            // android compatibility
            return Base64.decodeBase64(keyData.getBytes());
        case 2:
            return DatatypeConverter.parseHexBinary(keyData.trim().replaceAll("\\s", ""));
        default:
            return null;
        }
    }

    private static int getVersion(Document doc) throws SOSKeePassKeyFileException {
        try {
            String v = (String) xpath.evaluate("//KeyFile/Meta/Version/text()", doc, XPathConstants.STRING);
            if (v == null) {
                throw new Exception("version not found");
            }
            if (v.startsWith("1")) {
                return 1;
            } else if (v.startsWith("2")) {
                return 2;
            }
            throw new Exception(String.format("not supported version=%s", v));
        } catch (Throwable e) {
            throw new SOSKeePassKeyFileException(String.format("[can't evaluate version]%s", e.toString()), e);
        }
    }

    private static String getKeyData(Document doc) throws SOSKeePassKeyFileException {
        try {
            String keyData = (String) xpath.evaluate("//KeyFile/Key/Data/text()", doc, XPathConstants.STRING);
            if (keyData == null) {
                throw new Exception("key data not found");
            }
            return keyData;
        } catch (Throwable e) {
            throw new SOSKeePassKeyFileException(String.format("[can't evaluate key data]%s", e.toString()), e);
        }
    }
}
