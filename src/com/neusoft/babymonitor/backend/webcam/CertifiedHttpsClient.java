package com.neusoft.babymonitor.backend.webcam;

/*
 This file is part of “Onni smart care desktop application” software
 Copyright (C) <2013>  Erasmus van Niekerk <erasmus.van.niekerk@sepsolutions.fi>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import com.neusoft.babymonitor.backend.webcam.CertifiedHttpsClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertifiedHttpsClient extends DefaultHttpClient {

    private static final Logger LOG = LoggerFactory.getLogger(CertifiedHttpsClient.class);

    private static final int HTTPS_PORT = 8443;

    private static final String HTTPS_SCHEME = "https";

    /**
     * @see org.apache.http.impl.client.DefaultHttpClient#createClientConnectionManager()
     */
    @Override
    protected ClientConnectionManager createClientConnectionManager() {
        SchemeRegistry registry = null;
        // to the ConnectionManager
        try {
            registry = createSchemeRegistry();
        } catch (CertificateException e) {
            LOG.error("Error loading certificate, certificate"
                    + " has either expired, is not yet valid or is malformed", e);
        } catch (IOException e) {
            LOG.error("Could not load certificate, check certificate or update the application", e);
        } catch (KeyStoreException e) {
            LOG.error("Could not create SSL Socket Factory  or get keystore instance", e);
        } catch (KeyManagementException e) {
            LOG.error("Could not create SSL Socket Factory ", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Algorithm used for keystore is not available.");
        } catch (UnrecoverableKeyException e) {
            LOG.error("Could not get key from keystore", e);
        }

        return new PoolingClientConnectionManager(registry);

    }

    public SchemeRegistry createSchemeRegistry() throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {

        // load a trusting manager that accepts all certificates, the keystore is ignored
        TrustManager[] tm = getInsecureTrustingManager();

        SSLContext sslcontext = SSLContext.getInstance(SSLSocketFactory.SSL);
        sslcontext.init(null, tm, null);
        SSLSocketFactory sslsf = new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme(HTTPS_SCHEME, HTTPS_PORT, sslsf));

        return registry;

    }

    private TrustManager[] getInsecureTrustingManager() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                LOG.warn("XXX INSECURE XXX - Accepting all issuers");
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                LOG.warn("XXX INSECURE XXX - Trusting all clients");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                LOG.warn("XXX INSECURE XXX - Trusting all servers");
            }

        } };
        return trustAllCerts;
    }
}
