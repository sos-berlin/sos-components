package com.sos.js7.converter.autosys.common.v12.job.attr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.commons.util.SOSString;

public class CommonJobResource {

    /** FREE=A: Automatic release after job completion. <br/>
     * FREE=B: Release only on success. <br/>
     * FREE=C: Release only on failure. <br/>
     * FREE=I: Release on inactivity. <br/>
     * FREE=N: No automatic release. <br/>
     * FREE=K: Release after termination by user.<br/>
     * FREE=Y: Release after all jobs in the box are completed. */
    public enum FREE {
        A, B, C, I, N, K, Y
    }

    private String name;
    private int quantity;
    private FREE free;
    private String original;

    public CommonJobResource(String val) throws Exception {
        this.original = val;
        parseResource(val);
    }

    private void parseResource(String val) throws Exception {
        Pattern pattern = Pattern.compile("\\(([^,]+),\\s*quantity=(\\d+)(?:,\\s*FREE=([A-Z]))?\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(val);

        if (matcher.find()) {
            this.name = matcher.group(1).trim();
            this.quantity = Integer.parseInt(matcher.group(2).trim());
            String freeValue = matcher.group(3);
            if (!SOSString.isEmpty(freeValue)) {
                this.free = FREE.valueOf(freeValue.trim());
            }
        } else {
            throw new Exception("Invalid resource format=" + val);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public FREE getFree() {
        return free;
    }

    public void setFree(FREE free) {
        this.free = free;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(name);
        sb.append(",quantity=").append(quantity);
        if (free != null) {
            sb.append(",free=").append(free);
        }
        return sb.toString();
    }

}
