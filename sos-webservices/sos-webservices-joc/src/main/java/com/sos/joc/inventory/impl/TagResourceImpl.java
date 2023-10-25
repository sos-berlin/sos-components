package com.sos.joc.inventory.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.impl.common.AReadTag;
import com.sos.joc.inventory.resource.ITagResource;
import com.sos.joc.model.inventory.common.RequestTag;
import com.sos.joc.model.inventory.common.ResponseTag;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class TagResourceImpl extends AReadTag implements ITagResource {

    @Override
    public JOCDefaultResponse readTag(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestTag.class);
            RequestTag in = Globals.objectMapper.readValue(inBytes, RequestTag.class);

            boolean permission = getJocPermissions(accessToken).getInventory().getView();
            JOCDefaultResponse response = initPermissions(null, permission);
            if (response == null) {
                ResponseTag tag = readTag(in, IMPL_PATH);
                response = JOCDefaultResponse.responseStatus200(tag);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse readTrashTag(final String accessToken, final byte[] inBytes) {
        try {
            initLogging(TRASH_IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, RequestTag.class);
            RequestTag in = Globals.objectMapper.readValue(inBytes, RequestTag.class);

            boolean permission = getJocPermissions(accessToken).getInventory().getView();
            JOCDefaultResponse response = initPermissions(null, permission);
            if (response == null) {
                ResponseTag tag = readTag(in, TRASH_IMPL_PATH);
                response = JOCDefaultResponse.responseStatus200(tag);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
