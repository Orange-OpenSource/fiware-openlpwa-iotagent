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
 * * Created by Christophe AZEMAR on 28/09/2016.
 */

package com.orange.fiware.openlpwa.iotagent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * AgentMqttProviderCallback unit tests
 */
public class AgentMqttClientCallbackTest {

    private Agent.AgentMqttProviderCallback callback;
    @Mock
    private Agent agent;
    @Mock
    private AgentConnectionLostCallback lostCallback;
    @Mock
    private OpenLpwaNgsiConverter converter;

    @Before
    public void setup() {
        initMocks(this);
        callback = agent.new AgentMqttProviderCallback();
        ReflectionTestUtils.setField(callback, "reconnectingDelay", 10);
        ReflectionTestUtils.setField(agent, "connectionLostCallback", lostCallback);
        ReflectionTestUtils.setField(agent, "converter", converter);
    }

    @Test
    public void testConnectionLostOk() {
        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
        ArgumentCaptor<AgentSuccessCallback> successCallback = ArgumentCaptor.forClass(AgentSuccessCallback.class);
        callback.connectionLost(new Exception());
        assertTrue((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
        verify(agent, times(1)).start(any(OpenLpwaNgsiConverter.class), successCallback.capture(), any(AgentFailureCallback.class));
        successCallback.getValue().onSuccess();
        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
    }

    @Test
    public void testConnectionLostStartFailed() {
        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
        ArgumentCaptor<AgentFailureCallback> failure = ArgumentCaptor.forClass(AgentFailureCallback.class);
        callback.connectionLost(new Exception());
        assertTrue((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
        verify(agent, times(1)).start(any(OpenLpwaNgsiConverter.class), any(AgentSuccessCallback.class), failure.capture());
        failure.getValue().onFailure(new Exception());
        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
        verify(lostCallback, times(1)).onConnectionLost();
    }

    @Test
    public void testConnectionLostAlreadyConnecting() {
        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
        callback.connectionLost(new Exception());
        assertTrue((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
        callback.connectionLost(new Exception());
        verify(agent, times(1)).start(any(OpenLpwaNgsiConverter.class), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
    }
}
