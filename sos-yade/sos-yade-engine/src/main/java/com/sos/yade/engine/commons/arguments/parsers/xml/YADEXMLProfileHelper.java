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

    protected static void parse(YADEXMLArgumentsSetter argsSetter, Node profile) throws Exception {
        NodeList nl = profile.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Operation":
                    parseOperation(argsSetter, n);
                    break;
                case "SystemPropertyFiles":
                    parseSystemPropertyFiles(argsSetter, n);
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

    private static void parseOperation(YADEXMLArgumentsSetter argsSetter, Node operation) throws Exception {
        Node node = argsSetter.getXPath().selectNode(operation, "*[1]"); // first child
        if (node == null) {
            throw new SOSMissingDataException("Profiles/Profile profile_id=" + argsSetter.getArgs().getProfile().getValue()
                    + "/Operation/<Child Node>");
        }
        String operationIdentifier = node.getNodeName();
        switch (operationIdentifier) {
        case "Copy":
            argsSetter.getArgs().getOperation().setValue(TransferOperation.COPY);
            parseOperationOnSourceTarget(argsSetter, node, operationIdentifier);
            break;
        case "Move":
            argsSetter.getArgs().getOperation().setValue(TransferOperation.MOVE);
            parseOperationOnSourceTarget(argsSetter, node, operationIdentifier);
            break;
        case "Remove":
            argsSetter.getArgs().getOperation().setValue(TransferOperation.REMOVE);
            argsSetter.nullifyTargetArgs();
            parseOperationOnSource(argsSetter, node, operationIdentifier);
            break;
        case "GetList":
            argsSetter.getArgs().getOperation().setValue(TransferOperation.GETLIST);
            argsSetter.nullifyTargetArgs();
            parseOperationOnSource(argsSetter, node, operationIdentifier);
            break;
        default:
            throw new Exception("[" + node.getNodeName() + "]Unknown Operation");
        }
    }

    private static void parseSystemPropertyFiles(YADEXMLArgumentsSetter argsSetter, Node systemPropertyFiles) {
        NodeList nl = systemPropertyFiles.getChildNodes();
        if (nl == null) {
            return;
        }
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && "SystemPropertyFile".equals(n.getNodeName())) {
                files.add(Path.of(argsSetter.getValue(n)));
            }
        }
        if (files.size() > 0) {
            argsSetter.getClientArgs().getSystemPropertyFiles().setValue(files);
        }
    }

    private static void parseOperationOnSourceTarget(YADEXMLArgumentsSetter argsSetter, Node operation, String operationIdentifier) throws Exception {
        argsSetter.initializeTargetArgsIfNull();

        NodeList nl = operation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                if (nodeName.equals(operationIdentifier + "Source")) {
                    parseSource(argsSetter, n, operationIdentifier);
                } else if (nodeName.equals(operationIdentifier + "Target")) {
                    parseTarget(argsSetter, n, operationIdentifier);
                } else if (nodeName.equals("TransferOptions")) {
                    parseTransferOptions(argsSetter, n);
                }
            }
        }
    }

    private static void parseOperationOnSource(YADEXMLArgumentsSetter argsSetter, Node operation, String operationIdentifier) throws Exception {
        NodeList nl = operation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals(operationIdentifier + "Source")) {
                    parseSource(argsSetter, n, operationIdentifier);
                }
            }
        }
    }

    private static void parseSource(YADEXMLArgumentsSetter argsSetter, Node sourceOperation, String operationIdentifier) throws Exception {
        NodeList nl = sourceOperation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                // e.g. CopySourceFragmentRef
                if (nodeName.equals(operationIdentifier + "SourceFragmentRef")) {
                    parseFragmentRef(argsSetter, n, true);
                } else if (nodeName.equals("Alternative" + operationIdentifier + "SourceFragmentRef")) {
                    parseAlternativeFragmentRef(argsSetter, n, true);
                } else if (nodeName.equals("SourceFileOptions")) {
                    parseSourceOptions(argsSetter, n);
                }
            }
        }
    }

    private static void parseTarget(YADEXMLArgumentsSetter argsSetter, Node targetOperation, String operationIdentifier) throws Exception {
        NodeList nl = targetOperation.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                String nodeName = n.getNodeName();
                // e.g. CopyTargetFragmentRef
                if (nodeName.equals(operationIdentifier + "TargetFragmentRef")) {
                    parseFragmentRef(argsSetter, n, false);
                } else if (nodeName.equals("Alternative" + operationIdentifier + "TargetFragmentRef")) {
                    parseAlternativeFragmentRef(argsSetter, n, false);
                } else if (nodeName.equals("Directory")) {
                    argsSetter.setStringArgumentValue(argsSetter.getTargetArgs().getDirectory(), n);
                } else if (nodeName.equals("TargetFileOptions")) {
                    parseTargetOptions(argsSetter, n);
                }
            }
        }
    }

    private static void parseTransferOptions(YADEXMLArgumentsSetter argsSetter, Node transferOptions) throws Exception {
        NodeList nl = transferOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "BufferSize":
                    argsSetter.setIntegerArgumentValue(argsSetter.getArgs().getBufferSize(), n);
                    break;
                case "Transactional":
                    argsSetter.setBooleanArgumentValue(argsSetter.getArgs().getTransactional(), n);
                    break;
                case "RetryOnConnectionError":
                    parseTransferOptionsRetryOnConnectionError(argsSetter, n);
                    break;
                }
            }
        }
    }

    private static void parseAlternativeFragmentRef(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        // YADEEngine - not argsSetteremented
    }

    private static void parseFragmentRef(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource) throws Exception {
        YADESourceTargetArguments sourceTargetArgs;
        if (isSource) {
            sourceTargetArgs = argsSetter.getSourceArgs();
        } else {
            sourceTargetArgs = argsSetter.getTargetArgs();
        }

        AProviderArguments providerArgs = null;
        // e.g. ref=CopySourceFragmentRef, refRef SFTPFragmentRef
        Node refRef = argsSetter.getXPath().selectNode(ref, "*[1]"); // first child
        if (refRef == null) {
            throw new SOSMissingDataException("Profiles/Profile profile_id=" + argsSetter.getArgs().getProfile().getValue() + "/../" + ref
                    .getNodeName() + "/<Child Node>");
        }
        switch (refRef.getNodeName()) {
        case "LocalSource":
            providerArgs = parseFragmentRefLocal(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        case "LocalTarget":
            providerArgs = parseFragmentRefLocal(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        case "SFTPFragmentRef":
            providerArgs = parseFragmentRefSFTP(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        case "FTPFragmentRef":
            providerArgs = parseFragmentRefFTP(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        case "FTPSFragmentRef":
            providerArgs = parseFragmentRefFTPS(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        case "HTTPFragmentRef":
            providerArgs = parseFragmentRefHTTP(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        case "HTTPSFragmentRef":
            providerArgs = parseFragmentRefHTTPS(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        case "SMBFragmentRef":
            providerArgs = parseFragmentRefSMB(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        case "WebDAVFragmentRef":
            providerArgs = parseFragmentRefWebDAV(argsSetter, refRef, isSource, sourceTargetArgs);
            break;
        }
        if (isSource) {
            argsSetter.getSourceArgs().setProvider(providerArgs);
        } else {
            argsSetter.getTargetArgs().setProvider(providerArgs);
        }
    }

    private static LocalProviderArguments parseFragmentRefLocal(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        LocalProviderArguments args = new LocalProviderArguments();
        args.applyDefaultIfNullQuietly();

        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "LocalPreProcessing":
                    parseFragmentRefPreProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "LocalPostProcessing":
                    parseFragmentRefPostProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    argsSetter.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsSetter, n, sourceTargetArgs);
                    break;
                case "CredentialStoreFragmentRef":
                    YADEXMLFragmentsCredentialStoreFragmentHelper.parse(argsSetter, n, isSource, args);
                    break;
                }
            }
        }
        return args;
    }

    private static FTPProviderArguments parseFragmentRefFTP(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        FTPProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseFTP(argsSetter, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FTPPreProcessing":
                    parseFragmentRefPreProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "FTPPostProcessing":
                    parseFragmentRefPostProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    argsSetter.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsSetter, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static FTPSProviderArguments parseFragmentRefFTPS(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        FTPSProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseFTPS(argsSetter, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FTPPreProcessing":
                    parseFragmentRefPreProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "FTPPostProcessing":
                    parseFragmentRefPostProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    argsSetter.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsSetter, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static HTTPProviderArguments parseFragmentRefHTTP(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        HTTPProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseHTTP(argsSetter, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Rename":
                    parseFragmentRefRename(argsSetter, n, sourceTargetArgs);
                    break;
                case "HTTPHeaders":
                    YADEXMLFragmentsProtocolFragmentHelper.parseHTTPHeaders(argsSetter, args, n);
                    break;
                }
            }
        }
        return args;
    }

    private static HTTPSProviderArguments parseFragmentRefHTTPS(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        HTTPSProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseHTTPS(argsSetter, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Rename":
                    parseFragmentRefRename(argsSetter, n, sourceTargetArgs);
                    break;
                case "HTTPHeaders":
                    YADEXMLFragmentsProtocolFragmentHelper.parseHTTPHeaders(argsSetter, args, n);
                    break;
                }
            }
        }
        return args;
    }

    private static SSHProviderArguments parseFragmentRefSFTP(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        SSHProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseSFTP(argsSetter, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SFTPPreProcessing":
                    parseFragmentRefPreProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "SFTPPostProcessing":
                    parseFragmentRefPostProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    argsSetter.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsSetter, n, sourceTargetArgs);
                    break;
                case "ZlibCompression":
                    args.getUseZlibCompression().setValue(Boolean.valueOf(true));
                    break;
                }
            }
        }
        return args;
    }

    private static SMBProviderArguments parseFragmentRefSMB(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        SMBProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseSMB(argsSetter, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "SMBPreProcessing":
                    parseFragmentRefPreProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "SMBPostProcessing":
                    parseFragmentRefPostProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "ProcessingCommandDelimiter":
                    argsSetter.setStringArgumentValue(sourceTargetArgs.getCommands().getCommandDelimiter(), n);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsSetter, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static WebDAVProviderArguments parseFragmentRefWebDAV(YADEXMLArgumentsSetter argsSetter, Node ref, boolean isSource,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        WebDAVProviderArguments args = YADEXMLFragmentsProtocolFragmentHelper.parseWebDAV(argsSetter, ref, isSource);
        NodeList nl = ref.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "WebDAVPostProcessing":
                    parseFragmentRefPostProcessing(argsSetter, n, sourceTargetArgs);
                    break;
                case "Rename":
                    parseFragmentRefRename(argsSetter, n, sourceTargetArgs);
                    break;
                }
            }
        }
        return args;
    }

    private static void parseSourceOptions(YADEXMLArgumentsSetter argsSetter, Node sourceFileOptions) throws Exception {
        NodeList nl = sourceFileOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "Selection":
                    parseSourceOptionSelection(argsSetter, n);
                    break;
                case "CheckSteadyState":
                    parseSourceOptionCheckSteadyState(argsSetter, n);
                    break;
                case "Directives":
                    parseSourceOptionDirectives(argsSetter, n);
                    break;
                case "Polling":
                    parseSourceOptionPolling(argsSetter, n);
                    break;
                case "ResultSet":
                    parseSourceOptionResultSet(argsSetter, n);
                    break;
                case "MaxFiles":
                    argsSetter.setIntegerArgumentValue(argsSetter.getSourceArgs().getMaxFiles(), n);
                    break;
                case "CheckIntegrityHash":
                    parseSourceOptionCheckIntegrityHash(argsSetter, n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelection(YADEXMLArgumentsSetter argsSetter, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FilePathSelection":
                    parseSourceOptionSelectionFilePath(argsSetter, n);
                    break;
                case "FileSpecSelection":
                    parseSourceOptionSelectionFileSpec(argsSetter, n);
                    break;
                case "FileListSelection":
                    parseSourceOptionSelectionFileList(argsSetter, n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFilePath(YADEXMLArgumentsSetter argsSetter, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FilePath":
                    argsSetter.getSourceArgs().setFilePath(argsSetter.getValue(n));
                    break;
                case "Directory":
                    argsSetter.setStringArgumentValue(argsSetter.getSourceArgs().getDirectory(), n);
                    break;
                case "Recursive":
                    argsSetter.setBooleanArgumentValue(argsSetter.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFileSpec(YADEXMLArgumentsSetter argsSetter, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FileSpec":
                    argsSetter.setStringArgumentValue(argsSetter.getSourceArgs().getFileSpec(), n);
                    break;
                case "Directory":
                    argsSetter.setStringArgumentValue(argsSetter.getSourceArgs().getDirectory(), n);
                    break;
                case "ExcludedDirectories":
                    argsSetter.setStringArgumentValue(argsSetter.getSourceArgs().getExcludedDirectories(), n);
                    break;
                case "Recursive":
                    argsSetter.setBooleanArgumentValue(argsSetter.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionSelectionFileList(YADEXMLArgumentsSetter argsSetter, Node selection) throws Exception {
        NodeList nl = selection.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "FileList":
                    argsSetter.setPathArgumentValue(argsSetter.getSourceArgs().getFileList(), n);
                    break;
                case "Directory":
                    argsSetter.setStringArgumentValue(argsSetter.getSourceArgs().getDirectory(), n);
                    break;
                case "Recursive":
                    argsSetter.setBooleanArgumentValue(argsSetter.getSourceArgs().getRecursive(), n);
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionCheckSteadyState(YADEXMLArgumentsSetter argsSetter, Node steadyState) throws Exception {
        NodeList nl = steadyState.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CheckSteadyStateInterval":
                    argsSetter.setStringArgumentValue(argsSetter.getSourceArgs().getCheckSteadyStateInterval(), n);
                    break;
                case "CheckSteadyStateCount":
                    argsSetter.setIntegerArgumentValue(argsSetter.getSourceArgs().getCheckSteadyCount(), n);
                    break;
                case "CheckSteadyStateErrorState":// for JS1 job chain
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionDirectives(YADEXMLArgumentsSetter argsSetter, Node directives) throws Exception {
        NodeList nl = directives.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "DisableErrorOnNoFilesFound":
                    argsSetter.setOppositeBooleanArgumentValue(argsSetter.getSourceArgs().getForceFiles(), n);
                    break;
                case "TransferZeroByteFiles":
                    argsSetter.getSourceArgs().setZeroByteTransfer(argsSetter.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseSourceOptionPolling(YADEXMLArgumentsSetter argsSetter, Node polling) throws Exception {
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
                        argsSetter.setStringArgumentValue(pollingArgs.getPollInterval(), n);
                        break;
                    case "PollTimeout":
                        argsSetter.setIntegerArgumentValue(pollingArgs.getPollTimeout(), n);
                        break;
                    case "MinFiles":
                        argsSetter.setIntegerArgumentValue(pollingArgs.getPollMinFiles(), n);
                        break;
                    case "WaitForSourceFolder":
                        argsSetter.setBooleanArgumentValue(pollingArgs.getWaitingForLateComers(), n);
                        break;
                    case "PollErrorState":
                        // YADE 1 - YADE Job API
                        break;
                    case "PollingServer":
                        argsSetter.setBooleanArgumentValue(pollingArgs.getPollingServer(), n);
                        break;
                    case "PollingServerDuration":
                        argsSetter.setStringArgumentValue(pollingArgs.getPollingServerDuration(), n);
                        break;
                    case "PollForever":
                        argsSetter.setBooleanArgumentValue(pollingArgs.getPollingServerPollForever(), n);
                        break;
                    }
                }
            }
            argsSetter.getSourceArgs().setPolling(pollingArgs);
        }
    }

    private static void parseSourceOptionResultSet(YADEXMLArgumentsSetter argsSetter, Node resultSet) throws Exception {
        NodeList nl = resultSet.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "ResultSetFile":
                        argsSetter.setPathArgumentValue(argsSetter.getClientArgs().getResultSetFileName(), n);
                        break;
                    case "CheckResultSetCount":
                        parseSourceOptionResultSetCheckCount(argsSetter, n);
                        break;
                    case "EmptyResultSetState":
                        // YADE 1 - YADE Job API
                        break;
                    }
                }
            }
        }
    }

    private static void parseSourceOptionResultSetCheckCount(YADEXMLArgumentsSetter argsSetter, Node resultSetCheckCount) throws Exception {
        NodeList nl = resultSetCheckCount.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "ExpectedResultSetCount":
                        argsSetter.setIntegerArgumentValue(argsSetter.getClientArgs().getExpectedSizeOfResultSet(), n);
                        break;
                    case "RaiseErrorIfResultSetIs":
                        SOSComparisonOperator comparisonOperator = SOSComparisonOperator.fromString(argsSetter.getValue(n));
                        if (comparisonOperator != null) {
                            argsSetter.getClientArgs().getRaiseErrorIfResultSetIs().setValue(comparisonOperator);
                        }
                        break;
                    }
                }
            }
        }
    }

    private static void parseSourceOptionCheckIntegrityHash(YADEXMLArgumentsSetter argsSetter, Node checkIntegrityHash) throws Exception {
        argsSetter.getSourceArgs().getCheckIntegrityHash().setValue(Boolean.valueOf(true));

        NodeList nl = checkIntegrityHash.getChildNodes();
        int len = nl.getLength();
        if (len > 0) {
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    switch (n.getNodeName()) {
                    case "HashAlgorithm":
                        argsSetter.setStringArgumentValue(argsSetter.getSourceArgs().getIntegrityHashAlgorithm(), n);
                        break;
                    }
                }
            }
        }
    }

    private static void parseTargetOptions(YADEXMLArgumentsSetter argsSetter, Node targetFileOptions) throws Exception {
        NodeList nl = targetFileOptions.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AppendFiles":
                    argsSetter.setBooleanArgumentValue(argsSetter.getTargetArgs().getAppendFiles(), n);
                    break;
                case "Atomicity":
                    parseTargetOptionAtomicity(argsSetter, n);
                    break;
                case "CheckSize":
                    argsSetter.setBooleanArgumentValue(argsSetter.getTargetArgs().getCheckSize(), n);
                    break;
                case "CumulateFiles":
                    parseTargetOptionCumulateFiles(argsSetter, n);
                    break;
                case "CompressFiles":
                    parseTargetOptionCompressFiles(argsSetter, n);
                    break;
                case "CreateIntegrityHashFile":
                    parseTargetOptionCreateIntegrityHashFile(argsSetter, n);
                    break;
                case "KeepModificationDate":
                    argsSetter.setBooleanArgumentValue(argsSetter.getTargetArgs().getKeepModificationDate(), n);
                    break;
                case "DisableMakeDirectories":
                    argsSetter.setOppositeBooleanArgumentValue(argsSetter.getTargetArgs().getCreateDirectories(), n);
                    break;
                case "DisableOverwriteFiles":
                    argsSetter.setOppositeBooleanArgumentValue(argsSetter.getTargetArgs().getOverwriteFiles(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionAtomicity(YADEXMLArgumentsSetter argsSetter, Node atomicity) throws Exception {
        NodeList nl = atomicity.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "AtomicPrefix":
                    argsSetter.setStringArgumentValue(argsSetter.getTargetArgs().getAtomicPrefix(), n);
                    break;
                case "AtomicSuffix":
                    argsSetter.setStringArgumentValue(argsSetter.getTargetArgs().getAtomicSuffix(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCumulateFiles(YADEXMLArgumentsSetter argsSetter, Node cumulateFiles) throws Exception {
        NodeList nl = cumulateFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CumulativeFileSeparator":
                    argsSetter.setStringArgumentValue(argsSetter.getTargetArgs().getCumulativeFileSeparator(), n);
                    break;
                case "CumulativeFilename":
                    argsSetter.setStringArgumentValue(argsSetter.getTargetArgs().getCumulativeFileName(), n);
                    break;
                case "CumulativeFileDelete":
                    argsSetter.setBooleanArgumentValue(argsSetter.getTargetArgs().getCumulativeFileDelete(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCompressFiles(YADEXMLArgumentsSetter argsSetter, Node compressFiles) throws Exception {
        NodeList nl = compressFiles.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CompressedFileExtension":
                    argsSetter.setStringArgumentValue(argsSetter.getTargetArgs().getCompressedFileExtension(), n);
                    break;
                }
            }
        }
    }

    private static void parseTargetOptionCreateIntegrityHashFile(YADEXMLArgumentsSetter argsSetter, Node createIntegrityHashFile) throws Exception {
        argsSetter.getTargetArgs().getCreateIntegrityHashFile().setValue(Boolean.valueOf(true));

        NodeList nl = createIntegrityHashFile.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "HashAlgorithm":
                    argsSetter.setStringArgumentValue(argsSetter.getTargetArgs().getIntegrityHashAlgorithm(), n);
                    break;
                }
            }
        }
    }

    private static void parseFragmentRefRename(YADEXMLArgumentsSetter argsSetter, Node rename, YADESourceTargetArguments sourceTargetArgs)
            throws Exception {
        NodeList nl = rename.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "ReplaceWhat":
                    argsSetter.setStringArgumentValue(sourceTargetArgs.getReplacing(), n);
                    break;
                case "ReplaceWith":
                    argsSetter.setStringArgumentValue(sourceTargetArgs.getReplacement(), n);
                    break;
                }
            }
        }
    }

    private static void parseFragmentRefPreProcessing(YADEXMLArgumentsSetter argsSetter, Node preProcessing,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        NodeList nl = preProcessing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CommandBeforeFile":
                    sourceTargetArgs.getCommands().setCommandsBeforeFile(argsSetter.getValue(n));
                    break;
                case "CommandBeforeOperation":
                    sourceTargetArgs.getCommands().setCommandsBeforeOperation(argsSetter.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseFragmentRefPostProcessing(YADEXMLArgumentsSetter argsSetter, Node postProcessing,
            YADESourceTargetArguments sourceTargetArgs) throws Exception {
        NodeList nl = postProcessing.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "CommandAfterFile":
                    sourceTargetArgs.getCommands().setCommandsAfterFile(argsSetter.getValue(n));
                    break;
                case "CommandAfterOperationOnSuccess":
                    sourceTargetArgs.getCommands().setCommandsAfterOperationOnSuccess(argsSetter.getValue(n));
                    break;
                case "CommandAfterOperationOnError":
                    sourceTargetArgs.getCommands().setCommandsAfterOperationOnError(argsSetter.getValue(n));
                    break;
                case "CommandAfterOperationFinal":
                    sourceTargetArgs.getCommands().setCommandsAfterOperationFinal(argsSetter.getValue(n));
                    break;
                case "CommandBeforeRename":
                    sourceTargetArgs.getCommands().setCommandsBeforeRename(argsSetter.getValue(n));
                    break;
                }
            }
        }
    }

    private static void parseTransferOptionsRetryOnConnectionError(YADEXMLArgumentsSetter argsSetter, Node retryOnConnectionError) throws Exception {
        NodeList nl = retryOnConnectionError.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                switch (n.getNodeName()) {
                case "RetryCountMax":
                    argsSetter.setIntegerArgumentValue(argsSetter.getSourceArgs().getConnectionErrorRetryCountMax(), n);
                    argsSetter.getTargetArgs().getConnectionErrorRetryCountMax().setValue(argsSetter.getSourceArgs().getConnectionErrorRetryCountMax()
                            .getValue());
                    break;
                case "RetryInterval":
                    argsSetter.setStringArgumentValue(argsSetter.getSourceArgs().getConnectionErrorRetryInterval(), n);
                    argsSetter.getTargetArgs().getConnectionErrorRetryInterval().setValue(argsSetter.getSourceArgs().getConnectionErrorRetryInterval()
                            .getValue());
                    break;
                }
            }
        }
    }

}
