/**
 * Copyright (C) 2016 Orange
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * * Created by Christophe AZEMAR on 28/06/2016.
 */

package com.orange.fiware.openlpwa.ngsi;

import com.orange.fiware.openlpwa.domain.DeviceEntity;
import com.orange.fiware.openlpwa.exception.AgentException;
import com.orange.fiware.openlpwa.iotagent.Device;
import com.orange.fiware.openlpwa.repository.DeviceEntityRepository;
import com.orange.ngsi.client.NgsiClient;
import com.orange.ngsi.model.*;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

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
    @Value("${contextBroker.remoteFiwareService}")
    private String contextBrokerRemoteFiwareService;
    @Value("${contextBroker.remoteFiwareServicePath}")
    private String contextBrokerRemoteFiwareServicePath;
    @Autowired
    private NgsiClient ngsiClient;
    @Autowired
    private DeviceEntityRepository deviceRepository;

    /**
     * Subscribes a device into NGSI Context broker to receive commands
     * @param device Device to register
     * @return A future for a SubscribeContextResponse
     * @throws AgentException when the device is incorrect (null, without deviceEUI or commands)
     */
    public ListenableFuture<SubscribeContextResponse> subscribeToCommands(Device device) throws AgentException {
        if (device == null || device.getDeviceID() == null) {
            String errorMsg = "Unable to subscribe to a null device or with a null EUI.";
            logger.error(errorMsg);
            throw new AgentException(errorMsg);
        }
        else if (device.getCommands() == null || device.getCommands().size() == 0) {
            String errorMsg = String.format("Unable to subscribe to a device without commands (%s).", device);
            logger.error(errorMsg);
            throw new AgentException(errorMsg);
        }
        // Check if subscription is already done on CB
        Optional<DeviceEntity> deviceRegistered = deviceRepository.findById(device.getDeviceID());
        if (deviceRegistered.isPresent() && deviceRegistered.get().getSubscriptionId() != null) {
            logger.debug("A subscription already exists on CB, call unsubscribe (subscriptionId:{}, EUI: {})", deviceRegistered.get().getSubscriptionId(), device.getDeviceID());
            unsubscribe(deviceRegistered.get().getSubscriptionId());
        }
        SubscribeContext context = new SubscribeContext();
        List<EntityId> entities = new ArrayList<>();
        entities.add(new EntityId(device.getEntityName(), device.getEntityType(), false));
        context.setEntityIdList(entities);
        context.setDuration("P1M");
        List<NotifyCondition> conditions = new ArrayList<>();
        List<String> attributesList = device.getCommands().stream().map(s -> s + COMMAND_SUFFIX).collect(Collectors.toList());
        context.setAttributeList(attributesList);
        context.setReference(URI.create(contextBrokerLocalUrl + "/v1/notifyContext/" + device.getDeviceID()));
        conditions.add(new NotifyCondition(NotifyConditionEnum.ONCHANGE, attributesList));
        context.setNotifyConditionList(conditions);
        logger.debug("Subscribe to Context Broker, url : {}", contextBrokerRemoteUrl);
        return ngsiClient.subscribeContext(contextBrokerRemoteUrl, remoteHeaders(), context);
    }

    /**
     * Updates a subscription
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
     * Updates device attributes sending an updateContext request to the context broker
     * @param device Device to update
     * @param attributes Attributes to update
     * @return A future for UpdateContextResponse
     * @throws AgentException when the device is null
     */
    public ListenableFuture<UpdateContextResponse> updateDeviceAttributes(DeviceEntity device, List<ContextAttribute> attributes) throws AgentException {
        if (device == null) {
            String errorMsg = "Receive a null device on updateDeviceAttributes";
            logger.error(errorMsg);
            throw new AgentException(errorMsg);
        }
        return updateDeviceAttributes(new EntityId(device.getName(), device.getType(), false), attributes);
    }

    /**
     * Updates device attributes sending an updateContext request to the context broker with the entityId
     * @param entityId Entity identifier
     * @param attributeList Attributes to update
     * @return A future for UpdateContextResponse
     * @throws AgentException when the entityId is null or where there isn't an attribute to update
     */
    public ListenableFuture<UpdateContextResponse> updateDeviceAttributes(EntityId entityId, List<ContextAttribute> attributeList) throws AgentException {
        if (entityId == null || attributeList == null || attributeList.size() == 0) {
            String errorMsg = String.format("Receive a null entity or a null or empty attributes list (entity:%s/attributes:%s)", entityId, attributeList);
            logger.error(errorMsg);
            throw new AgentException(errorMsg);
        }
        UpdateContext context = new UpdateContext();
        context.setUpdateAction(UpdateAction.APPEND);
        List<ContextElement> elementList = new ArrayList<>();
        ContextElement element = new ContextElement();
        element.setEntityId(entityId);
        element.setContextAttributeList(attributeList);
        elementList.add(element);
        context.setContextElements(elementList);
        logger.debug("Call updateContext (entityId:{}, context:{}, list:{})", entityId, context, attributeList);
        return ngsiClient.updateContext(contextBrokerRemoteUrl, remoteHeaders(), context);
    }

    /**
     * Generates headers for Context Broker
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
}
