package com.sos.yade.commons;

import java.util.HashMap;
import java.util.Map;

/** TODO Rename all classes in YADE<br/>
 * - due to serialization/compatibility with YADE 1 this is currently not possible<br/>
 * -- TODO check - is that correct?<br/>
 * - alternative:<br/>
 * -- JS7 can use another argument in addition to JOB_ARGUMENT_NAME_RETURN_VALUES,<br/>
 * --- such as yade_history, and check both */
public class Yade {

    public static final String JOB_ARGUMENT_NAME_RETURN_VALUES = "yade_return_values";
    public static final String DEFAULT_ACCOUNT = ".";

    public enum TransferOperation {

        // should be synchronized with com.sos.joc.model.yade.Operation
        UNKNOWN(0), COPY(1), MOVE(2), GETLIST(3), RENAME(4), COPYTOINTERNET(5), COPYFROMINTERNET(6), REMOVE(7);

        private final Integer intValue;
        private final static Map<String, TransferOperation> CONSTANTS = new HashMap<String, TransferOperation>();
        private final static Map<Integer, TransferOperation> INTCONSTANTS = new HashMap<Integer, TransferOperation>();

        static {
            for (TransferOperation c : values()) {
                CONSTANTS.put(c.name(), c);
            }
        }

        static {
            for (TransferOperation c : values()) {
                INTCONSTANTS.put(c.intValue, c);
            }
        }

        private TransferOperation(Integer intValue) {
            this.intValue = intValue;
        }

        @Override
        public String toString() {
            return this.name();
        }

        public String value() {
            return this.name().toLowerCase();
        }

        public Integer intValue() {
            return this.intValue;
        }

        public static TransferOperation fromValue(String value) {
            TransferOperation constant = CONSTANTS.get(value.toUpperCase());
            if (constant == null) {
                return TransferOperation.UNKNOWN;
            } else {
                return constant;
            }
        }

        public static TransferOperation fromValue(Integer intValue) {
            TransferOperation constant = INTCONSTANTS.get(intValue);
            if (constant == null) {
                return TransferOperation.UNKNOWN;
            } else {
                return constant;
            }
        }
    }

    public enum TransferProtocol {

        UNKNOWN(0), LOCAL(1), FTP(2), FTPS(3), SFTP(4), HTTP(5), HTTPS(6), WEBDAV(7), WEBDAVS(8), SMB(9), SSH(100), ZIP(200), MQ(300), SMTP(
                400), IMAP(500), AZURE_BLOB_STORAGE(70);

        private final Integer intValue;
        private final static Map<String, TransferProtocol> CONSTANTS = new HashMap<String, TransferProtocol>();
        private final static Map<Integer, TransferProtocol> INTCONSTANTS = new HashMap<Integer, TransferProtocol>();

        static {
            for (TransferProtocol c : values()) {
                CONSTANTS.put(c.name(), c);
            }
        }

        static {
            for (TransferProtocol c : values()) {
                INTCONSTANTS.put(c.intValue, c);
            }
        }

        private TransferProtocol(Integer intValue) {
            this.intValue = intValue;
        }

        @Override
        public String toString() {
            return this.name();
        }

        public String value() {
            return this.name().toLowerCase();
        }

        public Integer intValue() {
            return this.intValue;
        }

        public static TransferProtocol fromValue(String value) {
            TransferProtocol constant = CONSTANTS.get(value.toUpperCase());
            if (constant == null) {
                return TransferProtocol.UNKNOWN;
            } else {
                return constant;
            }
        }

        public static TransferProtocol fromValue(Integer intValue) {
            TransferProtocol constant = INTCONSTANTS.get(intValue);
            if (constant == null) {
                return TransferProtocol.UNKNOWN;
            } else {
                return constant;
            }
        }
    }

    public enum TransferState {

        UNKNOWN(0), SUCCESSFUL(1), INCOMPLETE(2), FAILED(3);

        private final Integer intValue;
        private final static Map<String, TransferState> CONSTANTS = new HashMap<String, TransferState>();
        private final static Map<Integer, TransferState> INTCONSTANTS = new HashMap<Integer, TransferState>();

        static {
            for (TransferState c : values()) {
                CONSTANTS.put(c.name(), c);
            }
        }

        static {
            for (TransferState c : values()) {
                INTCONSTANTS.put(c.intValue, c);
            }
        }

        private TransferState(Integer intValue) {
            this.intValue = intValue;
        }

        @Override
        public String toString() {
            return this.name();
        }

        public String value() {
            return this.name().toLowerCase();
        }

        public Integer intValue() {
            return this.intValue;
        }

        public static TransferState fromValue(String value) {
            TransferState constant = CONSTANTS.get(value.toUpperCase());
            if (constant == null) {
                return TransferState.UNKNOWN;
            } else {
                return constant;
            }
        }

        public static TransferState fromValue(Integer intValue) {
            TransferState constant = INTCONSTANTS.get(intValue);
            if (constant == null) {
                return TransferState.UNKNOWN;
            } else {
                return constant;
            }
        }
    }

    public enum TransferEntryState {

        UNKNOWN(1), SELECTED(2), TRANSFERRING(3), IN_PROGRESS(4), TRANSFERRED(5), SKIPPED(6), FAILED(7), ABORTED(8), COMPRESSED(9), NOT_OVERWRITTEN(
                10), DELETED(11), RENAMED(12), IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT(13), ROLLED_BACK(14), POLLING(15), MOVED(16), SUCCESS(
                        17), ROLLBACK_FAILED(18);

        private final Integer intValue;
        private final static Map<String, TransferEntryState> CONSTANTS = new HashMap<String, TransferEntryState>();
        private final static Map<Integer, TransferEntryState> INTCONSTANTS = new HashMap<Integer, TransferEntryState>();

        static {
            for (TransferEntryState c : values()) {
                CONSTANTS.put(c.name(), c);
            }
        }

        static {
            for (TransferEntryState c : values()) {
                INTCONSTANTS.put(c.intValue, c);
            }
        }

        private TransferEntryState(Integer intValue) {
            this.intValue = intValue;
        }

        @Override
        public String toString() {
            return this.name();
        }

        public String value() {
            return this.name().toLowerCase();
        }

        public Integer intValue() {
            return this.intValue;
        }

        public static TransferEntryState fromValue(String value) {
            TransferEntryState constant = CONSTANTS.get(value.toUpperCase());
            if (constant == null) {
                return TransferEntryState.UNKNOWN;
            } else {
                return constant;
            }
        }

        public static TransferEntryState fromValue(Integer intValue) {
            TransferEntryState constant = INTCONSTANTS.get(intValue);
            if (constant == null) {
                return TransferEntryState.UNKNOWN;
            } else {
                return constant;
            }
        }
    }

}
