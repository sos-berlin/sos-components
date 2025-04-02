package com.sos.yade.engine.commons.arguments.loaders.xml;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.vfs.ssh.commons.SSHProviderArguments;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADETargetArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;

/** TODO Not supported for SFTPFragment: <ZlibCompression> <ZlibCompressionLevel>1</ZlibCompressionLevel> </ZlibCompression> */
public class YADEXMLJumpHostSettingsWriter {

    private static final String SFTPFRAGMENT_NAME = "sftp";

    public static String fromJumpHostToTarget(AYADEArgumentsLoader argsLoader, String profileId, String jumpHostDataDirectory,
            boolean transactional) {
        YADETargetArguments targetArgs = argsLoader.getTargetArgs();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<Configurations>");
        /** Fragments (SFTP ProtocolFragment and e.g. CredentialStoreFragment) ----------------------------- */
        sb.append("<Fragments>");
        sb.append(generateFragments((SSHProviderArguments) targetArgs.getProvider()));
        sb.append("</Fragments>");
        /** Profile ----------------------------- */
        sb.append("<Profiles>");
        sb.append(generateFromJumpHostProfile(argsLoader.getArgs(), targetArgs, profileId, jumpHostDataDirectory, transactional));
        sb.append("</Profiles>");
        sb.append("</Configurations>");
        return sb.toString();
    }

    private static String generateFragments(SSHProviderArguments providerArgs) {
        StringBuilder sb = new StringBuilder();
        /** SFTP Protocol Fragment ----------------------------- */
        sb.append("<ProtocolFragments><SFTPFragment name=").append(attrValue(SFTPFRAGMENT_NAME)).append(">");
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
                sb.append("<ConfigurationFile>").append(configurationFile).append("</ConfigurationFile>");
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
        return sb.toString();
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

    private static StringBuilder generateFromJumpHostProfile(YADEArguments args, YADETargetArguments targetArgs, String profileId,
            String jumpDataDirectory, boolean transactional) {
        StringBuilder sb = new StringBuilder();
        sb.append("<Profile profile_id=").append(attrValue(profileId)).append(">");
        sb.append("<Operation>");

        sb.append("<Copy>");
        // Source (Jump)
        sb.append("<CopySource>");
        sb.append("<CopySourceFragmentRef><LocalSource /></CopySourceFragmentRef>");
        sb.append("<SourceFileOptions><Selection><FileSpecSelection>");
        sb.append("<FileSpec><![CDATA[.*]]></FileSpec><Directory>").append(cdata(jumpDataDirectory)).append("</Directory>");
        sb.append("</FileSpecSelection></Selection></SourceFileOptions>");
        sb.append("</CopySource>");
        // Target
        sb.append("<CopyTarget>");
        sb.append("<CopyTargetFragmentRef>").append(generateProfileTargetSFTPFragmentRef(targetArgs)).append("</CopyTargetFragmentRef>");
        if (targetArgs.getDirectory().isDirty()) {
            sb.append("<Directory>").append(cdata(targetArgs.getDirectory().getValue())).append("</Directory>");
        }
        sb.append(generateProfileTargetFileOptions(targetArgs));
        sb.append("</CopyTarget>");
        sb.append(generateProfileTransferOptions(args, targetArgs, transactional));
        sb.append("</Copy>");

        sb.append("</Operation>");
        sb.append("</Profile>");
        return sb;
    }

    private static StringBuilder generateProfileTargetSFTPFragmentRef(YADETargetArguments args) {
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

    private static StringBuilder generateProfileTargetFileOptions(YADETargetArguments args) {
        StringBuilder sb = new StringBuilder();
        sb.append("<TargetFileOptions>");
        if (args.getAppendFiles().isDirty()) {
            sb.append("<AppendFiles>").append(args.getAppendFiles().getValue()).append("</AppendFiles>");
        }
        if (args.isAtomicityEnabled()) {
            sb.append("<Atomicity>");
            if (args.getAtomicPrefix().isDirty()) {
                sb.append("<AtomicPrefix>").append(cdata(args.getAtomicPrefix().getValue())).append("</AtomicPrefix>");
            }
            if (args.getAtomicSuffix().isDirty()) {
                sb.append("<AtomicSuffix>").append(cdata(args.getAtomicSuffix().getValue())).append("</AtomicSuffix>");
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

    private static StringBuilder generateProfileTransferOptions(YADEArguments args, YADETargetArguments targetArgs, boolean transactional) {
        StringBuilder sb = new StringBuilder();
        sb.append("<TransferOptions>");
        sb.append("<Transactional>").append(transactional).append("</Transactional>");
        if (args.getBufferSize().isDirty()) {
            sb.append("<BufferSize>").append(args.getBufferSize().getValue()).append("</BufferSize>");
        }
        if (targetArgs.isRetryOnConnectionErrorEnabled()) {
            sb.append("<RetryOnConnectionError>");
            sb.append("<RetryCountMax>").append(targetArgs.getConnectionErrorRetryCountMax().getValue()).append("</RetryCountMax>");
            sb.append("<RetryInterval>").append(cdata(targetArgs.getConnectionErrorRetryInterval().getValue())).append("</RetryInterval>");
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
