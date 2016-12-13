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
 * * Created by Christophe AZEMAR on 26/09/2016.
 */

package com.orange.fiware.openlpwa.iotagent;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * AgentRestController unit tests
 */
public class AgentRestControllerTest {

    @InjectMocks
    private AgentRestController agentRestController;
    @Mock
    private Agent mockAgent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRegisterSuccess() {
        Device device = new Device();
        device.setDeviceEUI("123");
        device.setEntityType("type");
        device.setEntityName("name");
        device.setPort(1);

        simulateAgentRegisterSuccess();

        DeferredResult<ResponseEntity<?>> deferredResult = agentRestController.register(device);
        verify(mockAgent, times(1)).register(eq(device), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
        ResponseEntity<?> entity = (ResponseEntity<?>) deferredResult.getResult();
        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        assertEquals(String.format("/devices/%s", device.getDeviceEUI()), entity.getHeaders().get("Location").get(0));
    }

    @Test
    public void testRegisterFailure() {
        Device device = new Device();
        device.setDeviceEUI("123");
        device.setEntityType("type");
        device.setEntityName("name");
        device.setPort(1);

        simulateAgentRegisterFailure();

        DeferredResult<ResponseEntity<?>> deferredResult = agentRestController.register(device);
        verify(mockAgent, times(1)).register(eq(device), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
        ResponseEntity<?> entity = (ResponseEntity<?>) deferredResult.getResult();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
    }

    @Test
    public void testRegisterWithoutDevice() {
        simulateAgentRegisterSuccess();

        DeferredResult<ResponseEntity<?>> deferredResult = agentRestController.register(null);
        verify(mockAgent, times(1)).register(any(), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
        ResponseEntity<?> entity = (ResponseEntity<?>) deferredResult.getResult();
        assertEquals(HttpStatus.OK, entity.getStatusCode());
    }

    @Test
    public void testUnregisterSuccess() {
        String deviceEUI = "123";

        simulateAgentUnregisterSuccess();

        DeferredResult<ResponseEntity<?>> deferredResult = agentRestController.unregister(deviceEUI);
        verify(mockAgent, times(1)).unregister(anyString(), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
        ResponseEntity<?> entity = (ResponseEntity<?>) deferredResult.getResult();
        assertEquals(HttpStatus.NO_CONTENT, entity.getStatusCode());
    }

    @Test
    public void testUnregisterFailure() {
        String deviceEUI = "123";

        simulateAgentUnregisterFailure();

        DeferredResult<ResponseEntity<?>> deferredResult = agentRestController.unregister(deviceEUI);
        verify(mockAgent, times(1)).unregister(anyString(), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
        ResponseEntity<?> entity = (ResponseEntity<?>) deferredResult.getResult();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
    }

    private void simulateAgentRegisterSuccess() {
        doAnswer(invocationOnMock -> {
            AgentSuccessCallback originalArgument = invocationOnMock.getArgumentAt(1, AgentSuccessCallback.class);
            originalArgument.onSuccess();
            return null;
        }).when(mockAgent).register(any(Device.class), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
    }

    private void simulateAgentRegisterFailure() {
        doAnswer(invocationOnMock -> {
            AgentFailureCallback originalArgument = invocationOnMock.getArgumentAt(2, AgentFailureCallback.class);
            originalArgument.onFailure(new Exception());
            return null;
        }).when(mockAgent).register(any(Device.class), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
    }

    private void simulateAgentUnregisterSuccess() {
        doAnswer(invocationOnMock -> {
            AgentSuccessCallback originalArgument = invocationOnMock.getArgumentAt(1, AgentSuccessCallback.class);
            originalArgument.onSuccess();
            return null;
        }).when(mockAgent).unregister(anyString(), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
    }

    private void simulateAgentUnregisterFailure() {
        doAnswer(invocationOnMock -> {
            AgentFailureCallback originalArgument = invocationOnMock.getArgumentAt(2, AgentFailureCallback.class);
            originalArgument.onFailure(new Exception());
            return null;
        }).when(mockAgent).unregister(anyString(), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
    }
}
