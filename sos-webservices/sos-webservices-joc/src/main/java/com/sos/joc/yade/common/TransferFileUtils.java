package com.sos.joc.yade.common;

import com.sos.commons.util.SOSString;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.db.yade.DBItemYadeFile;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.yade.FileTransferState;
import com.sos.joc.model.yade.FileTransferStateText;
import com.sos.joc.model.yade.TransferFile;
import com.sos.yade.commons.Yade.TransferEntryState;

public class TransferFileUtils {

    public static TransferFile getFile(DBItemYadeFile item) {
        TransferFile file = new TransferFile();
        file.setTransferId(item.getTransferId());
        file.setSourcePath(item.getSourcePath());
        file.setTargetPath(item.getTargetPath());

        if (item.getErrorMessage() != null && !item.getErrorMessage().isEmpty()) {
            Err error = new Err();
            error.setMessage(item.getErrorMessage());
            file.setError(error);
        }
        file.setId(item.getId());
        file.setIntegrityHash(item.getIntegrityHash());
        file.setModificationDate(item.getModificationDate());
        file.setSize(item.getSize());
        file.setSourceName(getBasenameFromPath(item.getSourcePath()));
        file.setSourcePath(item.getSourcePath());
        if (!SOSString.isEmpty(item.getTargetPath())) {
            file.setTargetName(getBasenameFromPath(item.getTargetPath()));
        }
        file.setState(getState(TransferEntryState.fromValue(item.getState())));
        file.setSurveyDate(item.getModificationDate());
        return file;
    }

    public static FileTransferState getState(TransferEntryState val) {
        FileTransferState state = new FileTransferState();
        switch (val) {
        // severity=FINISHED
        case TRANSFERRED:
            state.set_text(FileTransferStateText.TRANSFERRED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        case COMPRESSED:
            state.set_text(FileTransferStateText.COMPRESSED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        case RENAMED:
            state.set_text(FileTransferStateText.RENAMED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        case MOVED:
            state.set_text(FileTransferStateText.RENAMED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        case NOT_OVERWRITTEN:
            state.set_text(FileTransferStateText.NOT_OVERWRITTEN);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        case IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT:
            state.set_text(FileTransferStateText.IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        case SKIPPED:
            state.set_text(FileTransferStateText.SKIPPED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        case DELETED:
            state.set_text(FileTransferStateText.DELETED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        case ROLLED_BACK:
            state.set_text(FileTransferStateText.ROLLED_BACK);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FINISHED));
            break;
        // severity=INPROGRESS
        case WAITING:
            state.set_text(FileTransferStateText.WAITING);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.INPROGRESS));
            break;
        case TRANSFERRING:
            state.set_text(FileTransferStateText.TRANSFERRING);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.INPROGRESS));
            break;
        case IN_PROGRESS:
            state.set_text(FileTransferStateText.IN_PROGRESS);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.INPROGRESS));
            break;
        case POLLING:
            state.set_text(FileTransferStateText.POLLING);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.INPROGRESS));
            break;
        // severity=FAILED
        case FAILED:
            state.set_text(FileTransferStateText.FAILED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FAILED));
            break;
        case ABORTED:
            state.set_text(FileTransferStateText.ABORTED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FAILED));
            break;
        // severity=3
        case UNKNOWN:
            state.set_text(FileTransferStateText.UNDEFINED);
            state.setSeverity(OrdersHelper.getHistoryStateSeverity(OrderStateText.FAILED));
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

    private static String getBasenameFromPath(String path) {
        int li = path.lastIndexOf("/");
        return li > -1 ? path.substring(li + 1) : path;
    }

}