package com.sos.joc.xmleditor.impl;

import java.util.stream.Stream;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class ACommonResourceImpl extends JOCResourceImpl {

    public enum Role {
        VIEW, MANAGE
    }

    public Stream<Boolean> getPermission(String accessToken, ObjectType type, Role role) {
        Stream<Boolean> permissions = Stream.of(false, false);
        switch (type) {
        case YADE:
            switch (role) {
            case VIEW:
                permissions = Stream.of(getBasicJocPermissions(accessToken).getFileTransfer().getView(), false);
                break;
            case MANAGE:
                permissions = getJocPermissions(accessToken).map(p -> p.getFileTransfer().getManage());
                break;
            }
            break;
        case NOTIFICATION:
            switch (role) {
            case VIEW:
                permissions = Stream.of(getBasicJocPermissions(accessToken).getNotification().getView(), false);
                break;
            case MANAGE:
                permissions = getJocPermissions(accessToken).map(p -> p.getNotification().getManage());
                break;
            }
            break;
        case OTHER:
            switch (role) {
            case VIEW:
                permissions = Stream.of(getBasicJocPermissions(accessToken).getOthers().getView(), false);
                break;
            case MANAGE:
                permissions = getJocPermissions(accessToken).map(p -> p.getOthers().getManage());
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
