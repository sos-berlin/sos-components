package com.sos.commons.credentialstore.keepass.extensions.simple.transformer;

import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.linguafranca.pwdb.kdbx.Helpers;
import org.linguafranca.pwdb.kdbx.StreamEncryptor;
import org.linguafranca.xml.XmlEventTransformer;

public class SOSKdbxOutputTransformer implements XmlEventTransformer {

    private XMLEventFactory eventFactory = com.fasterxml.aalto.stax.EventFactoryImpl.newInstance();
    private StreamEncryptor encryptor;
    private Boolean encryptContent = false;

    public SOSKdbxOutputTransformer(StreamEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public XMLEvent transform(XMLEvent event) {
        switch (event.getEventType()) {
        case START_ELEMENT: {
            Attribute attribute = event.asStartElement().getAttributeByName(new QName("Protected"));
            if (attribute != null) {
                encryptContent = Helpers.toBoolean(attribute.getValue());
                // this is a workaround for Simple XML not calling converter on attributes
                List<Attribute> attributes = new ArrayList<>();
                if (attribute.getValue().toLowerCase().equals("true")) {
                    attributes.add(eventFactory.createAttribute("Protected", "True"));
                } else {
                    attributes.add(eventFactory.createAttribute("Protected", "False"));
                }
                event = eventFactory.createStartElement(event.asStartElement().getName(), attributes.iterator(), null);
            } else {
                if (event.asStartElement().getName().toString().equals("Binary")) {
                    Attribute compressed = event.asStartElement().getAttributeByName(new QName("Compressed"));
                    if (compressed != null) {
                        // this is a workaround for Simple XML not calling converter on attributes
                        List<Attribute> attributes = new ArrayList<>();

                        if (compressed.getValue().toLowerCase().equals("true")) {
                            attributes.add(eventFactory.createAttribute("Compressed", "True"));
                        } else {
                            attributes.add(eventFactory.createAttribute("Compressed", "False"));
                        }
                        @SuppressWarnings("unchecked")
                        Iterator<Attribute> it = event.asStartElement().getAttributes();
                        if (it.hasNext()) {
                            Attribute att = (Attribute) it.next();
                            String attrName = att.getName().toString();
                            if (!attrName.equals("Compressed")) {
                                attributes.add(eventFactory.createAttribute(attrName, att.getValue()));
                            }
                        }
                        event = eventFactory.createStartElement(event.asStartElement().getName(), attributes.iterator(), null);
                    }
                }
            }

            break;
        }
        case CHARACTERS: {
            if (encryptContent && encryptor != null) {
                String unencrypted = event.asCharacters().getData();
                String encrypted = Helpers.encodeBase64Content(encryptor.encrypt(unencrypted.getBytes()), false);

                event = eventFactory.createCharacters(encrypted);
            }
            break;
        }
        case END_ELEMENT: {
            encryptContent = false;
            break;
        }
        }
        return event;
    }

}
