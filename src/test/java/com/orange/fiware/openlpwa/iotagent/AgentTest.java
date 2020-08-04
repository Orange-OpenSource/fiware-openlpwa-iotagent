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
 * * Created by François SUC on 14/09/2016.
 */

package com.orange.fiware.openlpwa.iotagent;

import com.orange.fiware.openlpwa.exception.AgentException;
import com.orange.fiware.openlpwa.exception.ConfigurationException;
import com.orange.fiware.openlpwa.provider.OpenLpwaMqttProvider;
import com.orange.fiware.openlpwa.provider.OpenLpwaProvider;
import com.orange.fiware.openlpwa.provider.model.DeviceCommand;
import com.orange.fiware.openlpwa.provider.model.DeviceInfo;
import com.orange.fiware.openlpwa.provider.model.RegisterDeviceCommandParameter;
import com.orange.ngsi.model.*;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.orange.ngsi.model.CodeEnum.CODE_200;
import static com.orange.ngsi.model.CodeEnum.CODE_500;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Agent unit tests
 */
public class AgentTest {

    private final static String subscriptionId = "abcdef1234567890";
    private final static String commandName = "testCommand";
    @InjectMocks
    Agent agent;
    @Mock
    private OpenLpwaProvider mockLpwaProvider;
    @Mock
    private OpenLpwaMqttProvider mockOpenLpwaMqttProvider;
    @Mock
    private SuccessCallback<Boolean> resultCallback;
    private Device device;
    private ContextAttribute commandAttribute;

    @Before
    public void setup() {
        initMocks(this);
        device = new Device();
        device.setDeviceID("testDevEUI");
        device.setPort(2);
        device.setEntityName("testEntityName");
        device.setEntityType("testEntityType");
        List<String> commands = new ArrayList<>();
        commands.add("testCommand");
        device.setCommands(commands);

        commandAttribute = new ContextAttribute();
        commandAttribute.setName("testCommand_command");
        commandAttribute.setType("command");
        commandAttribute.setValue("value");
    }

    @After
    public void teardown() {
        reset(mockLpwaProvider, mockOpenLpwaMqttProvider, resultCallback);
    }

    @Test
    public void testStartAgentWithoutConverter() {
        simulateMqttConnectionSuccess();
        simulateMqttSubscriptionSuccess();

        agent.start(null,
                () -> fail("Success callback unexpected call"),
                exception -> {
                    assertNotNull("exception is null", exception);
                    assertThat(exception, CoreMatchers.instanceOf(AgentException.class));
                    resultCallback.onSuccess(false);
                }
        );

        verify(resultCallback).onSuccess(false);
    }

    @Test
    public void testStartAgentWithSuccess() {
        simulateMqttConnectionSuccess();
        simulateMqttSubscriptionSuccess();

        agent.start(new OpenLpwaNgsiConverterTest(false),
                () -> resultCallback.onSuccess(true),
                exception -> fail("Failed callback unexpected call")
        );

        verify(resultCallback).onSuccess(true);
    }

    @Test
    public void testStartAgentWithConnectionFailure() {
        simulateMqttConnectionFailure();
        simulateMqttSubscriptionSuccess();

        agent.start(new OpenLpwaNgsiConverterTest(false),
                () -> fail("Success callback unexpected call"),
                exception -> {
                    assertNotNull("exception is null", exception);
                    resultCallback.onSuccess(false);
                }
        );

        verify(resultCallback).onSuccess(false);
    }

    @Test
    public void testStartAgentWithSubscriptionFailure() {
        simulateMqttConnectionSuccess();
        simulateMqttSubscriptionFailure();

        agent.start(new OpenLpwaNgsiConverterTest(false),
                () -> fail("Success callback unexpected call"),
                exception -> {
                    assertNotNull("exception is null", exception);
                    resultCallback.onSuccess(false);
                }
        );

        verify(resultCallback).onSuccess(false);
    }

    @Test
    public void testStopAgentWithSuccess() {
        simulateMqttDisconnectionSuccess();

        agent.stop(
                () -> resultCallback.onSuccess(true),
                exception -> fail("Failed callback unexpected call")
        );

        verify(resultCallback).onSuccess(true);
    }

    @Test
    public void testStopAgentWithDisconnectionFailure() {
        simulateMqttDisconnectionFailure();

        agent.stop(
                () -> fail("Success callback unexpected call"),
                exception -> {
                    assertNotNull("exception is null", exception);
                    resultCallback.onSuccess(false);
                }
        );

        verify(resultCallback).onSuccess(false);
    }

    private void simulateMqttConnectionSuccess() {
        doAnswer(invocationOnMock -> {
            SuccessCallback<String> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
            originalArgument.onSuccess("connectedClientId");
            return null;
        }).when(mockOpenLpwaMqttProvider).connect(Matchers.any(), any(FailureCallback.class));
    }

    private void simulateMqttConnectionFailure() {
        doAnswer(invocationOnMock -> {
            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
            originalArgument.onFailure(new Exception("simulateMqttConnectionFailure"));
            return null;
        }).when(mockOpenLpwaMqttProvider).connect(Matchers.any(), any(FailureCallback.class));
    }

    private void simulateMqttDisconnectionSuccess() {
        doAnswer(invocationOnMock -> {
            SuccessCallback<String> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
            originalArgument.onSuccess("disconnectedClientId");
            return null;
        }).when(mockOpenLpwaMqttProvider).disconnect(Matchers.any(), any(FailureCallback.class));
    }

    private void simulateMqttDisconnectionFailure() {
        doAnswer(invocationOnMock -> {
            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
            originalArgument.onFailure(new Exception("simulateMqttDisconnectionFailure"));
            return null;
        }).when(mockOpenLpwaMqttProvider).disconnect(Matchers.any(), any(FailureCallback.class));
    }

    private void simulateMqttSubscriptionSuccess() {
        doAnswer(invocationOnMock -> {
            SuccessCallback<String> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
            originalArgument.onSuccess("subscribedClientId");
            return null;
        }).when(mockOpenLpwaMqttProvider).subscribe(Matchers.any(), any(FailureCallback.class));
    }

    private void simulateMqttSubscriptionFailure() {
        doAnswer(invocationOnMock -> {
            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
            originalArgument.onFailure(new Exception("simulateMqttSubscriptionFailure"));
            return null;
        }).when(mockOpenLpwaMqttProvider).subscribe(Matchers.any(), any(FailureCallback.class));
    }
}