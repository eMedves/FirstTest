/**

    Copyright 2007, 2008 Engineering Ingegneria Informatica S.p.A.

    This file is part of Spagic.

    Spagic is free software; you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    any later version.

    Spagic is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
**/
/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.spagic3.connectors.tcp.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spagic3.connectors.tcp.TCPBCConfig;




/**
 * Factory to create a TCPBC SSLContext.
 */
public class TCPBCSSLContextFactory
{
	private static final Logger log = LoggerFactory.getLogger(TCPBCSSLContextFactory.class);
	
	private static final String CLASSPATH_PREFIX = "classpath:";
	
    /**
     * Protocol to use.
     * TODO check if you need to configure the protocol
     */
    private static final String PROTOCOL = "TLS";
    
    private static final String KEY_MANAGER_FACTORY_ALGORITHM;
    private static final String TRUST_MANAGER_FACTORY_ALGORITHM;

    static {
        String algorithm = Security.getProperty( "ssl.KeyManagerFactory.algorithm" );
        if( algorithm == null )
        {
            algorithm = "SunX509";
        }
        
        KEY_MANAGER_FACTORY_ALGORITHM = algorithm;
        algorithm = Security.getProperty( "ssl.TrustManagerFactory.algorithm" );
        if (algorithm == null) {
        	algorithm = "PKIX";
        }
        TRUST_MANAGER_FACTORY_ALGORITHM = algorithm;
    }

    // NOTE: The keystore was generated using keytool:
    
//    keytool -genkey -alias tcpbc -keyalg RSA -keysize 512
//    		-dname "CN=tcpbc, OU=DCRI, O=Engineering, L=Padova, C=IT"
//    		-validity 3650 -keypass tcpbcpw -keystore tcpbc.cert
//    		-storepass tcpbcpw

    private static URL getResourceURL(String location) {
    	//TODO : Rivedere questo metodo che usava il classloader
    	URL locationUrl = null;
    	return locationUrl;
    	
    	/*
    	try {
    		if (location.startsWith(CLASSPATH_PREFIX)) {
    			location = location.substring(CLASSPATH_PREFIX.length());
    		}
    		locationUrl = new ClassPathResource(location).getURL();
    	} catch (IOException ioe) {
    		log.error("Error retrieving resource url", ioe);
    	}
    	return locationUrl;
    	*/
    }
    
    /**
     * Get SSLContext using configuration.
     *
     * @return SSLContext
     * @throws java.security.GeneralSecurityException
     *
     */
    public static SSLContext getInstance(TCPBCConfig config)
            throws GeneralSecurityException
    {
        SSLContext retInstance = null;

        synchronized( TCPBCSSLContextFactory.class )
        {
            try
            {
            	String keyStoreType = config.getKeyStoreType();
            	if (keyStoreType == null)
            		keyStoreType = "JKS";
            	
                KeyStore ks = KeyStore.getInstance( keyStoreType );
                InputStream kin = null;
                try
                {
                	URL locUrl = getResourceURL(config.getKeyStoreFileName());
                	kin = locUrl.openStream();
//                    kin = new FileInputStream(config.getKeyStoreFileName());
                    ks.load( kin, config.getKeyStorePassword().toCharArray() );
                }
                finally
                {
                    if( kin != null )
                    {
                        try
                        {
                            kin.close();
                        }
                        catch( IOException ignored )
                        {
                        }
                    }
                }

                // Set up key manager factory to use our key store
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                        KEY_MANAGER_FACTORY_ALGORITHM );
                kmf.init( ks, config.getKeyStorePassword().toCharArray() );

                // Initialize the SSLContext to work with our key managers.
                retInstance = SSLContext.getInstance( PROTOCOL );

                if (config.isUseSSLClientMode()) {
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    		TRUST_MANAGER_FACTORY_ALGORITHM );

                    String trustStoreType = config.getTrustStoreType();
                	if (trustStoreType == null)
                		trustStoreType = "JKS";
                    KeyStore ts = KeyStore.getInstance( trustStoreType );
                    
                    InputStream tin = null;
                    try
                    {
                    	URL locUrl = getResourceURL(config.getTrustStoreFileName());
                    	tin = locUrl.openStream();
//                    	tin = new FileInputStream(config.getTrustStoreFileName());
                    	ts.load( tin, config.getTrustStorePassword().toCharArray() );
                    }
                    finally
                    {
                        if( tin != null )
                        {
                            try
                            {
                                tin.close();
                            }
                            catch( IOException ignored )
                            {
                            }
                        }
                    }
                    
                    tmf.init(ts);
                    
                    retInstance.init(
                        	kmf.getKeyManagers(), tmf.getTrustManagers(), null );
                    
                } else {
                	// No trust manager
                    retInstance.init(
                        	kmf.getKeyManagers(), null, null );
                }
                
                log.debug("Server SSLContext created");
            }
            catch( Exception ioe )
            {
                throw new GeneralSecurityException(
                        "Can't create Server SSLContext:" + ioe );
            }
        }

        return retInstance;
    }

}
