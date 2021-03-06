/*
 *Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.identity.application.mgt;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.config.IdentityApplicationConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.application.mgt.dao.OAuthApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.SAMLApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.ApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.IdentityProviderDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.OAuthApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.SAMLApplicationDAOImpl;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;

/**
 * This instance holds all the system configurations
 */
public class ApplicationMgtSystemConfig {

    private static final Log log = LogFactory.getLog(ApplicationMgtSystemConfig.class);
    // Configuration elements in the application-authentication.xml
    private static final String CONFIG_ELEMENT_SP_MGT = "ServiceProvidersManagement";
    private static final String CONFIG_APPLICATION_DAO = "ApplicationDAO";
    private static final String CONFIG_OAUTH_OIDC_DAO = "OAuthOIDCClientDAO";
    private static final String CONFIG_SAML_DAO = "SAMLClientDAO";
    private static final String CONFIG_SYSTEM_IDP_DAO = "SystemIDPDAO";
    private static final String CONFIG_CLAIM_DIALECT = "ClaimDialect";
    private static ApplicationMgtSystemConfig instance = null;
    // configured String values
    private String appDAOClassName = null;
    private String oauthDAOClassName = null;
    private String samlDAOClassName = null;
    private String systemIDPDAPClassName = null;
    private String claimDialect = null;


    private ApplicationMgtSystemConfig() {

        synchronized (ApplicationMgtSystemConfig.class) {
            buildSystemConfiguration();
        }
    }

    /**
     * Returns the Singleton of <code>ApplicationMgtSystemConfig</code>
     *
     * @return
     */
    public static ApplicationMgtSystemConfig getInstance() {
        CarbonUtils.checkSecurity();
        if (instance == null) {
            synchronized (ApplicationMgtSystemConfig.class) {
                if (instance == null) {
                    instance = new ApplicationMgtSystemConfig();
                }
            }
        }
        return instance;
    }

    /**
     * Start building the system config
     */
    private void buildSystemConfiguration() {

        IdentityApplicationConfig configParser = IdentityApplicationConfig.getInstance();
        OMElement spConfigElem = configParser.getConfigElement(CONFIG_ELEMENT_SP_MGT);

        if (spConfigElem == null) {
            log.warn("No Identity Application Management configuration found. System Starts with default settings");
        } else {
            // application DAO class
            OMElement appDAOConfigElem =
                    spConfigElem.getFirstChildWithName(getQNameWithIdentityNS(CONFIG_APPLICATION_DAO));
            if (appDAOConfigElem != null) {
                appDAOClassName = appDAOConfigElem.getText().trim();
            }

            // OAuth and OpenID Connect DAO class
            OMElement oauthOidcDAOConfigElem =
                    spConfigElem.getFirstChildWithName(getQNameWithIdentityNS(CONFIG_OAUTH_OIDC_DAO));
            if (oauthOidcDAOConfigElem != null) {
                oauthDAOClassName = oauthOidcDAOConfigElem.getText().trim();
            }

            // SAML DAO class
            OMElement samlDAOConfigElem =
                    spConfigElem.getFirstChildWithName(getQNameWithIdentityNS(CONFIG_SAML_DAO));
            if (samlDAOConfigElem != null) {
                samlDAOClassName = samlDAOConfigElem.getText().trim();
            }

            // IDP DAO class
            OMElement idpDAOConfigElem =
                    spConfigElem.getFirstChildWithName(getQNameWithIdentityNS(CONFIG_SYSTEM_IDP_DAO));
            if (idpDAOConfigElem != null) {
                systemIDPDAPClassName = idpDAOConfigElem.getText().trim();
            }

            OMElement claimDAOConfigElem =
                    spConfigElem.getFirstChildWithName(getQNameWithIdentityNS(CONFIG_CLAIM_DIALECT));
            if (claimDAOConfigElem != null) {
                claimDialect = claimDAOConfigElem.getText().trim();
            }

        }
    }

    private QName getQNameWithIdentityNS(String localPart) {
        return new QName(IdentityApplicationConstants.APPLICATION_AUTHENTICATION_DEFAULT_NAMESPACE, localPart);
    }

    /**
     * Return an instance of the ApplicationDAO
     *
     * @return
     */
    public ApplicationDAO getApplicationDAO() {

        ApplicationDAO applicationDAO = null;

        if (appDAOClassName != null) {

            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(appDAOClassName);
                applicationDAO = (ApplicationDAO) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the ApplicationDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the ApplicationDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the ApplicationDAO ", e);
            }

        } else {
            applicationDAO = new ApplicationDAOImpl();
        }

        return applicationDAO;
    }

    /**
     * Return an instance of the OAuthOIDCClientDAO
     *
     * @return
     */
    public OAuthApplicationDAO getOAuthOIDCClientDAO() {

        OAuthApplicationDAO oauthOidcDAO = null;

        if (oauthDAOClassName != null) {

            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(oauthDAOClassName);
                oauthOidcDAO = (OAuthApplicationDAO) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the OAuthOIDCClientDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the OAuthOIDCClientDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the OAuthOIDCClientDAO ", e);
            }

        } else {
            oauthOidcDAO = new OAuthApplicationDAOImpl();
        }

        return oauthOidcDAO;
    }

    /**
     * Return an instance of the SAMLClientDAO
     *
     * @return
     */
    public SAMLApplicationDAO getSAMLClientDAO() {

        SAMLApplicationDAO samlDAO = null;

        if (samlDAOClassName != null) {

            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(samlDAOClassName);
                samlDAO = (SAMLApplicationDAO) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            }

        } else {
            samlDAO = new SAMLApplicationDAOImpl();
        }

        return samlDAO;
    }


    /**
     * Return an instance of the SystemIDPDAO
     *
     * @return
     */
    public IdentityProviderDAO getIdentityProviderDAO() {

        IdentityProviderDAO idpDAO = null;

        if (systemIDPDAPClassName != null) {

            try {
                // Bundle class loader will cache the loaded class and returned
                // the already loaded instance, hence calling this method
                // multiple times doesn't cost.
                Class clazz = Class.forName(systemIDPDAPClassName);
                idpDAO = (IdentityProviderDAO) clazz.newInstance();

            } catch (ClassNotFoundException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            } catch (InstantiationException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            } catch (IllegalAccessException e) {
                log.error("Error while instantiating the SAMLClientDAO ", e);
            }

        } else {
            idpDAO = new IdentityProviderDAOImpl();
        }

        return idpDAO;
    }

    /**
     * Returns the claim dialect for claim mappings
     *
     * @return
     */
    public String getClaimDialect() {
        if (claimDialect != null) {
            return claimDialect;
        }
        return "http://wso2.org/claims";
    }

}
