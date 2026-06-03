package com.sos.auth.classes;

public class SOSInitialPasswordSetting {

    String initialPassword;
    Integer mininumPasswordLength;

    public String getInitialPassword() {
        if (initialPassword == null) {
            return SOSAuthHelper.INITIAL;
        }
        return initialPassword;
    }

    public void setInitialPassword(String initialPassword) {
        this.initialPassword = initialPassword;
    }

    public Integer getMininumPasswordLength() {
        return mininumPasswordLength;
    }

    public void setMininumPasswordLength(Integer mininumPasswordLength) {
        this.mininumPasswordLength = mininumPasswordLength;
    }

    public boolean isMininumPasswordLength(String password) {
        return (mininumPasswordLength == 0) || "********".equals(password) || (password.length() >= mininumPasswordLength);
    }

}
