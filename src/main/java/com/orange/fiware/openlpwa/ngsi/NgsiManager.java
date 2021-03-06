/**
 * Copyright (C) 2016 Orange
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * * Created by Christophe AZEMAR on 28/06/2016.
 */

package com.orange.fiware.openlpwa.ngsi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.fiware.openlpwa.exception.AgentException;
import com.orange.ngsi.client.NgsiClient;
import com.orange.ngsi.model.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Manage Ngsi operations
 */
@Service
public class NgsiManager {

    private static Logger logger = LoggerFactory.getLogger(NgsiManager.class);
    public final static String COMMAND_SUFFIX = "_command";
    public final static String COMMAND_STATUS_SUFFIX = "_commandStatus";
    public final static String COMMAND_SENT = "SENT";
    public final static String COMMAND_ERROR = "ERROR";
    @Value("${contextBroker.localUrl}")
    private String contextBrokerLocalUrl;
    @Value("${contextBroker.remoteUrl}")
    private String contextBrokerRemoteUrl;
    @Value("${contextBroker.remoteAuthToken}")
    private String contextBrokerRemoteAuthToken;
    @Value("${contextBroker.remoteAuthTokenURI}")
    private String contextBrokerRemoteAuthTokenURI;
    @Value("${contextBroker.remoteClientId}")
    private String contextBrokerRemoteClientId;
    @Value("${contextBroker.remoteClientSecret}")
    private String contextBrokerRemoteClientSecret;
    @Value("${contextBroker.remoteUserLogin}")
    private String contextBrokerRemoteUserLogin;
    @Value("${contextBroker.remoteUserPassword}")
    private String contextBrokerRemoteUserPassword;

    @Value("${contextBroker.remoteFiwareService}")
    private String contextBrokerRemoteFiwareService;
    @Value("${contextBroker.remoteFiwareServicePath}")
    private String contextBrokerRemoteFiwareServicePath;
    @Autowired
    private NgsiClient ngsiClient;


    /**
     * Updates a subscription
     *
     * @param subscriptionId Subscription identifier
     * @return A future for a UpdateContextSubscriptionResponse
     * @throws AgentException when the subscriptionId is null
     */
    public ListenableFuture<UpdateContextSubscriptionResponse> updateSubscription(String subscriptionId) throws AgentException {
        if (subscriptionId == null) {
            String errorMsg = "Unable to update subscription for a null subscriptionId";
            logger.error(errorMsg);
            throw new AgentException(errorMsg);
        }
        UpdateContextSubscription updateContextSubscription = new UpdateContextSubscription();
        updateContextSubscription.setSubscriptionId(subscriptionId);
        updateContextSubscription.setDuration("P1M");
        return ngsiClient.updateContextSubscription(contextBrokerRemoteUrl, remoteHeaders(), updateContextSubscription);
    }

    /**
     * Unsubcribes a device
     *
     * @param subscriptionId Subscription identifier
     * @return A future for a UnsubscribeContextResponse
     * @throws AgentException when the subscriptionId is null
     */
    public ListenableFuture<UnsubscribeContextResponse> unsubscribe(String subscriptionId) throws AgentException {
        if (subscriptionId == null) {
            String errorMsg = "Unable to unsubscribe for a null subscriptionId";
            logger.error(errorMsg);
            throw new AgentException(errorMsg);
        }
        return ngsiClient.unsubscribeContext(contextBrokerRemoteUrl, remoteHeaders(), subscriptionId);
    }

    /**
     * Updates device attributes sending an updateContext request to the context broker with the deviceID
     *
     * @param deviceID      Device identifier
     * @param attributeList Attributes to update
     * @return A future for UpdateContextResponse
     * @throws AgentException when the deviceID is null or where there isn't an attribute to update
     */
    public ListenableFuture<UpdateContextResponse> updateDeviceAttributes(String deviceID, List<ContextAttribute> attributeList) throws AgentException {
        UpdateContext context = new UpdateContext();
        context.setUpdateAction(UpdateAction.APPEND);
        List<ContextElement> elementList = new ArrayList<>();
        ContextElement element = new ContextElement();
        element.setEntityId(new EntityId(deviceID, "", false));
        element.setContextAttributeList(attributeList);
        elementList.add(element);
        context.setContextElements(elementList);
        logger.info("Sending an update request to the context broker (deviceID:{}, context:{}, list:{})", deviceID, context, attributeList);
        return ngsiClient.updateContext(contextBrokerRemoteUrl, remoteHeaders(), context);
    }

    /**
     * Generates headers for Context Broker
     *
     * @return Http headers for provided configuration
     */
    private HttpHeaders remoteHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();


        if (contextBrokerRemoteAuthToken == null) {
            setAccessTokenSync();
        }
        if (contextBrokerRemoteAuthToken != null) {
            httpHeaders.set("X-Auth-Token", contextBrokerRemoteAuthToken);
        }
        if (contextBrokerRemoteFiwareService != null && contextBrokerRemoteFiwareService.length() > 0) {
            httpHeaders.set("Fiware-Service", contextBrokerRemoteFiwareService);
        }
        if (contextBrokerRemoteFiwareServicePath != null && contextBrokerRemoteFiwareServicePath.length() > 0) {
            httpHeaders.set("Fiware-ServicePath", contextBrokerRemoteFiwareServicePath);
        }
        httpHeaders.set("Accept", "application/json");
        httpHeaders.set("Content-Type", "application/json");

        return httpHeaders;
    }
    
    public void setAccessTokenSync() {

        if (contextBrokerRemoteClientId == null || contextBrokerRemoteClientSecret == null ||
                contextBrokerRemoteUserLogin == null || contextBrokerRemoteUserPassword == null) {
            return;
        }

        String encoding = Base64.getEncoder().encodeToString((contextBrokerRemoteClientId + ":" + contextBrokerRemoteClientSecret).getBytes(StandardCharsets.UTF_8));
        try {
            HttpUriRequest request = RequestBuilder.post()
                    .setUri(contextBrokerRemoteAuthTokenURI)
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .setHeader("Authorization", "Basic " + encoding)
                    .addParameter("grant_type", "password")
                    .addParameter("username", contextBrokerRemoteUserLogin)
                    .addParameter("password", contextBrokerRemoteUserPassword)
                    .build();

            CloseableHttpClient client = HttpClientBuilder.create().build();
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            if (response.getStatusLine().getStatusCode() != 200) {
                logger.error("Unable to retrieve token: status{}. Please check your credentials.", response.getStatusLine());
            } else {
                this.contextBrokerRemoteAuthToken = tokenParsing(EntityUtils.toString(entity)).getAccess_token();
                logger.info("Token successfully recovered");
            }
        } catch (IOException e) {
            logger.error("An error occurred while retrieving the token. error{}", e.getMessage());
        }
    }

    private TokenResponse tokenParsing(String tokenJsonReponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(tokenJsonReponse, TokenResponse.class);
        } catch (JsonProcessingException e) {
            logger.error("An error occurred while parsing the token. error{}", e.getMessage());
        }
        return null;
    }
}
