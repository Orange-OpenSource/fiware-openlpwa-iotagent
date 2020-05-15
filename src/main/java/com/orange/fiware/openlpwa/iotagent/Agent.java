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

package com.orange.fiware.openlpwa.iotagent;

import com.orange.fiware.openlpwa.exception.AgentException;
import com.orange.fiware.openlpwa.provider.OpenLpwaMqttProvider;
import com.orange.fiware.openlpwa.provider.OpenLpwaMqttProviderCallback;
import com.orange.fiware.openlpwa.provider.model.DeviceIncomingMessage;
import com.orange.ngsi.model.ContextAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@EnableScheduling
@Service
public class Agent {

    private static final Logger logger = LoggerFactory.getLogger(Agent.class);
    @Autowired
    private OpenLpwaMqttProvider openLpwaMqttProvider;
    @Autowired
    private AgentMqttProviderCallback mqttClientCallback;
    private OpenLpwaNgsiConverter converter;
    private AgentConnectionLostCallback connectionLostCallback;
    private boolean reconnecting = false;

    public void setConnectionLostCallback(AgentConnectionLostCallback connectionLostCallback) {
        this.connectionLostCallback = connectionLostCallback;
    }

    /**
     * Start the IoT agent
     * @param converter converter
     * @param successCallback   Callback called when the agent is correctly started
     * @param failureCallback   Callback called when an error occurs
     */
    public void start(OpenLpwaNgsiConverter converter, AgentSuccessCallback successCallback, AgentFailureCallback failureCallback) {
        logger.debug("Starting the agent");
        if (converter != null) {
            this.converter = converter;
        } else {
            launchFailureCallback(failureCallback, new AgentException("Custom OpenLpwaNgsiConverter is missing."));
            return;
        }

        // Initialization
        openLpwaMqttProvider.setClientCallback(mqttClientCallback);

        // Connect to the Mqtt broker
        logger.debug("Connecting to the Mqtt broker");
        openLpwaMqttProvider.connect(
                connectedClientId -> {
                    logger.debug("Connected to the Mqtt broker");
                    // Subscribe to the Mqtt topic
                    logger.debug("Subscribing to the Mqtt topic");
                    openLpwaMqttProvider.subscribe(
                            subscribedClientId -> {
                                logger.debug("Subscribed to the Mqtt topic");
                                launchSuccessCallback(successCallback);
                            },
                            ex -> {
                                openLpwaMqttProvider.disconnect(null, null);
                                String errorMsg = "Unable to subscribe to Mqtt topic";
                                logger.error(errorMsg, ex);
                                launchFailureCallback(failureCallback, new AgentException(errorMsg, ex));
                            });
                },
                ex -> {
                    String errorMsg = "Unable to connect to the Mqtt broker";
                    logger.error(errorMsg, ex);
                    launchFailureCallback(failureCallback, new AgentException(errorMsg, ex));
                });
    }

    /**
     * Stop the IoT agent
     * @param successCallback   Callback called when the agent is correctly stopped
     * @param failureCallback   Callback called when an error occurs
     */
    public void stop(AgentSuccessCallback successCallback, AgentFailureCallback failureCallback) {
        // Disconnect from the Mqtt broker
        openLpwaMqttProvider.disconnect(
                disconnectedClientId -> {
                    logger.debug("Disconnected from the Mqtt broker");
                    launchSuccessCallback(successCallback);
                },
                ex -> {
                    String errorMsg = "Unable to disconnect from the Mqtt broker";
                    logger.error(errorMsg, ex);
                    launchFailureCallback(failureCallback, new AgentException(errorMsg, ex));
                }
        );
    }

    /**
     * Launch a AgentSuccessCallback if not null
     * @param successCallback   AgentSuccessCallback to launch
     */
    private void launchSuccessCallback(AgentSuccessCallback successCallback) {
        if (successCallback != null) {
            successCallback.onSuccess();
        }
    }

    /**
     * Launch a AgentFailureCallback if not null
     * @param failureCallback   AgentFailureCallback to launch
     * @param exception         AgentException
     */
    private void launchFailureCallback(AgentFailureCallback failureCallback, AgentException exception) {
        if (failureCallback != null) {
            failureCallback.onFailure(exception);
        }
    }

    /**
     * Manages Mqtt events
     */
    @Component
    class AgentMqttProviderCallback implements OpenLpwaMqttProviderCallback {

        private int reconnectingDelay = 5000;

        @Override
        public void connectionLost(Throwable throwable) {
            // Prevent a new connectionLost event while the reconnection
            if (!reconnecting) {
                logger.warn("Connection lost with the MQTT broker, waiting before reconnect", throwable);
                try {
                    Thread.sleep(reconnectingDelay);
                } catch (InterruptedException e) {
                }
                logger.debug("Reconnecting to the MQTT broker");
                reconnecting = true;
                start(converter,
                        () -> {
                            logger.debug("Reconnected to the MQTT broker");
                            reconnecting = false;
                        },
                        ex -> {
                            logger.error("Reconnection failed to the MQTT broker", ex);
                            reconnecting = false;
                            if (connectionLostCallback != null) {
                                connectionLostCallback.onConnectionLost();
                            }
                        }
                );
            }
        }

        @Override
        public void newMessageArrived(String deviceID, DeviceIncomingMessage incomingMessage) {
            String payload = null;
            if (incomingMessage != null && incomingMessage.getValue() != null && incomingMessage.getData() != null) {
                payload = incomingMessage.getData();
            }
            if (payload == null) {
                logger.error("Payload not found, can't treat message (ID:{})", deviceID);
                return;
            }

            if (converter != null) {
                List<ContextAttribute> decodedAttributes = converter.decodeData(deviceID, payload, incomingMessage);
            } else {
                logger.error("Converter is not defined, message is not treated. (ID:{}, message:{})", deviceID, incomingMessage);
            }
        }
    }
}
