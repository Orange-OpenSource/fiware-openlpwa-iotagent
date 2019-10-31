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
 * * Created by François SUC on 11/07/2016.
 */

package com.orange.fiware.openlpwa.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.fiware.openlpwa.exception.ConfigurationException;
import com.orange.fiware.openlpwa.provider.model.DeviceIncomingMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.SuccessCallback;

import static org.eclipse.paho.client.mqttv3.MqttConnectOptions.MQTT_VERSION_3_1;

/**
 * Provides methods to connect, subscribe and consume OpenLpwa provider Mqtt interface
 */
@Service
public class OpenLpwaMqttProvider implements MqttCallback {

    private static Logger logger = LoggerFactory.getLogger(OpenLpwaMqttProvider.class);
    private final static String userName = "payload";
    private final static String topicPath = "router/~event/v1/data/new/typ/+/dev/+/con/lora/evt/+/grp/#";
    private final static int subscribeQOS = 1;
    private String serverUri;
    private String clientId;
    private String apiKey;
    private OpenLpwaMqttProviderCallback clientCallback;
    private MqttAsyncClient mqttAsyncClient;

    public String getClientId() {
        return clientId;
    }

    public void setClientCallback(OpenLpwaMqttProviderCallback clientCallback) {
        this.clientCallback = clientCallback;
    }

    @Autowired
    public OpenLpwaMqttProvider(@Value("${openLpwaProvider.mqttUri}") String serverUri,
                                @Value("${openLpwaProvider.mqttClientId}") String clientId,
                                @Value("${openLpwaProvider.apiKey}") String apiKey,
                                OpenLpwaMqttProviderCallback clientCallback) throws ConfigurationException, MqttException {
        this.serverUri = serverUri;
        this.clientId = clientId != null ? clientId : MqttAsyncClient.generateClientId();
        this.apiKey = apiKey;
        this.clientCallback = clientCallback;

        checkOpenLpwaMqttProviderInitialization();

        mqttAsyncClient = new MqttAsyncClient(this.serverUri, this.clientId, null);
        mqttAsyncClient.setCallback(this);
    }

    /**
     * Connects to the Mqtt broker asynchronously
     * @param successCallback Callback when the connection succeeds
     * @param failureCallback Callback when the connection fails
     */
    public void connect(SuccessCallback<String> successCallback, FailureCallback failureCallback) {
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setMqttVersion(MQTT_VERSION_3_1);
        connectOptions.setCleanSession(true);
        connectOptions.setKeepAliveInterval(30);
        connectOptions.setUserName(userName);
        connectOptions.setPassword(apiKey.toCharArray());

        try {
            mqttAsyncClient.connect(connectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    if (successCallback != null) {
                        successCallback.onSuccess(clientId);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    if (failureCallback != null) {
                        failureCallback.onFailure(throwable);
                    }
                }
            });
        } catch (MqttException e) {
            if (failureCallback != null) {
                failureCallback.onFailure(e);
            }
        }
    }

    /**
     * Disconnects from the Mqtt broker asynchronously
     * @param successCallback Callback when the disconnection succeeds
     * @param failureCallback Callback when the disconnection fails
     */
    public void disconnect(SuccessCallback<String> successCallback, FailureCallback failureCallback) {
        try {
            mqttAsyncClient.disconnect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    if (successCallback != null) {
                        successCallback.onSuccess(clientId);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    if (failureCallback != null) {
                        failureCallback.onFailure(throwable);
                    }
                }
            });
        } catch (MqttException e) {
            if (failureCallback != null) {
                failureCallback.onFailure(e);
            }
        }
    }

    /**
     * Subscribes to a topic to retrieve messages for all devices
     * @param successCallback Callback when the subscription succeeds
     * @param failureCallback Callback when the subscription fails
     */
    public void subscribe(SuccessCallback<String> successCallback, FailureCallback failureCallback) {
        try {
            mqttAsyncClient.subscribe(topicPath, subscribeQOS, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    if (successCallback != null) {
                        successCallback.onSuccess(clientId);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    if (failureCallback != null) {
                        failureCallback.onFailure(throwable);
                    }
                }
            });
        } catch (MqttException e) {
            if (failureCallback != null) {
                failureCallback.onFailure(e);
            }
        }
    }

    /**
     * Unsubscribes from a topic for all devices
     * @param successCallback Callback when the unsubscription succeeds
     * @param failureCallback Callback when the unsubscription fails
     */
    public void unsubscribe(SuccessCallback<String> successCallback, FailureCallback failureCallback) {
        try {
            mqttAsyncClient.unsubscribe(topicPath, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    if (successCallback != null) {
                        successCallback.onSuccess(clientId);
                    }
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    if (failureCallback != null) {
                        failureCallback.onFailure(throwable);
                    }
                }
            });
        } catch (MqttException e) {
            if (failureCallback != null) {
                failureCallback.onFailure(e);
            }
        }
    }

    /**
     * Check mandatory properties to use the class properly
     * @throws ConfigurationException A configuration problem occurred
     */
    private void checkOpenLpwaMqttProviderInitialization() throws ConfigurationException {
        if (serverUri == null || serverUri.isEmpty()) {
            throw new ConfigurationException("OpenLpwa provider MQTT Uri is missing.");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new ConfigurationException("OpenLpwa provider API key us missing");
        }
    }

    // MqttCallback implementation

    @Override
    public void connectionLost(Throwable throwable) {
        if (clientCallback != null) {
            clientCallback.connectionLost(throwable);
        }
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        if (clientCallback != null) {
            // Deserialize incoming message
            DeviceIncomingMessage incomingMessage;
            String deviceID = null;
            try {
                incomingMessage = new ObjectMapper().readValue(mqttMessage.getPayload(), DeviceIncomingMessage.class);
                if (incomingMessage != null) {
                    // Retrieve the deviceID from the source metadata
                    String source = incomingMessage.getMetadata().getSource();
                    if (source != null) {
                        deviceID = source;
                    }
                }

                clientCallback.newMessageArrived(deviceID, incomingMessage);
            } catch (Throwable e) {
                logger.error("Unhandled exception while reading message.", e);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        // Publish not implemented so deliveryComplete never called
    }
}
