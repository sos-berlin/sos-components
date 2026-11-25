
package com.sos.sign.model.filetransfer;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.job.Environment;
import com.sos.sign.model.jobresource.JobResource;

/** JobResource
 * <p>
 * deploy object with fixed property 'TYPE':'JobResource' */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "TYPE", "path", "variables", "env" })
public class FileTransferJobResource extends JobResource {

    public FileTransferJobResource() {
        super();
    }

    /** @param path
     * @param settings
     * @param tYPE
     * @param env */
    public FileTransferJobResource(DeployType tYPE, String path, Environment variables, Environment env) {
        super(tYPE, path, variables, env);
    }

}
