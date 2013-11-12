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
import static com.neusoft.babymonitor.backend.webcam.Constants.COMMANDS;
import static com.neusoft.babymonitor.backend.webcam.Constants.HARDWARE;
import static com.neusoft.babymonitor.backend.webcam.Constants.LOGIN_URL;
import static com.neusoft.babymonitor.backend.webcam.Constants.PROTOCOL;
import static com.neusoft.babymonitor.backend.webcam.Constants.STREAMING;
import static com.neusoft.babymonitor.backend.webcam.Constants.VIDEO;
import static com.neusoft.babymonitor.backend.webcam.Constants.WEBCAM;
import static com.neusoft.babymonitor.backend.webcam.Constants.WEBCAM_CODE;

import com.neusoft.babymonitor.backend.webcam.exception.AuthenticationException;
import com.neusoft.babymonitor.backend.webcam.exception.HttpConnectionException;
import com.neusoft.babymonitor.backend.webcam.exception.InvalidTokenException;
import com.neusoft.babymonitor.backend.webcam.exception.ServerException;
import com.neusoft.babymonitor.backend.webcam.model.CommandMessage;
import com.neusoft.babymonitor.backend.webcam.model.request.StreamingURLRequest;
import com.neusoft.babymonitor.backend.webcam.model.response.WebcamCodeResponse;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ClientTemplate extends RestTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTemplate.class);

    /** Base64 encoded value for authorization header containing client id and secret. */
    private static final String AUTHORIZATION_HEADER_VALUE = "Basic bW9iaWxlX2FuZHJvaWQ6NjEzMjY5OTY5MWE4MmVhMzMzZGFjNWFkZmJlYjc1OWI0N2Q1NDlhOA==";
    private static final int CONNECTION_TIMEOUT = 10 * 000;
    // if there is no response in 10 minutes retry the connection
    private static final int READ_TIMEOUT = 10 * 60 * 1000;

    public String server;

    public ClientTemplate(String host, int port) {
        server = PROTOCOL + host + ":" + port + "/api";
        HttpComponentsClientHttpRequestFactory httpClientFactory = new HttpComponentsClientHttpRequestFactory();

        httpClientFactory.setHttpClient(new CertifiedHttpsClient());
        httpClientFactory.setConnectTimeout(CONNECTION_TIMEOUT);
        httpClientFactory.setReadTimeout(READ_TIMEOUT);
        this.setRequestFactory(httpClientFactory);
        this.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        this.getMessageConverters().add(new FormHttpMessageConverter());
        LOGGER.info("Initialized REST template with server {}", server);
    }

    /**
     * Performs the login request (OAuth 2.0).
     * 
     * @param username the resource owner username
     * @param password the resource owner password
     * @return {@link OAuthResponse} a standard OAuth2.0 response
     * @throws RestClientException
     * @throws HttpConnectionException
     * @throws ServerException
     * @throws AuthenticationException
     */
    public OAuthResponse login(String username, String password) throws RestClientException, HttpConnectionException,
            ServerException, AuthenticationException {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "password");
        form.add("username", username);
        form.add("password", password);

        return oauthRequest(form);
    }

    /**
     * Performs the refresh_token request (OAuth 2.0).
     * 
     * @param refreshToken the refresh token received at login
     * @return {@link OAuthResponse} a standard OAuth2.0 response
     * @throws RestClientException
     * @throws HttpConnectionException
     * @throws ServerException
     * @throws AuthenticationException
     */
    public OAuthResponse refreshToken(String refreshToken) throws RestClientException, HttpConnectionException,
            ServerException, AuthenticationException {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", refreshToken);

        return oauthRequest(form);
    }

    /**
     * Performs an OAuth2.0 request (get token or refresh token) depending on the parameters received.
     * 
     * @param requestForm a {@link MultiValueMap} containing the x-www-form-urlencoded data
     * @return {@link OauthResponse}
     * @throws RestClientException
     * @throws HttpConnectionException
     * @throws ServerException
     * @throws AuthenticationException
     */
    private OAuthResponse oauthRequest(MultiValueMap<String, String> requestForm) throws RestClientException,
            HttpConnectionException, ServerException, AuthenticationException {
        HttpHeaders requestHeaders = initLoginHeaders();
        try {
            // Make the HTTP GET request, marshaling the response from JSON to
            // an array of Events
            ResponseEntity<OAuthResponse> responseEntity = this.exchange(LOGIN_URL, HttpMethod.POST,
                    new HttpEntity<MultiValueMap<String, String>>(requestForm, requestHeaders), OAuthResponse.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                OAuthResponse loginResponse = responseEntity.getBody();
                return loginResponse;
            } else {
                LOGGER.error("Server response: {}", responseEntity.getStatusCode());
                throw new RestClientException("Server did not respond ok. Code: " + responseEntity.getStatusCode());
            }
        } catch (ResourceAccessException e) {
            LOGGER.error("ResourceAccessException: {}", e);
            throw new HttpConnectionException("Could not login.", e);
        } catch (HttpClientErrorException ex) {
            LOGGER.error("HttpClientErrorException: {}", ex.getResponseBodyAsString());
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new AuthenticationException("Unable to login.", ex);
            } else {
                throw new RestClientException("Could not login.", ex);
            }
        } catch (HttpServerErrorException e) {
            throw new ServerException("Could not login.", e);
        }
    }

    public CommandMessage getCommand(Long webcamCode) throws HttpConnectionException, InvalidTokenException,
            ServerException {
        LOGGER.debug("Trying to get command...");
        try {
            return this.getForObject(server + HARDWARE + COMMANDS + "/" + webcamCode, CommandMessage.class);
        } catch (ResourceAccessException e) {
            LOGGER.error("ResourceAccessException: {}", e);
            throw new HttpConnectionException("Could not get commands list.", e);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new InvalidTokenException("Tried to get list of commands but authorization denied.");
            } else {
                LOGGER.error("HttpClientErrorException): {} ", ex);
                throw new RestClientException("Could not get commands list.", ex);
            }
        } catch (HttpServerErrorException e) {
            throw new ServerException("Could not get commands list.", e);
        } catch (Exception e) {
            System.out.println(" the exception is e: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks the webcam code.
     * 
     * @param webcamCode the code that needs to be verified
     * @return
     * @throws HttpConnectionException
     * @throws InvalidTokenException
     * @throws ServerException
     */
    public WebcamCodeResponse checkWebcamCode(Long webcamCode) throws HttpConnectionException, InvalidTokenException,
            ServerException {
        LOGGER.debug("Check the webcam code {}", webcamCode);
        try {
            return this.getForObject(server + STREAMING + WEBCAM_CODE + "/" + webcamCode, WebcamCodeResponse.class);
        } catch (ResourceAccessException e) {
            LOGGER.error("ResourceAccessException: {}", e);
            throw new HttpConnectionException("Could not check the webcam code.", e);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new InvalidTokenException("Tried to check the webcam code but authorization denied.");
            } else {
                LOGGER.error("HttpClientErrorException): {} ", ex);
                throw new RestClientException("Could not check the webcam code.", ex);
            }
        } catch (HttpServerErrorException e) {
            throw new ServerException("Could not check the webcam code.", e);
        } catch (Exception e) {
            System.out.println(" the exception is e: " + e.getMessage());
            return null;
        }
    }

    public void sendVideoStreamingURL(Long webcamCode, String url) throws HttpConnectionException,
            InvalidTokenException, ServerException {
        LOGGER.debug("Sending the video streaming URL");

        // get the URL
        StreamingURLRequest streamingUrl = new StreamingURLRequest(url);

        LOGGER.debug(" Sending URL {}", url);
        HttpHeaders requestHeaders = initHeadersWithoutOauth();
        HttpEntity<StreamingURLRequest> requestEntity = new HttpEntity<StreamingURLRequest>(streamingUrl,
                requestHeaders);

        // Make a POST request in order to send the video streaming URL
        try {
            this.postForLocation(server + HARDWARE + COMMANDS + WEBCAM + VIDEO + "/" + webcamCode, requestEntity);
        } catch (ResourceAccessException e) {
            LOGGER.error("ResourceAccessException: {}", e);
            throw new HttpConnectionException("Could not get commands list.", e);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new InvalidTokenException("Tried to get list of commands but authorization denied.");
            } else {
                LOGGER.error("HttpClientErrorException): {} ", ex);
                throw new RestClientException("Could not get commands list.", ex);
            }
        } catch (HttpServerErrorException e) {
            throw new ServerException("Could not get commands list.", e);
        }
    }

    private HttpHeaders initHeadersWithoutOauth() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
        requestHeaders.setCacheControl("no-cache");
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        LOGGER.trace("Initialized headers without oauth: {}", requestHeaders);
        return requestHeaders;
    }

    private HttpHeaders initLoginHeaders() {
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
        requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        requestHeaders.setCacheControl("no-cache");
        requestHeaders.set("Authorization", AUTHORIZATION_HEADER_VALUE);
        LOGGER.trace("Initialized login headers : {}", requestHeaders);
        return requestHeaders;
    }

}