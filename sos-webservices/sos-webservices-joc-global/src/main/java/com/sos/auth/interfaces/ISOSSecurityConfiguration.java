package com.sos.auth.interfaces;

import java.io.IOException;

import org.ini4j.InvalidFileFormatException;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.authentication.DBItemIamIdentityService;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.SecurityConfiguration;

public interface ISOSSecurityConfiguration {

    public SecurityConfiguration readConfiguration() throws InvalidFileFormatException, IOException, JocException, SOSHibernateException;
    public SecurityConfiguration writeConfiguration(SecurityConfiguration securityConfiguration,DBItemIamIdentityService dbItemIamIdentityService) throws Exception;

}
