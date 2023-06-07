package com.sos.commons.credentialstore.keepass.db.kdbx.jaxb;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.linguafranca.pwdb.Entry;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbDatabase;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbEntry;
import org.linguafranca.pwdb.kdbx.jaxb.JaxbGroup;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbEntryBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.JaxbGroupBinding;
import org.linguafranca.pwdb.kdbx.jaxb.binding.Times;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.credentialstore.keepass.SOSKeePassDatabase;
import com.sos.commons.credentialstore.keepass.db.kdbx.ASOSKdbxHandler;
import com.sos.commons.credentialstore.keepass.exceptions.SOSKeePassDatabaseException;

public class SOSKdbxJAXBHandler extends ASOSKdbxHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKdbxJAXBHandler.class);

    @Override
    protected JaxbDatabase load() throws SOSKeePassDatabaseException {
        try (InputStream is = Files.newInputStream(getKeePassFile())) {
            return JaxbDatabase.load(getCredentials(), is);
        } catch (Throwable ex) {
            throw new SOSKeePassDatabaseException(String.format("[%s]%s", SOSKeePassDatabase.getFilePath(getKeePassFile()), ex.toString()), ex);
        }
    }

    @Override
    public Entry<?, ?, ?, ?> createEntry(String[] entryPath) throws SOSKeePassDatabaseException {
        String method = "createEntry";

        JaxbDatabase d = (JaxbDatabase) getDatabase();
        JaxbGroup lastGroup = d.getRootGroup();
        JaxbEntry entry = null;

        for (int i = 2; i < entryPath.length; i++) {
            String name = entryPath[i];

            if (i == entryPath.length - 1) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][addEntry]%s", method, name));
                }
                entry = addEntry(d, lastGroup, name);
            } else {
                List<? extends JaxbGroup> result = lastGroup.findGroups(name);
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

    private JaxbEntry addEntry(JaxbDatabase d, JaxbGroup parentGroup, String entryName) {
        JaxbEntry e = d.newEntry(entryName);
        e.setIcon(d.newIcon(ICON_INDEX_NEW_ENTRY));
        setNewEntryTimesAsUTC(e);

        JaxbEntry entry = parentGroup.addEntry(e);
        setModifiedGroupTimesAsUTC(parentGroup);

        return entry;
    }

    private JaxbGroup addGroup(JaxbDatabase d, JaxbGroup parentGroup, String groupName) {
        JaxbGroup g = d.newGroup(groupName);
        g.setIcon(d.newIcon(ICON_INDEX_NEW_GROUP));
        setNewGroupTimesAsUTC(g);

        JaxbGroup group = parentGroup.addGroup(g);
        setModifiedGroupTimesAsUTC(parentGroup);
        return group;
    }

    private void setNewEntryTimesAsUTC(Object obj) {
        try {
            Field f = FieldUtils.getField(obj.getClass(), "delegate", true);

            JaxbEntryBinding b = (JaxbEntryBinding) f.get(obj);
            b.setTimes(setNewItemTimes(b.getTimes()));

            f.set(obj, b);
        } catch (Throwable e) {
        }
    }

    private void setNewGroupTimesAsUTC(Object obj) {
        try {
            Field f = FieldUtils.getField(obj.getClass(), "delegate", true);

            JaxbGroupBinding b = (JaxbGroupBinding) f.get(obj);
            b.setTimes(setNewItemTimes(b.getTimes()));

            f.set(obj, b);
        } catch (Throwable e) {
        }
    }

    private void setModifiedGroupTimesAsUTC(Object obj) {
        try {
            Field f = FieldUtils.getField(obj.getClass(), "delegate", true);

            JaxbGroupBinding b = (JaxbGroupBinding) f.get(obj);
            b.setTimes(setModifiedItemTimes(b.getTimes()));

            f.set(obj, b);
        } catch (Throwable e) {
        }
    }

    private Times setNewItemTimes(Times t) {
        t.setCreationTime(getNowAsUTC());
        t.setLastAccessTime(t.getCreationTime());
        t.setLastModificationTime(t.getCreationTime());
        return t;
    }

    private Times setModifiedItemTimes(Times t) {
        t.setLastAccessTime(getNowAsUTC());
        t.setLastModificationTime(t.getLastAccessTime());
        return t;
    }
}
