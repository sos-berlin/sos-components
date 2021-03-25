package com.sos.joc.yade.common;

import java.nio.file.Paths;

import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.yade.FileTransferState;
import com.sos.joc.model.yade.FileTransferStateText;
import com.sos.joc.model.yade.TransferFile;
import com.sos.yade.commons.Yade.TransferEntryState;

public class TransferFileUtils {

    public static TransferFile getFile(DBItemYadeFile item) {
        TransferFile file = new TransferFile();
        if (item.getErrorMessage() != null && !item.getErrorMessage().isEmpty()) {
            Err error = new Err();
            error.setMessage(item.getErrorMessage());
            file.setError(error);
        }
        file.setId(item.getId());
        file.setIntegrityHash(item.getIntegrityHash());
        file.setInterventionTransferId(0L);// file.getInterventionTransferId()
        file.setModificationDate(item.getModificationDate());
        file.setSize(item.getSize());
        file.setSourceName(Paths.get(item.getSourcePath()).getFileName().toString());
        file.setSourcePath(item.getSourcePath());
        if (item.getTargetPath() != null && !item.getTargetPath().isEmpty()) {
            file.setTargetName(Paths.get(item.getTargetPath()).getFileName().toString());
            file.setTargetPath(item.getTargetPath());
        }
        file.setTransferId(item.getTransferId());
        file.setState(getState(TransferEntryState.fromValue(item.getState())));
        // no Created-Date in DB, therefore use ModificationDate as surveyDate also
        file.setSurveyDate(item.getModificationDate());
        return file;
    }

    public static FileTransferState getState(TransferEntryState val) {
        FileTransferState state = new FileTransferState();
        switch (val) {
        // severity=0
        case TRANSFERRED:
            state.set_text(FileTransferStateText.TRANSFERRED);
            state.setSeverity(0);
            break;
        case COMPRESSED:
            state.set_text(FileTransferStateText.COMPRESSED);
            state.setSeverity(0);
            break;
        case RENAMED:
            state.set_text(FileTransferStateText.RENAMED);
            state.setSeverity(0);
            break;
        case MOVED:
            state.set_text(FileTransferStateText.RENAMED);
            state.setSeverity(0);
            break;
        // severity=1
        case NOT_OVERWRITTEN:
            state.set_text(FileTransferStateText.NOT_OVERWRITTEN);
            state.setSeverity(1);
            break;
        case IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT:
            state.set_text(FileTransferStateText.IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT);
            state.setSeverity(1);
            break;
        case SKIPPED:
            state.set_text(FileTransferStateText.SKIPPED);
            state.setSeverity(1);
            break;
        // severity=2
        case FAILED:
            state.set_text(FileTransferStateText.FAILED);
            state.setSeverity(2);
            break;
        case ABORTED:
            state.set_text(FileTransferStateText.ABORTED);
            state.setSeverity(2);
            break;
        case DELETED:
            state.set_text(FileTransferStateText.DELETED);
            state.setSeverity(2);
            break;
        // severity=3
        case UNKNOWN:
            state.set_text(FileTransferStateText.UNDEFINED);
            state.setSeverity(3);
            break;
        // severity=5
        case WAITING:
            state.set_text(FileTransferStateText.WAITING);
            state.setSeverity(5);
            break;
        case TRANSFERRING:
            state.set_text(FileTransferStateText.TRANSFERRING);
            state.setSeverity(5);
            break;
        case IN_PROGRESS:
            state.set_text(FileTransferStateText.IN_PROGRESS);
            state.setSeverity(5);
            break;
        case ROLLED_BACK:
            state.set_text(FileTransferStateText.ROLLED_BACK);
            state.setSeverity(5);
            break;
        case POLLING:
            state.set_text(FileTransferStateText.POLLING);
            state.setSeverity(5);
            break;
        default:
            break;
        }
        return state;
    }

    public static Integer getState(FileTransferStateText val) {
        switch (val) {
        case UNDEFINED:
            return TransferEntryState.UNKNOWN.intValue();
        case WAITING:
            return TransferEntryState.WAITING.intValue();
        case TRANSFERRING:
            return TransferEntryState.TRANSFERRING.intValue();
        case IN_PROGRESS:
            return TransferEntryState.IN_PROGRESS.intValue();
        case TRANSFERRED:
        case SUCCESS:
            return TransferEntryState.TRANSFERRED.intValue();
        case SKIPPED:
            return TransferEntryState.SKIPPED.intValue();
        case FAILED:
            return TransferEntryState.FAILED.intValue();
        case ABORTED:
            return TransferEntryState.ABORTED.intValue();
        case COMPRESSED:
            return TransferEntryState.COMPRESSED.intValue();
        case NOT_OVERWRITTEN:
            return TransferEntryState.NOT_OVERWRITTEN.intValue();
        case DELETED:
            return TransferEntryState.DELETED.intValue();
        case RENAMED:
            return TransferEntryState.RENAMED.intValue();
        case IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT:
            return TransferEntryState.IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT.intValue();
        case ROLLED_BACK:
            return TransferEntryState.ROLLED_BACK.intValue();
        case POLLING:
            return TransferEntryState.POLLING.intValue();
        }
        return null;
    }

}