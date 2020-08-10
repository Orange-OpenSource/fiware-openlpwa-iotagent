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

import com.orange.fiware.openlpwa.exception.ConfigurationException;
import com.orange.fiware.openlpwa.provider.model.DeviceIncomingMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.eclipse.paho.client.mqttv3.MqttException.REASON_CODE_CLIENT_TIMEOUT;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEmptyString.isEmptyOrNullString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * OpenLpwaMqttProvider unit tests
 */
public class OpenLpwaMqttProviderTest {

    private static final String serverUri = "tcp://localhost:1883";
    private static final String clientId = "testClientId";
    private static final String apiKey = "TESTOpenLpwaProviderMQTT";
    private final static String deviceEUI = "testMQTTdevice";
    private final static String topicPath = "fifo/fiware_orange";
    private OpenLpwaMqttProvider mqttClient;
    @Mock
    private MqttAsyncClient mockMqttAsyncClient;
    @Rule
    public TestName testName = new TestName();
    @Mock
    OpenLpwaMqttProviderCallback clientCallback;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        String methodName = testName.getMethodName();
        List<String> testsWithoutInitialization = Arrays.asList("testMqttWithoutApiKeyParameter",
                "testMqttWithoutApiKeyParameter",
                "testMqttWithoutClientIdParameter");
        if (!testsWithoutInitialization.stream().anyMatch(s -> s.equals(methodName))) {
            mqttClient = new OpenLpwaMqttProvider(serverUri, clientId, apiKey, topicPath, clientCallback);
            ReflectionTestUtils.setField(mqttClient, "mqttAsyncClient", mockMqttAsyncClient);
        }
    }

    @After
    public void teardown() {
        reset(mockMqttAsyncClient);
    }

    @Test(expected = ConfigurationException.class)
    public void testMqttWithoutServerUriParameter() throws Exception {
        mqttClient = new OpenLpwaMqttProvider(null, clientId, apiKey, topicPath, null);
    }

    @Test(expected = ConfigurationException.class)
    public void testMqttWithoutApiKeyParameter() throws Exception {
        mqttClient = new OpenLpwaMqttProvider(serverUri, clientId, null, topicPath, null);
    }

    @Test
    public void testMqttWithoutClientIdParameter() throws Exception {
        mqttClient = new OpenLpwaMqttProvider(serverUri, null, apiKey, topicPath, null);
        assertThat(mqttClient.getClientId(), not(isEmptyOrNullString()));
    }

    @Test
    public void testMqttConnectWithSuccess() throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[2];
                originalArgument.onSuccess(null);
                return null;
            }
        }).when(mockMqttAsyncClient).connect(any(MqttConnectOptions.class), anyObject(), any(IMqttActionListener.class));

        mqttClient.connect(connectedClientId -> assertEquals(clientId, connectedClientId),
                exception -> fail("Failure callback unexpected call"));
    }

    @Test
    public void testMqttConnectWithError() throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[2];
                originalArgument.onFailure(null, new Exception());
                return null;
            }
        }).when(mockMqttAsyncClient).connect(any(MqttConnectOptions.class), anyObject(), any(IMqttActionListener.class));

        mqttClient.connect(clientId -> fail("Success callback unexpected call"),
                exception -> assertNotNull("exception is null", exception));
    }

    @Test
    public void testMqttConnectWithException() throws MqttException {
        doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_EXCEPTION))
                .when(mockMqttAsyncClient).connect(any(MqttConnectOptions.class), anyObject(), any(IMqttActionListener.class));

        mqttClient.connect(clientId -> fail("Success callback unexpected call"),
                exception -> {
                    assertNotNull("exception is null", exception);
                    assertThat(exception, CoreMatchers.instanceOf(MqttException.class));
                });
    }

    @Test
    public void testMqttDisconnectWithSuccess() throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[1];
                originalArgument.onSuccess(null);
                return null;
            }
        }).when(mockMqttAsyncClient).disconnect(anyObject(), any(IMqttActionListener.class));

        mqttClient.disconnect(connectedClientId -> assertEquals(clientId, connectedClientId),
                exception -> fail("Failure callback unexpected call"));
    }

    @Test
    public void testMqttDisconnectWithError() throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[1];
                originalArgument.onFailure(null, new Exception());
                return null;
            }
        }).when(mockMqttAsyncClient).disconnect(anyObject(), any(IMqttActionListener.class));

        mqttClient.disconnect(clientId -> fail("Success callback unexpected call"),
                exception -> assertNotNull("exception is null", exception));
    }

    @Test
    public void testMqttDisconnectWithException() throws MqttException {
        doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_EXCEPTION))
                .when(mockMqttAsyncClient).disconnect(anyObject(), any(IMqttActionListener.class));

        mqttClient.disconnect(clientId -> fail("Success callback unexpected call"),
                exception -> {
                    assertNotNull("exception is null", exception);
                    assertThat(exception, CoreMatchers.instanceOf(MqttException.class));
                });
    }

    @Test
    public void testMqttSubscribeWithSuccess() throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[3];
                originalArgument.onSuccess(null);
                return null;
            }
        }).when(mockMqttAsyncClient).subscribe(anyString(), anyInt(), anyObject(), any(IMqttActionListener.class));

        mqttClient.subscribe(subscribedClientId -> assertEquals(clientId, subscribedClientId),
                exception -> fail("Failure callback unexpected call"));
    }

    @Test
    public void testMqttSubscribeWithError() throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[3];
                originalArgument.onFailure(null, new Exception());
                return null;
            }
        }).when(mockMqttAsyncClient).subscribe(anyString(), anyInt(), anyObject(), any(IMqttActionListener.class));

        mqttClient.subscribe(subscribedClientId -> fail("Success callback unexpected call"),
                exception -> assertNotNull("exception is null", exception));
    }

    @Test
    public void testMqttSubscribeWithException() throws Exception {
        doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_EXCEPTION))
                .when(mockMqttAsyncClient).subscribe(anyString(), anyInt(), anyObject(), any(IMqttActionListener.class));

        mqttClient.subscribe(subscribedClientId -> fail("Success callback unexpected call"),
                exception -> {
                    assertNotNull("exception is null", exception);
                    assertThat(exception, CoreMatchers.instanceOf(MqttException.class));
                });
    }

    @Test
    public void testMqttUnsubscribeWithSuccess() throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[2];
                originalArgument.onSuccess(null);
                return null;
            }
        }).when(mockMqttAsyncClient).unsubscribe(anyString(), anyObject(), any(IMqttActionListener.class));

        mqttClient.unsubscribe(unsubscribedClientId -> assertEquals(clientId, unsubscribedClientId),
                exception -> fail("Failure callback unexpected call"));
    }

    @Test
    public void testMqttUnsubscribeWithError() throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[2];
                originalArgument.onFailure(null, new Exception());
                return null;
            }
        }).when(mockMqttAsyncClient).unsubscribe(anyString(), anyObject(), any(IMqttActionListener.class));

        mqttClient.unsubscribe(unsubscribedClientId -> fail("Success callback unexpected call"),
                exception -> assertNotNull("exception is null", exception));
    }

    @Test
    public void testMqttUnsubscribeWithException() throws Exception {
        doThrow(new MqttException(MqttException.REASON_CODE_CLIENT_EXCEPTION))
                .when(mockMqttAsyncClient).unsubscribe(anyString(), anyObject(), any(IMqttActionListener.class));

        mqttClient.unsubscribe(unsubscribedClientId -> fail("Success callback unexpected call"),
                exception -> {
                    assertNotNull("exception is null", exception);
                    assertThat(exception, CoreMatchers.instanceOf(MqttException.class));
                });
    }

    @Test
    public void testMqttConnectionLost() {
        Exception exception = new MqttException(REASON_CODE_CLIENT_TIMEOUT);
        mqttClient.connectionLost(exception);
        verify(clientCallback).connectionLost(exception);
    }

    @Test
    public void testMqttConnectionLostWithoutCallback() {
        mqttClient.setClientCallback(null);

        Exception exception = new MqttException(REASON_CODE_CLIENT_TIMEOUT);
        mqttClient.connectionLost(exception);
        verify(clientCallback, never()).connectionLost(exception);
    }

    @Test
    public void testMqttNewMessageArrived() throws Exception {
        String jsonPayload = "{\"streamId\":\"testMQTTdevice!uplink\",\"timestamp\":\"2016-05-23T13:05:18.307Z\",\"model\":\"lora_v0\",\"value\":{\"port\":1,\"fcnt\":8,\"signalLevel\":2,\"payload\":\"ae2109000cf3\"},\"tags\":[\"Lyon\",\"Test\"],\"metadata\":{\"source\":\"testMQTTdevice\"}}";

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[3];
                originalArgument.onSuccess(null);
                return null;
            }
        }).when(mockMqttAsyncClient).subscribe(anyString(), anyInt(), anyObject(), any(IMqttActionListener.class));

        mqttClient.subscribe(subscribedClientId -> assertEquals(clientId, subscribedClientId),
                exception -> fail("Failure callback unexpected call"));

        mqttClient.messageArrived(topicPath, new MqttMessage(jsonPayload.getBytes()));
        verify(clientCallback).newMessageArrived(eq(deviceEUI), argThat(o -> {
            if (o instanceof DeviceIncomingMessage) {
                DeviceIncomingMessage incomingMessage = o;
                return incomingMessage.getStreamId() != null &&
                        incomingMessage.getDate() != null &&
                        incomingMessage.getModel() != null &&
                        incomingMessage.getValue() != null &&
                        incomingMessage.getTags().size() == 2;
            }
            return true;
        }));
    }


    @Test
    public void testMqttNewMessageArrivedWithoutCallback() throws Exception {
        mqttClient.setClientCallback(null);

        String jsonPayload = "{\"streamId\":\"testMQTTdevice!uplink\",\"timestamp\":\"2016-05-23T13:05:18.307Z\",\"model\":\"lora_v0\",\"value\":{\"port\":1,\"fcnt\":8,\"signalLevel\":2,\"payload\":\"ae2109000cf3\"},\"tags\":[\"Lyon\",\"Test\"],\"metadata\":{\"source\":\"testMQTTdevice\"}}";

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[3];
                originalArgument.onSuccess(null);
                return null;
            }
        }).when(mockMqttAsyncClient).subscribe(anyString(), anyInt(), anyObject(), any(IMqttActionListener.class));

        mqttClient.subscribe(subscribedClientId -> assertEquals(clientId, subscribedClientId),
                exception -> fail("Failure callback unexpected call"));

        mqttClient.messageArrived(topicPath, new MqttMessage(jsonPayload.getBytes()));
        verify(clientCallback, never()).newMessageArrived(anyString(), any(DeviceIncomingMessage.class));
    }

    @Test
    public void testMqttNewMessageArrivedWithInvalidPayload() throws Exception {
        String jsonPayload = "{\"badf00d\":\"yes\"}";

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                IMqttActionListener originalArgument = (IMqttActionListener) (invocationOnMock.getArguments())[3];
                originalArgument.onSuccess(null);
                return null;
            }
        }).when(mockMqttAsyncClient).subscribe(anyString(), anyInt(), anyObject(), any(IMqttActionListener.class));

        mqttClient.subscribe(subscribedClientId -> assertEquals(clientId, subscribedClientId),
                exception -> fail("Failure callback unexpected call"));

        mqttClient.messageArrived("router/~event/v1/data/new/urn/lora/#", new MqttMessage(jsonPayload.getBytes()));
        verify(clientCallback, never()).newMessageArrived(anyString(), any(DeviceIncomingMessage.class));
    }
}