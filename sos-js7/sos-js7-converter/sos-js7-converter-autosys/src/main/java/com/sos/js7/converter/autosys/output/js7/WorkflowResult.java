package com.sos.js7.converter.autosys.output.js7;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sos.inventory.model.instruction.PostNotices;
import com.sos.js7.converter.autosys.output.js7.helper.BoardTryCatchHelper;

public class WorkflowResult {

    public enum Type {
        REAL, PSEUDO
    }

    private Path path;
    private String name;
    private String timezone;
    private boolean isAutosysTimezone;
    private boolean cycle = false;
    private List<PostNotices> postNotices;

    public WorkflowResult() {
        postNotices = new ArrayList<>();
    }

    public void setPath(Path val) {
        path = val;
    }

    public void setName(String val) {
        name = val;
    }

    public void setTimezone(String val, boolean isAutosysTimezone) {
        timezone = val;
        this.isAutosysTimezone = isAutosysTimezone;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getTimezone() {
        return timezone;
    }

    public boolean isAutosysTimezone() {
        return isAutosysTimezone;
    }

    public void setCycle() {
        cycle = true;
    }

    public boolean isCycle() {
        return cycle;
    }

    public void addPostNotices(PostNotices val) {
        addPostNotices(val, -1);
    }

    public void addPostNotices(PostNotices val, int index) {
        if (val == null || val.getNoticeBoardNames() == null || val.getNoticeBoardNames().size() == 0) {
            return;
        }
        if (index > -1) {
            postNotices.add(index, val);
        } else {
            postNotices.add(val);
        }
    }

    public void addPostNotices(BoardTryCatchHelper h) {
        if (h == null) {
            return;
        }
        addPostNotices(h.getTryPostNotices());
        addPostNotices(h.getCatchPostNotices());
    }

    public List<PostNotices> getPostNotices() {
        return postNotices;
    }

}
