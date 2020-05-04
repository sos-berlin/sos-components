package com.sos.joc.publish.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.jobscheduler.db.inventory.DBItemJSDraftObject;
import com.sos.jobscheduler.model.command.UpdateRepo;
import com.sos.jobscheduler.model.deploy.Signature;
import com.sos.jobscheduler.model.deploy.SignatureType;
import com.sos.jobscheduler.model.deploy.SignedObject;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IDeploy;

@Path("publish")
public class DeployImpl extends JOCResourceImpl implements IDeploy {

    private static final String API_CALL = "./publish/deploy";

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, DeployFilter deployFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, deployFilter, xAccessToken, "",
                    /* getPermissonsJocCockpit("", xAccessToken).getPublish().isDeploy() */
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            List<DBItemJSDraftObject> drafts = dbLayer.getFilteredJobSchedulerDraftObjects(deployFilter.getJsObjects());
            List<JobSchedulerId> jsMasters = deployFilter.getSchedulers();
            // Where in the db is the information about the Urls of the masters?
            
            // TODO:
            // read all objects provided in the filter from the database
            // SECLVL HIGH:
            // only signed objects are allowed
            // SECLVL MEDIUM + LOW
            // signed and unsigned objects are allowed
            // existing signatures of objects are verified
            // SECLVL MEDIUM
            // unsigned objects are signed with the user private key automatically
            // SECLVL LOW
            // unsigned objects are signed with the default key automatically
            // call UpdateRepo for all provided JobScheduler Masters
            // update mapping table for JSObject -> JobScheduler Master relation
            // update the existing draft object
            // * new commitHash (property versionId)
            // * clear signature
            //
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private void updateRepo (List<DBItemJSDraftObject> drafts)
            throws IllegalArgumentException, UriBuilderException, JsonProcessingException, SOSException {
        UpdateRepo updateRepo = new UpdateRepo();
        updateRepo.setVersionId("PUT_NEW_GENERATED_VERSION_ID_HERE");
        for (DBItemJSDraftObject draft : drafts) {
            SignedObject signedObject = new SignedObject();
            signedObject.setString(draft.getContent());
            Signature signature = new Signature();
            signature.setTYPE(SignatureType.PGP);
            signature.setSignatureString(draft.getSignedContent());
            signedObject.setSignature(signature);
            updateRepo.getChange().add(signedObject);
        }
        SOSRestApiClient httpClient = new SOSRestApiClient();
        httpClient.setAllowAllHostnameVerifier(false);
        httpClient.setBasicAuthorization("VGVzdDp0ZXN0"); // Woher bekomm ich die BasicAuthorization?
        httpClient.addHeader("Accept", "application/json");
        httpClient.addHeader("Content-Type", "application/json");
        // for each Master
        String response = httpClient.postRestService(
                UriBuilder.fromPath("http://localhost:4222/master/api/command").build(), Globals.objectMapper.writeValueAsString(updateRepo));

    }
}
