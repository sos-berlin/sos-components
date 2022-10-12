package com.sos.joc.xmleditor.impl;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class ACommonResourceImpl extends JOCResourceImpl {

    public enum Role {
        VIEW, MANAGE
    }

    public boolean getPermission(String accessToken, ObjectType type, Role role) {
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
        return permissions;
    }

    public JOCDefaultResponse initPermissions(String accessToken, ObjectType type, Role role) {
        return initPermissions(null, getPermission(accessToken, type, role));
    }
}
