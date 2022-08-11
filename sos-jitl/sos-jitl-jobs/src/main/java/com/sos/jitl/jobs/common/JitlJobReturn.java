package com.sos.jitl.jobs.common;

import java.util.Map;

public class JitlJobReturn {

    private Integer exitCode;
    private Map<String, Object> resultMap;

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

}
