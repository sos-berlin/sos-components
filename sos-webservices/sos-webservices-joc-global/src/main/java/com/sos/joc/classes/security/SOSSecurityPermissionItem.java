package com.sos.joc.classes.security;

import com.sos.joc.model.security.configuration.permissions.IniPermission;

public class SOSSecurityPermissionItem {

    private Boolean excluded;
    private String permission;
    private String normalizedPermission;
    private String controllerId;
    private boolean isJocPermission;

    public SOSSecurityPermissionItem(String permission) {
        super();
        this.permission = permission.trim();
        this.isJocPermission = setIsJocPermission();
        if (this.isJocPermission) {
            this.controllerId = "";
        } else {
            this.controllerId = extractMaster();
        }
        this.normalizedPermission = normalizePermission();
    }

    public SOSSecurityPermissionItem(String controllerId, IniPermission securityConfigurationPermission) {
        super();
        this.permission = securityConfigurationPermission.getPath();
        this.excluded = securityConfigurationPermission.getExcluded();
        this.controllerId = controllerId;
        this.normalizedPermission = permission;
    }

    public String getIniValue() {
        String s = "";
        if (isExcluded()) {
            s= "-";
        }
        if (controllerId.isEmpty()) {
            return s + permission;
        } else if (permission.matches("sos:products")) {
            return s + controllerId + ":" + permission + ":controller";
        } else {
            return s + controllerId + ":" + permission;
        }
    }
    
    public String getController() {
        return controllerId;
    }

    public Boolean isExcluded() {
        return excluded == Boolean.TRUE;
    }
    
    public boolean isJocPermission() {
        return isJocPermission;
    }

    public String getPermission() {
        return permission;
    }

    public String getNormalizedPermission() {
        return normalizedPermission;
    }

    private String extractMaster() {
        String s = permission;
        if (permission.startsWith("-")) {
            s = permission.substring(1);
        }
            
        if (s.startsWith("sos:products:controller")) {
            return "";
        } else if (s.matches("(.*):sos:products")) {
            return s.replaceFirst("^(.*):sos:products$", "$1");
        } else {
            return s.replaceFirst("^(.*):sos:products:controller(?::[^:]+)*$", "$1");
        }
    }

    private String normalizePermission() {
        String s = permission;
        if (permission.startsWith("-")) {
            s = permission.substring(1);
            excluded = true;
        }
        if (!controllerId.isEmpty() && s.matches("(.*):sos:products")) {
            return "sos:products:controller";
        }
        return s.replaceFirst("^(?:.*:)*(sos:products(?::[^:]+)*)$", "$1");
    }
    
    private boolean setIsJocPermission() {
        if (permission.matches("-?sos:products")) {
           return true; 
        }
        return permission.matches("-?sos:products:joc(:[^:]+)*");
    }

}
