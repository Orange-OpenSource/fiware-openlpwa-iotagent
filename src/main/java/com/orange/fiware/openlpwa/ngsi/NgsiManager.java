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

import com.orange.fiware.openlpwa.exception.AgentException;
import com.orange.ngsi.client.NgsiClient;
import com.orange.ngsi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import java.net.http.HttpRequest.BodyPublishers;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.util.jar.Attributes.Name.CONTENT_TYPE;

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
    @Value("${contextBroker.remoteAuthTokenURL}")
    private String contextBrokerRemoteAuthTokenURL;
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



    public void accessToken() throws ExecutionException, InterruptedException {
        String encoding = Base64.getEncoder().encodeToString((contextBrokerRemoteClientId + ":" + contextBrokerRemoteClientSecret).getBytes(StandardCharsets.UTF_8));
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("grant_type", "password");
        parameters.put("username", contextBrokerRemoteUserLogin);
        parameters.put("password", contextBrokerRemoteUserPassword);

        String form = parameters.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(contextBrokerRemoteAuthTokenURL))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + encoding)
                .POST(BodyPublishers.ofString(form))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        String result = response.thenApply(HttpResponse::body).get();
        System.out.println(result);

//        try {
//            HttpUriRequest request = RequestBuilder.post()
//                    .setUri(contextBrokerRemoteAuthTokenURL)
//                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
//                    .setHeader("Authorization", "Basic " + encoding)
//                    .addParameter("grant_type", "password")
//                    .addParameter("username", contextBrokerRemoteUserLogin)
//                    .addParameter("password", contextBrokerRemoteUserPassword)
//                    .build();
//
//            HttpClient client = HttpClientBuilder.create().build();
//            HttpResponse response = client.execute(request);
//            HttpEntity entity = response.getEntity();
//
//            System.out.println(EntityUtils.toString(entity));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
