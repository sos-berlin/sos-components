package com.sos.auth.classes;

public class SOSInitialPasswordSetting {

    String initialPassword;
    Long mininumPasswordLength;

    public String getInitialPassword() {
        if (initialPassword == null) {
            return "initial";
        }
        return initialPassword;
    }

    public void setInitialPassword(String initialPassword) {
        this.initialPassword = initialPassword;
    }

    public Long getMininumPasswordLength() {
        return mininumPasswordLength;
    }

    public void setMininumPasswordLength(Long mininumPasswordLength) {
        this.mininumPasswordLength = mininumPasswordLength;
    }

    public boolean isMininumPasswordLength(String password) {
        return (mininumPasswordLength == 0L) || "********".equals(password) || (password.length() >= mininumPasswordLength);
    }

}
