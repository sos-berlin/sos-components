package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.yade.engine.addons.YADEEngineJumpHostAddon.JumpHostConfig;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADEClientArguments;
import com.sos.yade.engine.commons.arguments.YADEJumpHostArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;

/** TODO Not supported for SFTPFragment: <ZlibCompression> <ZlibCompressionLevel>1</ZlibCompressionLevel> </ZlibCompression> */
public class YADEXMLJumpHostSettingsWriter {

    private static final String SFTPFRAGMENT_NAME = "sftp";

    // -------- SOURCE_TO_JUMP_HOST XML settings -------------------
    /** COPY/MOVE operations */
    public static String sourceToJumpHostCOPY(AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments((SSHProviderArguments) sourceArgs.getProvider());
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "Copy", true);
        return generateConfiguration(fragments, profile).toString();
    }

    /** GETLIST operation */
    public static String sourceToJumpHostGETLIST(AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments((SSHProviderArguments) sourceArgs.getProvider());
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "GetList", false);
        return generateConfiguration(fragments, profile).toString();
    }

    /** REMOVE operation */
    public static String sourceToJumpHostREMOVE(AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments((SSHProviderArguments) sourceArgs.getProvider());
        StringBuilder profile = generateProfileSourceToJumpHost(argsLoader.getArgs(), argsLoader.getClientArgs(), sourceArgs, argsLoader
                .getTargetArgs(), config, "Remove", false);
        return generateConfiguration(fragments, profile).toString();
    }

    /** additional configuration for a MOVE operation - removing the source files after successful transfer */
    public static String sourceToJumpHostMOVERemove(AYADEArgumentsLoader argsLoader, JumpHostConfig config, String profileId) {
        YADESourceArguments sourceArgs = argsLoader.getSourceArgs();

        StringBuilder fragments = generateFragments((SSHProviderArguments) sourceArgs.getProvider());
        StringBuilder profile = generateProfileSourceToJumpHostMOVERemove(sourceArgs, config, profileId);
        return generateConfiguration(fragments, profile).toString();
    }

    // -------- JUMP_HOST_TO_TARGET -------------------
    /** COPY/MOVE operations<br/>
     * 
     * @apiNote GETLIST and REMOVE operations are ignored because they are performed for the Source(Any Provider) and not require a Jump Host */
    public static String jumpHostToTargetCOPY(AYADEArgumentsLoader argsLoader, JumpHostConfig config) {
        YADETargetArguments targetArgs = argsLoader.getTargetArgs();

        StringBuilder fragments = generateFragments((SSHProviderArguments) targetArgs.getProvider());
        StringBuilder profile = generateProfileJumpHostToTargetCOPY(argsLoader.getArgs(), targetArgs, config);
        return generateConfiguration(fragments, profile).toString();
    }

    // ------------- Help-Methods -----
    private static StringBuilder generateConfiguration(StringBuilder fragments, StringBuilder profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Configurations>");
        sb.append("<Fragments>");
        sb.append(fragments);
        sb.append("</Fragments>");
        sb.append("<Profiles>");
        sb.append(profile);
        sb.append("</Profiles>");
        sb.append("</Configurations>");
        return sb;
    }

    private static StringBuilder generateFragments(SSHProviderArguments providerArgs) {
        StringBuilder sb = new StringBuilder();
        /** SFTP Protocol Fragment ----------------------------- */
        sb.append("<ProtocolFragments>");
        sb.append("<SFTPFragment name=").append(attrValue(SFTPFRAGMENT_NAME)).append(">");
        sb.append(generateFragmentsSFTPFragmentBasicConnection(providerArgs.getHost(), providerArgs.getPort()));
        // SSHAuthentication
        sb.append("<SSHAuthentication>");
        sb.append("<Account>").append(cdata(providerArgs.getUser().getValue())).append("</Account>");
        if (!providerArgs.getPreferredAuthentications().isEmpty()) {
            sb.append("<PreferredAuthentications>");
            sb.append(cdata(providerArgs.getPreferredAuthentications().getValue().toString()));
            sb.append("</PreferredAuthentications>");
        }
        if (!providerArgs.getRequiredAuthentications().isEmpty()) {
            sb.append("<RequiredAuthentications>");
            sb.append(cdata(providerArgs.getRequiredAuthentications().getValue().toString()));
            sb.append("</RequiredAuthentications>");
        }
        if (!providerArgs.getPassword().isEmpty()) {
            sb.append("<AuthenticationMethodPassword>");
            sb.append("<Password>").append(cdata(providerArgs.getPassword().getValue())).append("</Password>");
            sb.append("</AuthenticationMethodPassword>");
        }
        if (!providerArgs.getAuthFile().isEmpty()) {
            sb.append("<AuthenticationMethodPublickey>");
            sb.append("<AuthenticationFile>").append(cdata(providerArgs.getAuthFile().getValue())).append("</AuthenticationFile>");
            if (!providerArgs.getPassphrase().isEmpty()) {
                sb.append("<Passphrase>").append(cdata(providerArgs.getPassphrase().getValue())).append("</Passphrase>");
            }
            sb.append("</AuthenticationMethodPublickey>");
        }
        sb.append("</SSHAuthentication>");
        // CredentialStore
        boolean generateCS = false;
        if (providerArgs.getCredentialStore() != null && providerArgs.getCredentialStore().getFile().isDirty()) {
            sb.append("<CredentialStoreFragmentRef ref=\"cs\"/>");
            generateCS = true;
        }

        // ProxyForSFTP
        if (providerArgs.getProxy() != null) {
            sb.append("<ProxyForSFTP>");
            if (providerArgs.getProxy().isHTTP()) {
                sb.append("<HTTPProxy>");
                sb.append(generateFragmentsSFTPFragmentBasicConnection(providerArgs.getProxy().getHost(), providerArgs.getProxy().getPort()));
                sb.append(generateFragmentsSFTPFragmentBasicAuthentication(providerArgs.getProxy().getUser(), providerArgs.getProxy().getPassword()));
                sb.append("</HTTPProxy>");
            } else {
                sb.append("<SOCKS5Proxy>");
                sb.append(generateFragmentsSFTPFragmentBasicConnection(providerArgs.getProxy().getHost(), providerArgs.getProxy().getPort()));
                sb.append(generateFragmentsSFTPFragmentBasicAuthentication(providerArgs.getProxy().getUser(), providerArgs.getProxy().getPassword()));
                sb.append("</SOCKS5Proxy>");
            }
            sb.append("</ProxyForSFTP>");
        }
        // Other
        if (providerArgs.getStrictHostkeyChecking().isDirty()) {
            sb.append("<StrictHostkeyChecking>").append(providerArgs.getStrictHostkeyChecking().getValue()).append("</StrictHostkeyChecking>");
        }
        if (providerArgs.getConfigurationFiles().isDirty()) {
            sb.append("<ConfigurationFiles>");
            for (Path configurationFile : providerArgs.getConfigurationFiles().getValue()) {
                sb.append("<ConfigurationFile>").append(cdata(configurationFile.toString())).append("</ConfigurationFile>");
            }
            sb.append("</ConfigurationFiles>");
        }
        if (providerArgs.getServerAliveInterval().isDirty()) {
            sb.append("<ServerAliveInterval>").append(cdata(providerArgs.getServerAliveInterval().getValue())).append("</ServerAliveInterval>");
        }
        if (providerArgs.getServerAliveCountMax().isDirty()) {
            sb.append("<ServerAliveCountMax>").append(providerArgs.getServerAliveCountMax().getValue()).append("</ServerAliveCountMax>");
        }
        if (providerArgs.getConnectTimeout().isDirty()) {
            sb.append("<ConnectTimeout>").append(cdata(providerArgs.getConnectTimeout().getValue())).append("</ConnectTimeout>");
        }
        if (providerArgs.getSocketTimeout().isDirty()) {
            sb.append("<ChannelConnectTimeout>").append(cdata(providerArgs.getSocketTimeout().getValue())).append("</ChannelConnectTimeout>");
        }
        sb.append("</SFTPFragment></ProtocolFragments>");
        if (generateCS) {
            /** CredentialStore Fragment */
            sb.append("<CredentialStoreFragments>");
            sb.append("<CredentialStoreFragment name=\"cs\">");
            sb.append("<CSFile>").append(cdata(providerArgs.getCredentialStore().getFile().getValue())).append("</CSFile>");
            sb.append("<CSAuthentication>");
            if (providerArgs.getCredentialStore().getKeyFile().isDirty()) {
                sb.append("<KeyFileAuthentication>");
                sb.append("<CSKeyFile>").append(cdata(providerArgs.getCredentialStore().getKeyFile().getValue())).append("</CSKeyFile>");
                if (providerArgs.getCredentialStore().getPassword().isDirty()) {
                    sb.append("<CSPassword>").append(cdata(providerArgs.getCredentialStore().getPassword().getValue())).append("</CSPassword>");
                }
                sb.append("</KeyFileAuthentication>");
            } else if (providerArgs.getCredentialStore().getPassword().isDirty()) {
                sb.append("<PasswordAuthentication>");
                sb.append("<CSPassword>").append(cdata(providerArgs.getCredentialStore().getPassword().getValue())).append("</CSPassword>");
                sb.append("</PasswordAuthentication>");
            }
            sb.append("</CSAuthentication>");
            sb.append("</CredentialStoreFragment>");
            sb.append("</CredentialStoreFragments>");
        }
        return sb;
    }

    private static StringBuilder generateFragmentsSFTPFragmentBasicConnection(SOSArgument<String> host, SOSArgument<Integer> port) {
        StringBuilder sb = new StringBuilder();
        sb.append("<BasicConnection>");
        sb.append("<Hostname>").append(cdata(host.getValue())).append("</Hostname>");
        if (port.isDirty()) {
            sb.append("<Port>").append(cdata(String.valueOf(port.getValue()))).append("</Port>");
        }
        sb.append("</BasicConnection>");
        return sb;
    }

    private static StringBuilder generateFragmentsSFTPFragmentBasicAuthentication(SOSArgument<String> user, SOSArgument<String> password) {
        StringBuilder sb = new StringBuilder();
        if (user.isEmpty()) {
            return sb;
        }

        sb.append("<BasicAuthentication>");
        sb.append("<Account>").append(cdata(user.getValue())).append("</Account>");
        if (password.isDirty()) {
            sb.append("<Password>").append(cdata(password.getValue())).append("</Password>");
        }
        sb.append("</BasicAuthentication>");
        return sb;
    }

    private static StringBuilder generateProfileSourceToJumpHost(YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourceArgs,
            YADETargetArguments targetArgs, JumpHostConfig config, String operation, boolean useTarget) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(config.getProfileId())).append(">");
        sb.append("<Operation>");
        sb.append("<").append(operation).append(">");

        // Source (SFTPFragment) -----------------------
        String sourcePrefix = operation + "Source";
        sb.append("<").append(sourcePrefix).append(">");
        sb.append("<").append(sourcePrefix).append("FragmentRef>");
        sb.append(generateProfileTargetSFTPFragmentRef(sourceArgs));
        sb.append("</").append(sourcePrefix).append("FragmentRef>");
        sb.append("<SourceFileOptions>");
        // Source - Selection
        sb.append("<Selection>");
        if (config.getSourceToJumpHost().getFileList() != null) {
            sb.append("<FileListSelection>");
            sb.append("<FileList>").append(cdata(config.getSourceToJumpHost().getFileList().getJumpHostFile())).append("</FileList>");
            if (!sourceArgs.getDirectory().isEmpty()) {
                sb.append("<Directory>").append(cdata(sourceArgs.getDirectory().getValue())).append("</Directory>");
            }
            if (sourceArgs.getRecursive().isTrue()) {
                sb.append("<Recursive>true</Recursive>");
            }
            sb.append("</FileListSelection>");
        } else if (sourceArgs.isFilePathEnabled()) {
            sb.append("<FilePathSelection>");
            sb.append("<FilePath>").append(cdata(sourceArgs.getFilePathAsString())).append("</FilePath>");
            if (!sourceArgs.getDirectory().isEmpty()) {
                sb.append("<Directory>").append(cdata(sourceArgs.getDirectory().getValue())).append("</Directory>");
            }
            if (sourceArgs.getRecursive().isTrue()) {
                sb.append("<Recursive>true</Recursive>");
            }
            sb.append("</FilePathSelection>");
        } else {
            sb.append("<FileSpecSelection>");
            sb.append("<FileSpec>").append(cdata(sourceArgs.getFileSpec().getValue())).append("</FileSpec>");
            if (!sourceArgs.getDirectory().isEmpty()) {
                sb.append("<Directory>").append(cdata(sourceArgs.getDirectory().getValue())).append("</Directory>");
            }
            if (!sourceArgs.getExcludedDirectories().isEmpty()) {
                sb.append("<ExcludedDirectories>").append(cdata(sourceArgs.getExcludedDirectories().getValue())).append("</ExcludedDirectories>");
            }
            if (sourceArgs.getRecursive().isTrue()) {
                sb.append("<Recursive>true</Recursive>");
            }
            sb.append("</FileSpecSelection>");
        }
        sb.append("</Selection>");
        // Source - CheckSteadyState
        if (sourceArgs.isCheckSteadyStateEnabled()) {
            sb.append("<CheckSteadyState>");
            sb.append("<CheckSteadyStateInterval>");
            sb.append(cdata(sourceArgs.getCheckSteadyStateInterval().getValue()));
            sb.append("</CheckSteadyStateInterval>");
            if (!sourceArgs.getCheckSteadyCount().isEmpty()) {
                sb.append("<CheckSteadyStateCount>");
                sb.append(sourceArgs.getCheckSteadyCount().getValue());
                sb.append("</CheckSteadyStateCount>");
            }
            sb.append("</CheckSteadyState>");
        }
        // Source - Directives
        if (sourceArgs.isDirectivesEnabled()) {
            sb.append("<Directives>");
            if (sourceArgs.getErrorOnNoFilesFound().isDirty()) {
                sb.append("<DisableErrorOnNoFilesFound>");
                sb.append(!sourceArgs.getErrorOnNoFilesFound().getValue());
                sb.append("</DisableErrorOnNoFilesFound>");
            }
            if (sourceArgs.getZeroByteTransfer().isDirty()) {
                sb.append("<TransferZeroByteFiles>");
                sb.append(cdata(sourceArgs.getZeroByteTransfer().getValue().name()));
                sb.append("</TransferZeroByteFiles>");
            }
            sb.append("</Directives>");
        }
        // Source - Polling
        if (sourceArgs.isPollingEnabled()) {
            sb.append("<Polling>");
            if (sourceArgs.getPolling().getPollInterval().isDirty()) {
                sb.append("<PollInterval>");
                sb.append(cdata(sourceArgs.getPolling().getPollInterval().getValue()));
                sb.append("</PollInterval>");
            }
            if (sourceArgs.getPolling().getPollTimeout().isDirty()) {
                sb.append("<PollTimeout>");
                sb.append(sourceArgs.getPolling().getPollTimeout().getValue());
                sb.append("</PollTimeout>");
            }
            if (sourceArgs.getPolling().getPollMinFiles().isDirty()) {
                sb.append("<MinFiles>");
                sb.append(sourceArgs.getPolling().getPollMinFiles().getValue());
                sb.append("</MinFiles>");
            }
            if (sourceArgs.getPolling().getWaitingForLateComers().isDirty()) {
                sb.append("<WaitForSourceFolder>");
                sb.append(sourceArgs.getPolling().getWaitingForLateComers().getValue());
                sb.append("</WaitForSourceFolder>");
            }
            if (sourceArgs.getPolling().getPollingServer().isDirty()) {
                sb.append("<PollingServer>");
                sb.append(sourceArgs.getPolling().getPollingServer().getValue());
                sb.append("</PollingServer>");
            }
            if (sourceArgs.getPolling().getPollingServerDuration().isDirty()) {
                sb.append("<PollingServerDuration>");
                sb.append(cdata(sourceArgs.getPolling().getPollingServerDuration().getValue()));
                sb.append("</PollingServerDuration>");
            }
            if (sourceArgs.getPolling().getPollingServerPollForever().isDirty()) {
                sb.append("<PollForever>");
                sb.append(sourceArgs.getPolling().getPollingServerPollForever().getValue());
                sb.append("</PollForever>");
            }
            sb.append("</Polling>");
        }
        if (config.getSourceToJumpHost().getResultSetFile() != null) {
            sb.append("<ResultSet>");
            sb.append("<ResultSetFile>").append(cdata(config.getSourceToJumpHost().getResultSetFile().getJumpHostFile())).append("</ResultSetFile>");
            if (clientArgs.isCheckResultSetCountEnabled()) {
                sb.append("<CheckResultSetCount>");
                if (clientArgs.getExpectedResultSetCount().isDirty()) {
                    sb.append("<ExpectedResultSetCount>");
                    sb.append(clientArgs.getExpectedResultSetCount().getValue());
                    sb.append("</ExpectedResultSetCount>");
                }
                if (clientArgs.getRaiseErrorIfResultSetIs().isDirty()) {
                    sb.append("<RaiseErrorIfResultSetIs>");
                    sb.append(cdata(clientArgs.getRaiseErrorIfResultSetIs().getValue().getFirstAlias()));
                    sb.append("</RaiseErrorIfResultSetIs>");
                }
                sb.append("</CheckResultSetCount>");
            }
            sb.append("</ResultSet>");
        }
        if (sourceArgs.getMaxFiles().isDirty()) {
            sb.append("<MaxFiles>").append(sourceArgs.getMaxFiles().getValue()).append("</MaxFiles>");
        }
        if (sourceArgs.getCheckIntegrityHash().isTrue()) {
            sb.append("<CheckIntegrityHash>");
            sb.append("<HashAlgorithm>").append(cdata(sourceArgs.getIntegrityHashAlgorithm().getValue())).append("</HashAlgorithm>");
            sb.append("</CheckIntegrityHash>");
        }

        sb.append("</SourceFileOptions>");
        sb.append("</").append(sourcePrefix).append(">");
        if (useTarget) {
            // Target (Jump) ----------------------------------
            sb.append(generateJumpHostLocalCopyTarget(targetArgs, config));
            // TransferOptions
            sb.append(generateProfileTransferOptions(args, sourceArgs, config));
        }
        sb.append("</").append(operation).append(">");
        sb.append("</Operation>");
        sb.append("</Profile>");
        return sb;
    }

    private static StringBuilder generateProfileSourceToJumpHostMOVERemove(YADESourceArguments sourceArgs, JumpHostConfig config, String profileId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(profileId)).append(">");
        sb.append("<Operation>");
        sb.append("<Remove>");

        // Source (SFTPFragment) -----------------------
        sb.append("<RemoveSource>");
        sb.append("<RemoveSourceFragmentRef>");
        sb.append("<SFTPFragmentRef ref=").append(attrValue(SFTPFRAGMENT_NAME)).append(" />");
        sb.append("</RemoveSourceFragmentRef>");
        sb.append("<SourceFileOptions>");
        // Source - Selection
        sb.append("<Selection>");
        sb.append("<FileListSelection>");
        sb.append("<FileList>").append(cdata(config.getSourceToJumpHost().getFileList().getJumpHostFile())).append("</FileList>");
        sb.append("<Directory>").append(cdata(config.getDataDirectory())).append("</Directory>");
        sb.append("</FileListSelection>");
        sb.append("</Selection>");
        sb.append("</SourceFileOptions>");
        sb.append("</RemoveSource>");

        sb.append("</Remove>");
        sb.append("</Operation>");
        sb.append("</Profile>");
        return sb;
    }

    private static StringBuilder generateProfileJumpHostToTargetCOPY(YADEArguments args, YADETargetArguments targetArgs, JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(config.getProfileId())).append(">");
        sb.append("<Operation>");
        sb.append("<Copy>");

        // Source (Jump)
        sb.append(generateJumpHostLocalCopySource(config));
        // Target
        sb.append("<CopyTarget>");
        sb.append("<CopyTargetFragmentRef>").append(generateProfileTargetSFTPFragmentRef(targetArgs)).append("</CopyTargetFragmentRef>");
        if (targetArgs.getDirectory().isDirty()) {
            sb.append("<Directory>").append(cdata(targetArgs.getDirectory().getValue())).append("</Directory>");
        }
        sb.append(generateProfileTargetFileOptions(targetArgs, config));
        sb.append("</CopyTarget>");
        // TransferOptions
        sb.append(generateProfileTransferOptions(args, targetArgs, config));

        sb.append("</Copy>");
        sb.append("</Operation>");
        sb.append("</Profile>");
        return sb;
    }

    private static StringBuilder generateJumpHostLocalCopySource(JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<CopySource>");
        sb.append("<CopySourceFragmentRef>");
        sb.append("<LocalSource ").append(generateJumpAttribute()).append("/>");
        sb.append("</CopySourceFragmentRef>");
        sb.append("<SourceFileOptions>");
        sb.append("<Selection>");
        sb.append("<FileSpecSelection>");
        sb.append("<FileSpec><![CDATA[.*]]></FileSpec>");
        sb.append("<Directory>").append(cdata(config.getDataDirectory())).append("</Directory>");
        sb.append("<Recursive>true</Recursive>");
        sb.append("</FileSpecSelection>");
        sb.append("</Selection>");
        sb.append("</SourceFileOptions>");
        sb.append("</CopySource>");
        return sb;
    }

    private static StringBuilder generateJumpHostLocalCopyTarget(YADETargetArguments targetArgs, JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<CopyTarget>");
        sb.append("<CopyTargetFragmentRef>");
        sb.append("<LocalTarget ").append(generateJumpAttribute()).append("/>");
        sb.append("</CopyTargetFragmentRef>");
        sb.append("<Directory>").append(cdata(config.getDataDirectory())).append("</Directory>");
        sb.append("<KeepModificationDate>").append(targetArgs.getKeepModificationDate().getValue()).append("</KeepModificationDate>");
        sb.append("</CopyTarget>");
        return sb;
    }

    private static String generateJumpAttribute() {
        return YADEXMLArgumentsLoader.INTERNAL_ATTRIBUTE_LABEL + "=" + attrValue(YADEJumpHostArguments.LABEL);
    }

    private static StringBuilder generateProfileTargetSFTPFragmentRef(YADESourceTargetArguments args) {
        StringBuilder sb = new StringBuilder();
        sb.append("<SFTPFragmentRef ref=").append(attrValue(SFTPFRAGMENT_NAME)).append(">");
        // Pre-Processing
        if (args.getCommands().isPreProcessingEnabled()) {
            sb.append("<SFTPPreProcessing>");
            if (args.getCommands().getCommandsBeforeFile().isDirty()) {
                sb.append("<CommandBeforeFile enable_for_skipped_transfer=");
                sb.append(attrValue(args.getCommands().getCommandsBeforeFileEnableForSkipped().getValue() + ""));
                sb.append(">");
                sb.append(cdata(args.getCommands().getCommandsBeforeFileAsString()));
                sb.append("</CommandBeforeFile>");
            }
            if (args.getCommands().getCommandsBeforeOperation().isDirty()) {
                sb.append("<CommandBeforeOperation>");
                sb.append(cdata(args.getCommands().getCommandsBeforeOperationAsString()));
                sb.append("</CommandBeforeOperation>");
            }
            sb.append("</SFTPPreProcessing>");
        }
        // Post-Processing
        if (args.getCommands().isPostProcessingEnabled()) {
            sb.append("<SFTPPostProcessing>");
            if (args.getCommands().getCommandsAfterFile().isDirty()) {
                sb.append("<CommandAfterFile disable_for_skipped_transfer=");
                sb.append(attrValue(args.getCommands().getCommandsAfterFileDisableForSkipped().getValue() + ""));
                sb.append(">");
                sb.append(cdata(args.getCommands().getCommandsAfterFileAsString()));
                sb.append("</CommandAfterFile>");
            }
            if (args.getCommands().getCommandsAfterOperationOnSuccess().isDirty()) {
                sb.append("<CommandAfterOperationOnSuccess>");
                sb.append(cdata(args.getCommands().getCommandsAfterOperationOnSuccessAsString()));
                sb.append("</CommandAfterOperationOnSuccess>");
            }
            if (args.getCommands().getCommandsAfterOperationOnError().isDirty()) {
                sb.append("<CommandAfterOperationOnError>");
                sb.append(cdata(args.getCommands().getCommandsAfterOperationOnErrorAsString()));
                sb.append("</CommandAfterOperationOnError>");
            }
            if (args.getCommands().getCommandsAfterOperationFinal().isDirty()) {
                sb.append("<CommandAfterOperationFinal>");
                sb.append(cdata(args.getCommands().getCommandsAfterOperationFinalAsString()));
                sb.append("</CommandAfterOperationFinal>");
            }
            if (args.getCommands().getCommandsBeforeRename().isDirty()) {
                sb.append("<CommandBeforeRename>");
                sb.append(cdata(args.getCommands().getCommandsBeforeRenameAsString()));
                sb.append("</CommandBeforeRename>");
            }
            sb.append("</SFTPPostProcessing>");
        }
        if (args.getCommands().getCommandDelimiter().isDirty()) {
            sb.append("<ProcessingCommandDelimiter>");
            sb.append(cdata(args.getCommands().getCommandDelimiter().getValue()));
            sb.append("</ProcessingCommandDelimiter>");
        }
        // Rename
        if (args.isReplacementEnabled()) {
            sb.append("<Rename>");
            sb.append("<ReplaceWhat>").append(cdata(args.getReplacing().getValue())).append("</ReplaceWhat>");
            sb.append("<ReplaceWith>").append(cdata(args.getReplacement().getValue())).append("</ReplaceWith>");
            sb.append("</Rename>");
        }
        // Schema: to remove because not used <ZlibCompression><ZlibCompressionLevel>1</ZlibCompressionLevel></ZlibCompression>
        sb.append("</SFTPFragmentRef>");
        return sb;
    }

    private static StringBuilder generateProfileTargetFileOptions(YADETargetArguments args, JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<TargetFileOptions>");
        if (args.getAppendFiles().isDirty()) {
            sb.append("<AppendFiles>").append(args.getAppendFiles().getValue()).append("</AppendFiles>");
        }
        if (config.isAtomicEnabled()) {
            sb.append("<Atomicity>");
            if (config.getAtomicPrefix() != null) {
                sb.append("<AtomicPrefix>").append(cdata(config.getAtomicPrefix())).append("</AtomicPrefix>");
            }
            if (config.getAtomicSuffix() != null) {
                sb.append("<AtomicSuffix>").append(cdata(config.getAtomicSuffix())).append("</AtomicSuffix>");
            }
            sb.append("</Atomicity>");
        }
        if (args.getCheckSize().isDirty()) {
            sb.append("<CheckSize>").append(args.getCheckSize().getValue()).append("</CheckSize>");
        }
        if (args.isCumulateFilesEnabled()) {
            sb.append("<CumulateFiles>");
            sb.append("<CumulativeFileSeparator>").append(cdata(args.getCumulativeFileSeparator().getValue())).append("</CumulativeFileSeparator>");
            sb.append("<CumulativeFilename>").append(cdata(args.getCumulativeFileName().getValue())).append("</CumulativeFilename>");
            sb.append("<CumulativeFileDelete>").append(args.getCumulativeFileDelete().getValue()).append("</CumulativeFileDelete>");
            sb.append("</CumulateFiles>");
        }
        if (args.isCompressFilesEnabled()) {
            sb.append("<CompressFiles>");
            sb.append("<CompressedFileExtension>").append(cdata(args.getCompressedFileExtension().getValue())).append("</CompressedFileExtension>");
            sb.append("</CompressFiles>");
        }
        if (args.getCreateIntegrityHashFile().isTrue()) {
            sb.append("<CreateIntegrityHashFile>");
            sb.append("<HashAlgorithm>").append(cdata(args.getIntegrityHashAlgorithm().getValue())).append("</HashAlgorithm>");
            sb.append("</CreateIntegrityHashFile>");
        }
        
        sb.append("<KeepModificationDate>").append(args.getKeepModificationDate().getValue()).append("</KeepModificationDate>");
        sb.append("<DisableMakeDirectories>").append(getOppositeValue(args.getCreateDirectories())).append("</DisableMakeDirectories>");
        sb.append("<DisableOverwriteFiles>").append(getOppositeValue(args.getOverwriteFiles())).append("</DisableOverwriteFiles>");
        sb.append("</TargetFileOptions>");
        return sb;
    }

    private static StringBuilder generateProfileTransferOptions(YADEArguments args, YADESourceTargetArguments sourceTargetArgs,
            JumpHostConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append("<TransferOptions>");
        sb.append("<Transactional>").append(config.isTransactional()).append("</Transactional>");
        if (args.getBufferSize().isDirty()) {
            sb.append("<BufferSize>").append(args.getBufferSize().getValue()).append("</BufferSize>");
        }
        if (sourceTargetArgs.isRetryOnConnectionErrorEnabled()) {
            sb.append("<RetryOnConnectionError>");
            sb.append("<RetryCountMax>").append(sourceTargetArgs.getConnectionErrorRetryCountMax().getValue()).append("</RetryCountMax>");
            sb.append("<RetryInterval>").append(cdata(sourceTargetArgs.getConnectionErrorRetryInterval().getValue())).append("</RetryInterval>");
            sb.append("</RetryOnConnectionError>");
        }
        sb.append("</TransferOptions>");
        return sb;
    }

    private static String attrValue(String val) {
        return "\"" + val + "\"";
    }

    private static String cdata(String val) {
        return "<![CDATA[" + val + "]]>";
    }

    private static boolean getOppositeValue(SOSArgument<Boolean> arg) {
        return !arg.isTrue();
    }

}
