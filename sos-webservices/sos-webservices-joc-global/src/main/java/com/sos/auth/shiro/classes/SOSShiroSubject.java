package com.sos.auth.shiro.classes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.Subject;

import com.sos.auth.classes.SOSPermissionsCreator;
import com.sos.auth.interfaces.ISOSAuthSubject;
import com.sos.auth.interfaces.ISOSSession;

public class SOSShiroSubject implements ISOSAuthSubject {

    private Subject subject;
    private SOSShiroSession session;

    @Override
    public Boolean hasRole(String role) {
        return subject.hasRole(role);
    }

    @Override
    public Boolean isPermitted(String permission) {
        return subject.isPermitted(permission);
    }

    @Override
    public Boolean isAuthenticated() {
        return subject.isAuthenticated();
    }

    @Override
    public ISOSSession getSession() {
        if (subject == null){
            throw new InvalidSessionException();             
        }
        if (session == null) {
            session = new SOSShiroSession();
        }
        session.setShiroSession(subject.getSession(false));
        return session;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public Map<String, List<String>> getMapOfFolderPermissions() {
        SOSPermissionsCreator sosPermissionsCreator = new SOSPermissionsCreator(null);
                return sosPermissionsCreator.getMapOfFolder();
    }

    @Override
    public Boolean isForcePasswordChange() {
        return false;
    }

    @Override
    public Set<String> getListOfAccountPermissions() {
        return new HashSet<String>();
    }

}
