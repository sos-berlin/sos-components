package com.sos.yade.engine.delegators;

public class YADETargetProviderFile extends YADEProviderFile {

    private String tmpFullPath;// atomic during transfer

    public YADETargetProviderFile(String fullPath, long size, long lastModifiedMillis, boolean checkSteady) {
        super(fullPath, size, lastModifiedMillis, checkSteady);
    }

    public void setTmpFullPath(String val) {
        tmpFullPath = val;
    }

    public String getTmpFullPath() {
        return tmpFullPath;
    }

}
