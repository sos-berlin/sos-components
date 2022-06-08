package com.sos.joc.xmleditor.impl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class ACommonResourceImpl extends JOCResourceImpl {

    public enum Role {
        VIEW, MANAGE
    }

    public JOCDefaultResponse initPermissions(String controllerId, String accessToken, ObjectType type, Role role) throws JsonParseException, JsonMappingException, JocException, IOException {
        boolean permissions = false;
        switch (type) {
        case YADE:
            switch (role) {
            case VIEW:
                permissions = getJocPermissions(accessToken).getFileTransfer().getView();
                break;
            case MANAGE:
                permissions = getJocPermissions(accessToken).getFileTransfer().getManage();
                break;
            }
            break;
        case NOTIFICATION:
            switch (role) {
            case VIEW:
                permissions = getJocPermissions(accessToken).getNotification().getView();
                break;
            case MANAGE:
                permissions = getJocPermissions(accessToken).getNotification().getManage();
                break;
            }
            break;
        case OTHER:
            switch (role) {
            case VIEW:
                permissions = getJocPermissions(accessToken).getOthers().getView();
                break;
            case MANAGE:
                permissions = getJocPermissions(accessToken).getOthers().getManage();
                break;
            }
            break;
        }
        return initPermissions(controllerId, permissions);
    }
}
