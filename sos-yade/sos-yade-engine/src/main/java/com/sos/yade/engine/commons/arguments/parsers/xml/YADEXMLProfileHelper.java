package com.sos.yade.engine.commons.arguments.parsers.xml;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSComparisonOperator;
import com.sos.commons.vfs.commons.AProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPProviderArguments;
import com.sos.commons.vfs.ftp.commons.FTPSProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPProviderArguments;
import com.sos.commons.vfs.http.commons.HTTPSProviderArguments;
import com.sos.commons.vfs.local.commons.LocalProviderArguments;
import com.sos.commons.vfs.smb.commons.SMBProviderArguments;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.commons.vfs.webdav.commons.WebDAVProviderArguments;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.arguments.YADESourcePollingArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;

public class YADEXMLProfileHelper {

    protected static void parse(YADEXMLParser impl, Node profile) throws Exception {
        NodeList nl = profile.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Operation":
                    parseOperation(impl, n);
                    break;
                case "SystemPropertyFiles":
                    parseSystemPropertyFiles(impl, n);
                    break;
                case "Client":
                case "JobScheduler":
                case "Logging":
                case "NotificationTriggers":
                case "Notifications":
                case "Assertions":
                case "Documentation":
                default:
                    break;
                }
            }
        }
    }

    private static void parseOperation(YADEXMLParser impl, Node operation) throws Exception {
        Node node = impl.getXPath().selectNode(operation, "*[1]"); // first child
        if (node == null) {
            throw new SOSMissingDataException("Profiles/Profile profile_id=" + impl.getArgs().getProfile().getValue() + "/Operation/<Child Node>");
        }
        String operationIdentifier = node.getNodeName();
        switch (operationIdentifier) {
        case "Copy":
            impl.getArgs().getOperation().setValue(TransferOperation.COPY);
            parseOperationOnSourceTarget(impl, node, operationIdentifier);
            break;
        case "Move":
            impl.getArgs().getOperation().setValue(TransferOperation.MOVE);
            parseOperationOnSourceTarget(impl, node, operationIdentifier);
            break;
        case "Remove":
            impl.getArgs().getOperation().setValue(TransferOperation.REMOVE);
            impl.nullifyTargetArgs();
            parseOperationOnSource(impl, node, operationIdentifier);
            break;
        case "GetList":
            impl.getArgs().getOperation().setValue(TransferOperation.GETLIST);
            impl.nullifyTargetArgs();
            parseOperationOnSource(impl, node, operationIdentifier);
            break;
        default:
            throw new Exception("[" + node.getNodeName() + "]Unknown Operation");
        }
    }

    private static void parseSystemPropertyFiles(YADEXMLParser impl, Node systemPropertyFiles) {
        NodeList nl = systemPropertyFiles.getChildNodes();
        if (nl == null) {
            return;
        }
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && "SystemPropertyFile".equals(n.getNodeName())) {
                files.add(Path.of(impl.getValue(n)));
            }
        }
        if (files.size() > 0) {
            impl.getClientArgs().getSystemPropertyFiles().setValue(files);
        }
    }

    private static void parseOperationOnSourceTarget(YADEXMLParser impl, Node operation, String operationIdentifier) throws Exception {
        impl.initializeTargetArgsIfNull();

        NodeList nl = operation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                if (nodeName.equals(operationIdentifier + "Source")) {
                    parseSource(impl, n, operationIdentifier);
                } else if (nodeName.equals(operationIdentifier + "Target")) {
                    parseTarget(impl, n, operationIdentifier);
                } else if (nodeName.equals("TransferOptions")) {
                    parseTransferOptions(impl, n);
                }
            }
        }
    }

    private static void parseOperationOnSource(YADEXMLParser impl, Node operation, String operationIdentifier) throws Exception {
        NodeList nl = operation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals(operationIdentifier + "Source")) {
                    parseSource(impl, n, operationIdentifier);
                }
            }
        }
    }

    private static void parseSource(YADEXMLParser impl, Node sourceOperation, String operationIdentifier) throws Exception {
        NodeList nl = sourceOperation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                // e.g. CopySourceFragmentRef
                if (nodeName.equals(operationIdentifier + "SourceFragmentRef")) {
                    parseFragmentRef(impl, n, true);
                } else if (nodeName.equals("Alternative" + operationIdentifier + "SourceFragmentRef")) {
                    parseAlternativeFragmentRef(impl, n, true);
                } else if (nodeName.equals("SourceFileOptions")) {
                    parseSourceOptions(impl, n);
                }
            }
        }
    }

    private static void parseTarget(YADEXMLParser impl, Node targetOperation, String operationIdentifier) throws Exception {
        NodeList nl = targetOperation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                // e.g. CopyTargetFragmentRef
                if (nodeName.equals(operationIdentifier + "TargetFragmentRef")) {
                    parseFragmentRef(impl, n, false);
                } else if (nodeName.equals("Alternative" + operationIdentifier + "TargetFragmentRef")) {
                    parseAlternativeFragmentRef(impl, n, false);
                } else if (nodeName.equals("Directory")) {
                    impl.setStringArgumentValue(impl.getTargetArgs().getDirectory(), n);
                } else if (nodeName.equals("TargetFileOptions")) {
                    parseTargetOptions(impl, n);
                }
            }
        }
    }

    private static void parseTransferOptions(YADEXMLParser impl, Node transferOptions) throws Exception {
        NodeList nl = transferOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BufferSize":
                    impl.setIntegerArgumentValue(impl.getArgs().getBufferSize(), n);
                    break;
                case "Transactional":
                    impl.setBooleanArgumentValue(impl.getTargetArgs().getTransactional(), n);
                    break;
                case "RetryOnConnectionError":
                    parseTransferOptionsRetryOnConnectionError(impl, n);
                    break;
                }
            }
        }
    }

    private static void parseAlternativeFragmentRef(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        // YADEEngine - not implemented
    }

    private static void parseFragmentRef(YADEXMLParser impl, Node ref, boolean isSource) throws Exception {
        YADESourceTargetArguments sourceTargetArgs;
        if (isSource) {
            sourceTargetArgs = impl.getSourceArgs();
        } else {
            sourceTargetArgs = impl.getTargetArgs();
        }

        AProviderArguments providerArgs = null;
        // e.g. ref=CopySourceFragmentRef, refRef SFTPFragmentRef
        Node refRef = impl.getXPath().selectNode(ref, "*[1]"); // first child
        if (refRef == null) {
            throw new SOSMissingDataException("Profiles/Profile profile_id=" + impl.getArgs().getProfile().getValue() + "/../" + ref.getNodeName()
                    + "/<Child Node>");
        }
        switch (refRef.getNodeName()) {
        case "LocalSource":
            providerArgs = parseFragmentRefLocal(impl, refRef, isSource, sourceTargetArgs);
            break;
        case "LocalTarget":
            providerArgs = parseFragmentRefLocal(impl, refRef, isSource, sourceTargetArgs);
            break;
        case "SFTPFragmentRef":
            providerArgs = parseFragmentRefSFTP(impl, refRef, isSource, sourceTargetArgs);
            break;
        case "FTPFragmentRef":
            providerArgs = parseFragmentRefFTP(impl, refRef, isSource, sourceTargetArgs);
            break;
        case "FTPSFragmentRef":
            providerArgs = parseFragmentRefFTPS(impl, refRef, isSource, sourceTargetArgs);
            break;
        case "HTTPFragmentRef":
            providerArgs = parseFragmentRefHTTP(impl, refRef, isSource, sourceTargetArgs);
            break;
        case "HTTPSFragmentRef":
            providerArgs = parseFragmentRefHTTPS(impl, refRef, isSource, sourceTargetArgs);
            break;
        case "SMBFragmentRef":
            providerArgs = parseFragmentRefSMB(impl, refRef, isSource, sourceTargetArgs);
            break;
        case "WebDAVFragmentRef":
            providerArgs = parseFragmentRefWebDAV(impl, refRef, isSource, sourceTargetArgs);
            break;
        }
        if (isSource) {
            impl.getSourceArgs().setProvider(providerArgs);
        } else {
            impl.getTargetArgs().setProvider(providerArgs);
        }
    }

    private static LocalProviderArguments parseFragmentRefLocal(YADEXMLParser impl, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        LocalProviderArguments args = new LocalProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "LocalPreProcessing":
                    parseFragmentRefPreProcessing(impl, n, sourceTargetArgs);
                    break;
                case "LocalPostProcessing":
                    parseFragmentRefPostProcessing(impl, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    impl.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(impl, n, sourceTargetArgs);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(impl, n, isSource, args);
                    break;
                }
            }
        }
        return args;
    }

    private static FTPProviderArguments parseFragmentRefFTP(YADEXMLParser impl, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        FTPProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseFTP(impl, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FTPPreProcessing":
                    parseFragmentRefPreProcessing(impl, n, sourceTargetArgs);
                    break;
                case "FTPPostProcessing":
                    parseFragmentRefPostProcessing(impl, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    impl.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(impl, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static FTPSProviderArguments parseFragmentRefFTPS(YADEXMLParser impl, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        FTPSProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseFTPS(impl, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FTPPreProcessing":
                    parseFragmentRefPreProcessing(impl, n, sourceTargetArgs);
                    break;
                case "FTPPostProcessing":
                    parseFragmentRefPostProcessing(impl, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    impl.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(impl, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static HTTPProviderArguments parseFragmentRefHTTP(YADEXMLParser impl, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        HTTPProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseHTTP(impl, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Rename":
                    parseFragmentRefRename(impl, n, sourceTargetArgs);
                    break;
                case "HTTPHeaders":
                    YADEXMLFragmentsProtocolFragmentHelper.parseHTTPHeaders(impl, args, n);
                    break;
                }
            }
        }
        return args;
    }

    private static HTTPSProviderArguments parseFragmentRefHTTPS(YADEXMLParser impl, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        HTTPSProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseHTTPS(impl, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Rename":
                    parseFragmentRefRename(impl, n, sourceTargetArgs);
                    break;
                case "HTTPHeaders":
                    YADEXMLFragmentsProtocolFragmentHelper.parseHTTPHeaders(impl, args, n);
                    break;
                }
            }
        }
        return args;
    }

    private static SSHProviderArguments parseFragmentRefSFTP(YADEXMLParser impl, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        SSHProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseSFTP(impl, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SFTPPreProcessing":
                    parseFragmentRefPreProcessing(impl, n, sourceTargetArgs);
                    break;
                case "SFTPPostProcessing":
                    parseFragmentRefPostProcessing(impl, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    impl.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(impl, n, sourceTargetArgs);
                    break;
                case "ZlibCompression":
                    args.getUseZlibCompression().setValue(Boolean.valueOf(true));
                    break;
                }
            }
        }
        return args;
    }

    private static SMBProviderArguments parseFragmentRefSMB(YADEXMLParser impl, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        SMBProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseSMB(impl, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SMBPreProcessing":
                    parseFragmentRefPreProcessing(impl, n, sourceTargetArgs);
                    break;
                case "SMBPostProcessing":
                    parseFragmentRefPostProcessing(impl, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    impl.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(impl, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static WebDAVProviderArguments parseFragmentRefWebDAV(YADEXMLParser impl, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        WebDAVProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseWebDAV(impl, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "WebDAVPostProcessing":
                    parseFragmentRefPostProcessing(impl, n, sourceTargetArgs);
                    break;
                case "Rename":
                    parseFragmentRefRename(impl, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static void parseSourceOptions(YADEXMLParser impl, Node sourceFileOptions) throws Exception {
        NodeList nl = sourceFileOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Selection":
                    parseSourceOptionSelection(impl, n);
                    break;
                case "CheckSteadyState":
                    parseSourceOptionCheckSteadyState(impl, n);
                    break;
                case "Directives":
                    parseSourceOptionDirectives(impl, n);
                    break;
                case "Polling":
                    parseSourceOptionPolling(impl, n);
                    break;
                case "ResultSet":
                    parseSourceOptionResultSet(impl, n);
                    break;
                case "MaxFiles":
                    impl.setIntegerArgumentValue(impl.getSourceArgs().getMaxFiles(), n);
                    break;
                case "CheckIntegrityHash":
                    parseSourceOptionCheckIntegrityHash(impl, n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelection(YADEXMLParser impl, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FilePathSelection":
                    parseSourceOptionSelectionFilePath(impl, n);
                    break;
                case "FileSpecSelection":
                    parseSourceOptionSelectionFileSpec(impl, n);
                    break;
                case "FileListSelection":
                    parseSourceOptionSelectionFileList(impl, n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFilePath(YADEXMLParser impl, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FilePath":
                    impl.getSourceArgs().setFilePath(impl.getValue(n));
                    break;
                case "Directory":
                    impl.setStringArgumentValue(impl.getSourceArgs().getDirectory(), n);
                    break;
                case "Recursive":
                    impl.setBooleanArgumentValue(impl.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFileSpec(YADEXMLParser impl, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FileSpec":
                    impl.setStringArgumentValue(impl.getSourceArgs().getFileSpec(), n);
                    break;
                case "Directory":
                    impl.setStringArgumentValue(impl.getSourceArgs().getDirectory(), n);
                    break;
                case "ExcludedDirectories":
                    impl.setStringArgumentValue(impl.getSourceArgs().getExcludedDirectories(), n);
                    break;
                case "Recursive":
                    impl.setBooleanArgumentValue(impl.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFileList(YADEXMLParser impl, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FileList":
                    impl.setPathArgumentValue(impl.getSourceArgs().getFileList(), n);
                    break;
                case "Directory":
                    impl.setStringArgumentValue(impl.getSourceArgs().getDirectory(), n);
                    break;
                case "Recursive":
                    impl.setBooleanArgumentValue(impl.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionCheckSteadyState(YADEXMLParser impl, Node steadyState) throws Exception {
        NodeList nl = steadyState.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CheckSteadyStateInterval":
                    impl.setStringArgumentValue(impl.getSourceArgs().getCheckSteadyStateInterval(), n);
                    break;
                case "CheckSteadyStateCount":
                    impl.setIntegerArgumentValue(impl.getSourceArgs().getCheckSteadyCount(), n);
                    break;
                case "CheckSteadyStateErrorState":// for JS1 job chain
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionDirectives(YADEXMLParser impl, Node directives) throws Exception {
        NodeList nl = directives.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "DisableErrorOnNoFilesFound":
                    impl.setOppositeBooleanArgumentValue(impl.getSourceArgs().getForceFiles(), n);
                    break;
                case "TransferZeroByteFiles":
                    impl.getSourceArgs().setZeroByteTransfer(impl.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionPolling(YADEXMLParser impl, Node polling) throws Exception {
        NodeList nl = polling.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            YADESourcePollingArguments pollingArgs = new YADESourcePollingArguments();
            pollingArgs.applyDefaultIfNullQuietly();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "PollInterval":
                        impl.setStringArgumentValue(pollingArgs.getPollInterval(), n);
                        break;
                    case "PollTimeout":
                        impl.setIntegerArgumentValue(pollingArgs.getPollTimeout(), n);
                        break;
                    case "MinFiles":
                        impl.setIntegerArgumentValue(pollingArgs.getPollMinFiles(), n);
                        break;
                    case "WaitForSourceFolder":
                        impl.setBooleanArgumentValue(pollingArgs.getWaitingForLateComers(), n);
                        break;
                    case "PollErrorState":
                        // YADE 1 - YADE Job API
                        break;
                    case "PollingServer":
                        impl.setBooleanArgumentValue(pollingArgs.getPollingServer(), n);
                        break;
                    case "PollingServerDuration":
                        impl.setStringArgumentValue(pollingArgs.getPollingServerDuration(), n);
                        break;
                    case "PollForever":
                        impl.setBooleanArgumentValue(pollingArgs.getPollingServerPollForever(), n);
                        break;
                    }
                }
            }
            impl.getSourceArgs().setPolling(pollingArgs);
        }
    }

    private static void parseSourceOptionResultSet(YADEXMLParser impl, Node resultSet) throws Exception {
        NodeList nl = resultSet.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "ResultSetFile":
                        impl.setPathArgumentValue(impl.getClientArgs().getResultSetFileName(), n);
                        break;
                    case "CheckResultSetCount":
                        parseSourceOptionResultSetCheckCount(impl, n);
                        break;
                    case "EmptyResultSetState":
                        // YADE 1 - YADE Job API
                        break;
                    }
                }
            }
        }
    }

    private static void parseSourceOptionResultSetCheckCount(YADEXMLParser impl, Node resultSetCheckCount) throws Exception {
        NodeList nl = resultSetCheckCount.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "ExpectedResultSetCount":
                        impl.setIntegerArgumentValue(impl.getClientArgs().getExpectedSizeOfResultSet(), n);
                        break;
                    case "RaiseErrorIfResultSetIs":
                        SOSComparisonOperator comparisonOperator = SOSComparisonOperator.fromString(impl.getValue(n));
                        if (comparisonOperator != null) {
                            impl.getClientArgs().getRaiseErrorIfResultSetIs().setValue(comparisonOperator);
                        }
                        break;
                    }
                }
            }
        }
    }

    private static void parseSourceOptionCheckIntegrityHash(YADEXMLParser impl, Node checkIntegrityHash) throws Exception {
        impl.getSourceArgs().getCheckIntegrityHash().setValue(Boolean.valueOf(true));

        NodeList nl = checkIntegrityHash.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "HashAlgorithm":
                        impl.setStringArgumentValue(impl.getSourceArgs().getIntegrityHashAlgorithm(), n);
                        break;
                    }
                }
            }
        }
    }

    private static void parseTargetOptions(YADEXMLParser impl, Node targetFileOptions) throws Exception {
        NodeList nl = targetFileOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AppendFiles":
                    impl.setBooleanArgumentValue(impl.getTargetArgs().getAppendFiles(), n);
                    break;
                case "Atomicity":
                    parseTargetOptionAtomicity(impl, n);
                    break;
                case "CheckSize":
                    impl.setBooleanArgumentValue(impl.getTargetArgs().getCheckSize(), n);
                    break;
                case "CumulateFiles":
                    parseTargetOptionCumulateFiles(impl, n);
                    break;
                case "CompressFiles":
                    parseTargetOptionCompressFiles(impl, n);
                    break;
                case "CreateIntegrityHashFile":
                    parseTargetOptionCreateIntegrityHashFile(impl, n);
                    break;
                case "KeepModificationDate":
                    impl.setBooleanArgumentValue(impl.getTargetArgs().getKeepModificationDate(), n);
                    break;
                case "DisableMakeDirectories":
                    impl.setOppositeBooleanArgumentValue(impl.getTargetArgs().getCreateDirectories(), n);
                    break;
                case "DisableOverwriteFiles":
                    impl.setOppositeBooleanArgumentValue(impl.getTargetArgs().getOverwriteFiles(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionAtomicity(YADEXMLParser impl, Node atomicity) throws Exception {
        NodeList nl = atomicity.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AtomicPrefix":
                    impl.setStringArgumentValue(impl.getTargetArgs().getAtomicPrefix(), n);
                    break;
                case "AtomicSuffix":
                    impl.setStringArgumentValue(impl.getTargetArgs().getAtomicSuffix(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCumulateFiles(YADEXMLParser impl, Node cumulateFiles) throws Exception {
        NodeList nl = cumulateFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CumulativeFileSeparator":
                    impl.setStringArgumentValue(impl.getTargetArgs().getCumulativeFileSeparator(), n);
                    break;
                case "CumulativeFilename":
                    impl.setStringArgumentValue(impl.getTargetArgs().getCumulativeFileName(), n);
                    break;
                case "CumulativeFileDelete":
                    impl.setBooleanArgumentValue(impl.getTargetArgs().getCumulativeFileDelete(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCompressFiles(YADEXMLParser impl, Node compressFiles) throws Exception {
        NodeList nl = compressFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CompressedFileExtension":
                    impl.setStringArgumentValue(impl.getTargetArgs().getCompressedFileExtension(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCreateIntegrityHashFile(YADEXMLParser impl, Node createIntegrityHashFile) throws Exception {
        impl.getTargetArgs().getCreateIntegrityHashFile().setValue(Boolean.valueOf(true));

        NodeList nl = createIntegrityHashFile.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "HashAlgorithm":
                    impl.setStringArgumentValue(impl.getTargetArgs().getIntegrityHashAlgorithm(), n);
                    break;
                }
            }
        }
    }

    private static void parseFragmentRefRename(YADEXMLParser impl, Node rename, YADESourceTargetArguments sourceTargetArgs) throws Exception {
        NodeList nl = rename.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "ReplaceWhat":
                    impl.setStringArgumentValue(sourceTargetArgs.getReplacing(), n);
                    break;
                case "ReplaceWith":
                    impl.setStringArgumentValue(sourceTargetArgs.getReplacement(), n);
                    break;
                }
            }
        }
    }

    private static void parseFragmentRefPreProcessing(YADEXMLParser impl, Node preProcessing, YADESourceTargetArguments sourceTargetArgs)
            throws Exception {
        NodeList nl = preProcessing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CommandBeforeFile":
                    sourceTargetArgs.getCommands().setCommandsBeforeFile(impl.getValue(n));
                    break;
                case "CommandBeforeOperation":
                    sourceTargetArgs.getCommands().setCommandsBeforeOperation(impl.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseFragmentRefPostProcessing(YADEXMLParser impl, Node postProcessing, YADESourceTargetArguments sourceTargetArgs)
            throws Exception {
        NodeList nl = postProcessing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CommandAfterFile":
                    sourceTargetArgs.getCommands().setCommandsAfterFile(impl.getValue(n));
                    break;
                case "CommandAfterOperationOnSuccess":
                    sourceTargetArgs.getCommands().setCommandsAfterOperationOnSuccess(impl.getValue(n));
                    break;
                case "CommandAfterOperationOnError":
                    sourceTargetArgs.getCommands().setCommandsAfterOperationOnError(impl.getValue(n));
                    break;
                case "CommandAfterOperationFinal":
                    sourceTargetArgs.getCommands().setCommandsAfterOperationFinal(impl.getValue(n));
                    break;
                case "CommandBeforeRename":
                    sourceTargetArgs.getCommands().setCommandsBeforeRename(impl.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseTransferOptionsRetryOnConnectionError(YADEXMLParser impl, Node retryOnConnectionError) throws Exception {
        NodeList nl = retryOnConnectionError.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "RetryCountMax":
                    impl.setIntegerArgumentValue(impl.getSourceArgs().getConnectionErrorRetryCountMax(), n);
                    impl.getTargetArgs().getConnectionErrorRetryCountMax().setValue(impl.getSourceArgs().getConnectionErrorRetryCountMax()
                            .getValue());
                    break;
                case "RetryInterval":
                    impl.setStringArgumentValue(impl.getSourceArgs().getConnectionErrorRetryInterval(), n);
                    impl.getTargetArgs().getConnectionErrorRetryInterval().setValue(impl.getSourceArgs().getConnectionErrorRetryInterval()
                            .getValue());
                    break;
                }
            }
        }
    }

}
