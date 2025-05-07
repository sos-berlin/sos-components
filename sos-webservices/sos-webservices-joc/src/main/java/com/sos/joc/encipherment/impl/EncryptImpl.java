package com.sos.joc.encipherment.impl;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import com.sos.commons.encryption.EncryptionUtils;
import com.sos.commons.encryption.encrypt.Encrypt;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.encipherment.DBItemEncCertificate;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IEncrypt;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.encipherment.EncryptRequestFilter;
import com.sos.joc.model.encipherment.EncryptResponse;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment")
public class EncryptImpl extends JOCResourceImpl implements IEncrypt {

    private static final String API_CALL = "./encipherment/encrypt";

    @Override
    public JOCDefaultResponse postEncrypt(String xAccessToken, byte[] encryptFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            encryptFilter = initLogging(API_CALL, encryptFilter, xAccessToken);
            JsonValidator.validate(encryptFilter, EncryptRequestFilter.class);
            EncryptRequestFilter filter = Globals.objectMapper.readValue(encryptFilter, EncryptRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getEncipherment().getEncrypt()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(filter.getAuditLog(), CategoryType.INVENTORY);
            
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            
            return JOCDefaultResponse.responseStatus200(encrypt(filter, hibernateSession));
        } catch (JocConcurrentAccessException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), xAccessToken, getJocError(), null);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private EncryptResponse encrypt(EncryptRequestFilter filter, SOSHibernateSession hibernateSession) throws CertificateException, UnsupportedEncodingException,
            NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException,
            BadPaddingException, IllegalBlockSizeException, SOSException {
        String certString = null;
        if(filter.getCertAlias() != null) {
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            DBItemEncCertificate encCert = dbLayer.getEnciphermentCertificate(filter.getCertAlias());
            if(encCert != null) {
                certString = encCert.getCertificate();
            }
        } else {
            certString = filter.getCertificate();
        }
        X509Certificate cert = KeyUtil.getX509Certificate(certString);
        SecretKey key = EncryptionUtils.generateSecretKey(128);
        IvParameterSpec iv = EncryptionUtils.generateIv();
        String encryptedValue = Encrypt.encrypt(EncryptionUtils.CIPHER_ALGORITHM, filter.getToEncrypt(), key, iv);
        String ivBase64Encoded = new String(Base64.getEncoder().encode(iv.getIV()));
        String encryptedKey = new String(EncryptionUtils.encryptSymmetricKey(key, cert));
        EncryptResponse response = new EncryptResponse();
        response.setEncryptedValue(EncryptionUtils.ENCRYPTION_IDENTIFIER 
                + Encrypt.concatOutput(encryptedKey, ivBase64Encoded, encryptedValue));
        return response;
    }
}
