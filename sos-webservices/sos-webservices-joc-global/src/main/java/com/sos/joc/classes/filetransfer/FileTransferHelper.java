package com.sos.joc.classes.filetransfer;

import java.util.Arrays;
import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.filetransfer.profile.Source;
import com.sos.inventory.model.filetransfer.profile.Target;
import com.sos.inventory.model.filetransfer.profile.fragments.Fragment;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class FileTransferHelper {

    private static final String PROPERY_NAME_ALTERNATIVE_AZURE_BLOB_STORAGE = "alternativeAzureBlobStorageFragmentName";
    private static final String PROPERY_NAME_ALTERNATIVE_FTP = "alternativeFtpFragmentName";
    private static final String PROPERY_NAME_ALTERNATIVE_HTTP = "alternativeHttpFragmentName";
    private static final String PROPERY_NAME_ALTERNATIVE_SFTP = "alternativeSftpFragmentName";
    private static final String PROPERY_NAME_ALTERNATIVE_JUMP = "alternativeJumpFragmentName";
    private static final String PROPERY_NAME_ALTERNATIVE_SMB = "alternativeSmbFragmentName";
    private static final String PROPERY_NAME_ALTERNATIVE_WEBDAV = "alternativeWebdavFragmentName";

    public static String getAlternativeProtocolFragmentPropertyName(ConfigurationType fileTransferType) throws SOSHibernateException {
        switch (fileTransferType) {
        case FT_FRAGMENT_SFTP:
            return PROPERY_NAME_ALTERNATIVE_SFTP;
        case FT_FRAGMENT_JUMP:
            return PROPERY_NAME_ALTERNATIVE_JUMP;
        case FT_FRAGMENT_SMB:
            return PROPERY_NAME_ALTERNATIVE_SMB;
        case FT_FRAGMENT_FTP:
            return PROPERY_NAME_ALTERNATIVE_FTP;
        case FT_FRAGMENT_HTTP:
            return PROPERY_NAME_ALTERNATIVE_HTTP;
        case FT_FRAGMENT_WEBDAV:
            return PROPERY_NAME_ALTERNATIVE_WEBDAV;
        case FT_FRAGMENT_AZURE_BLOB_STORAGE:
            return PROPERY_NAME_ALTERNATIVE_AZURE_BLOB_STORAGE;
        default:
            return null;
        }
    }

    // SOURCE - protocol fragments
    public static boolean isSourceFragmentLocal(Source source) {
        return source != null && isLocal(source.getFragment());
    }

    public static boolean isSourceFragmentAzureBlobStorage(Source source) {
        return source != null && isAzureBlobStorage(source.getFragment());
    }

    public static boolean isSourceFragmentFtp(Source source) {
        return source != null && isFtp(source.getFragment());
    }

    public static boolean isSourceFragmentHttp(Source source) {
        return source != null && isHttp(source.getFragment());
    }

    public static boolean isSourceFragmentSftp(Source source) {
        return source != null && isSftp(source.getFragment());
    }

    public static boolean isSourceFragmentSmb(Source source) {
        return source != null && isSmb(source.getFragment());
    }

    public static boolean isSourceFragmentWebdav(Source source) {
        return source != null && isWebdav(source.getFragment());
    }

    // Target - protocol fragments
    public static boolean isTargetFragmentNullOrLocal(Target target) {
        return target == null || isLocal(target.getFragment());
    }

    public static boolean isTargetFragmentAzureBlobStorage(Target target) {
        return target != null && isAzureBlobStorage(target.getFragment());
    }

    public static boolean isTargetFragmentFtp(Target target) {
        return target != null && isFtp(target.getFragment());
    }

    public static boolean isTargetFragmentHttp(Target target) {
        return target != null && isHttp(target.getFragment());
    }

    public static boolean isTargetFragmentSftp(Target target) {
        return target != null && isSftp(target.getFragment());
    }

    public static boolean isTargetFragmentSmb(Target target) {
        return target != null && isSmb(target.getFragment());
    }

    public static boolean isTargetFragmentWebdav(Target target) {
        return target != null && isWebdav(target.getFragment());
    }

    // common methods
    public static boolean isLocal(Fragment f) {
        return f != null && f.getLocal() != null;
    }

    public static boolean isAzureBlobStorage(Fragment f) {
        return f != null && f.getLocal() != null;
    }

    public static boolean isFtp(Fragment f) {
        return f != null && f.getFtp() != null;
    }

    public static boolean isHttp(Fragment f) {
        return f != null && f.getHttp() != null;
    }

    public static boolean isSftp(Fragment f) {
        return f != null && f.getSftp() != null;
    }

    public static boolean isSmb(Fragment f) {
        return f != null && f.getSmb() != null;
    }

    public static boolean isWebdav(Fragment f) {
        return f != null && f.getWebdav() != null;
    }

    /** Returns the protocol fragment types that can reference a jump fragment.<br />
     * The JUMP type itself is excluded.
     *
     * @return list of configuration types that may reference a jump fragment, excluding JUMP itself */
    public static List<ConfigurationType> getOtherFragmentsUsingJump() {
        return Arrays.asList(ConfigurationType.FT_FRAGMENT_AZURE_BLOB_STORAGE, ConfigurationType.FT_FRAGMENT_FTP, ConfigurationType.FT_FRAGMENT_SFTP,
                ConfigurationType.FT_FRAGMENT_HTTP, ConfigurationType.FT_FRAGMENT_WEBDAV);
    }
}
