package com.sos.joc.yade.impl.xmleditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.DbLayerXmlEditor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.yade.FileFilter;
import com.sos.joc.model.yade.xmleditor.Profiles;
import com.sos.joc.model.yade.xmleditor.ProfilesAnswer;
import com.sos.joc.model.yade.xmleditor.common.Profile;
import com.sos.joc.yade.resource.xmleditor.IProfilesResource;
import com.sos.schema.JsonValidator;

@Path("yade")
public class ProfilesResourceImpl extends JOCResourceImpl implements IProfilesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilesResourceImpl.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    @Override
    public JOCDefaultResponse getProfiles(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validate(inBytes, FileFilter.class);
            Profiles in = Globals.objectMapper.readValue(inBytes, Profiles.class);

            checkRequiredParameters(in);
            JOCDefaultResponse response = initPermissions(in.getControllerId(), getJocPermissions(accessToken).getFileTransfer().getView());
            if (response != null) {
                return response;
            }

            ProfilesAnswer answer = new ProfilesAnswer();
            DBItemXmlEditorConfiguration item = getItem(in.getControllerId(), in.getName());
            if (item != null) {
                ArrayList<Profile> profiles = new ArrayList<Profile>();
                List<String> draftProfiles = getProfiles(item.getConfigurationDraft());

                for (int i = 0; i < draftProfiles.size(); i++) {
                    Profile profile = new Profile();
                    profile.setProfile(draftProfiles.get(i));
                    profiles.add(profile);
                }

                Comparator<Profile> comparator = new Comparator<Profile>() {

                    @Override
                    public int compare(Profile p1, Profile p2) {
                        return p1.getProfile().compareTo(p2.getProfile());
                    }
                };

                Collections.sort(profiles, comparator);

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("profiles=%s", profiles.size()));
                }

                answer.setProfiles(profiles);
            } else {
                LOGGER.debug("YADE configuration not found");
            }

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private List<String> getProfiles(String xml) {
        List<String> result = new ArrayList<String>();
        if (xml != null) {
            try {
                Document doc = SOSXML.parse(xml);
                NodeList nodes = SOSXML.newXPath().selectNodes(doc, "//Profile");
                if (nodes != null) {
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Node child = nodes.item(i);
                        if (child.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }
                        try {
                            String profileId = child.getAttributes().getNamedItem("profile_id").getNodeValue();
                            if (!SOSString.isEmpty(profileId)) {
                                result.add(profileId);
                            }
                        } catch (Throwable e) {
                            LOGGER.error(String.format("[%s]can't get attribute profile_id", child.getNodeName()), e);
                        }
                    }
                }

            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
        return result;
    }

    private void checkRequiredParameters(final Profiles in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        checkRequiredParameter("name", in.getName());
    }

    private DBItemXmlEditorConfiguration getItem(String controllerId, String name) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);

            session.beginTransaction();
            DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);
            DBItemXmlEditorConfiguration item = dbLayer.getObject(controllerId, ObjectType.YADE.name(), name);
            session.commit();
            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
