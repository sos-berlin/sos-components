package com.sos.commons.credentialstore.keepass.db.kdbx.dom;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.dom.DomDatabaseWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomEntryWrapper;
import org.linguafranca.pwdb.kdbx.dom.DomGroupWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.db.kdbx.ASOSKdbxHandler;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;

public class SOSKdbxDOMHandler extends ASOSKdbxHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKdbxDOMHandler.class);

    // see org.linguafranca.pwdb.kdbx.dom.DomHelper
    static final String LAST_MODIFICATION_TIME_ELEMENT_NAME = "Times/LastModificationTime";
    static final String CREATION_TIME_ELEMENT_NAME = "Times/CreationTime";
    static final String LAST_ACCESS_TIME_ELEMENT_NAME = "Times/LastAccessTime";

    static XPath xpath = XPathFactory.newInstance().newXPath();
    static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    protected DomDatabaseWrapper load() throws SOSKeePassDatabaseException {
        try (InputStream is = Files.newInputStream(getKeePassFile())) {
            return DomDatabaseWrapper.load(getCredentials(), is);
        } catch (Exception ex) {
            throw new SOSKeePassDatabaseException(String.format("[%s]%s", SOSKeePassDatabase.getFilePath(getKeePassFile()), ex.toString()), ex);
        }
    }

    @Override
    public Entry<?, ?, ?, ?> createEntry(String[] entryPath) throws SOSKeePassDatabaseException {
        String method = "createEntry";

        DomDatabaseWrapper d = (DomDatabaseWrapper) getDatabase();
        DomGroupWrapper lastGroup = d.getRootGroup();
        DomEntryWrapper entry = null;

        for (int i = 2; i < entryPath.length; i++) {
            String name = entryPath[i];

            if (i == entryPath.length - 1) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][addEntry]%s", method, name));
                }
                entry = addEntry(d, lastGroup, name);
            } else {
                List<? extends DomGroupWrapper> result = lastGroup.findGroups(name);
                if (result == null || result.size() == 0) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[%s][addGroup]%s", method, name));
                    }
                    lastGroup = addGroup(d, lastGroup, name);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("[%s][useExistingGroup]%s", method, name));
                    }
                    lastGroup = result.get(0);
                }
            }
        }
        setDatabase(d);
        return entry;
    }

    private DomEntryWrapper addEntry(DomDatabaseWrapper d, DomGroupWrapper parentGroup, String entryName) {
        DomEntryWrapper e = d.newEntry(entryName);
        e.setIcon(d.newIcon(ICON_INDEX_NEW_ENTRY));
        setNewItemTimesAsUTC(e);

        DomEntryWrapper entry = parentGroup.addEntry(e);
        setModifiedItemTimesAsUTC(parentGroup);

        return entry;
    }

    private DomGroupWrapper addGroup(DomDatabaseWrapper d, DomGroupWrapper parentGroup, String groupName) {
        DomGroupWrapper g = d.newGroup(groupName);
        g.setIcon(d.newIcon(ICON_INDEX_NEW_GROUP));
        setNewItemTimesAsUTC(g);

        DomGroupWrapper group = parentGroup.addGroup(g);
        setModifiedItemTimesAsUTC(parentGroup);

        return group;
    }

    private void setNewItemTimesAsUTC(Object obj) {
        try {
            Field f = FieldUtils.getField(obj.getClass(), "element", true);
            f.set(obj, setNewItemTimes((Element) f.get(obj)));
        } catch (Exception e) {
        }
    }

    private void setModifiedItemTimesAsUTC(Object obj) {
        try {
            Field f = FieldUtils.getField(obj.getClass(), "element", true);
            f.set(obj, setModifiedItemTimes((Element) f.get(obj)));
        } catch (Exception e) {
        }
    }

    private Element setNewItemTimes(Element e) {
        Date now = getNowAsUTC();
        setElementDate(e, CREATION_TIME_ELEMENT_NAME, now);
        setElementDate(e, LAST_ACCESS_TIME_ELEMENT_NAME, now);
        setElementDate(e, LAST_MODIFICATION_TIME_ELEMENT_NAME, now);
        return e;
    }

    private Element setModifiedItemTimes(Element e) {
        Date now = getNowAsUTC();
        setElementDate(e, LAST_ACCESS_TIME_ELEMENT_NAME, now);
        setElementDate(e, LAST_MODIFICATION_TIME_ELEMENT_NAME, now);
        return e;
    }

    static void setElementDate(Element parentElement, String elementPath, Date d) {
        try {
            Element e = (Element) xpath.evaluate(elementPath, parentElement, XPathConstants.NODE);
            if (e != null) {
                e.setTextContent(dateFormatter.format(d));
            }
        } catch (Exception e) {
        }
    }
}
