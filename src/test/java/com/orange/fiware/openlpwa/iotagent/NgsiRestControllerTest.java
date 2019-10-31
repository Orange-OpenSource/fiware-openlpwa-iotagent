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

package com.orange.fiware.openlpwa.iotagent;

import com.orange.fiware.openlpwa.domain.DeviceEntity;
import com.orange.fiware.openlpwa.exception.AgentException;
import com.orange.fiware.openlpwa.ngsi.NgsiManager;
import com.orange.fiware.openlpwa.repository.DeviceEntityRepository;
import com.orange.ngsi.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * NgsiRestController unit tests
 */
public class NgsiRestControllerTest {

    @InjectMocks
    private NgsiRestController ngsiRestController;
    @Mock
    private NgsiManager manager;
    @Mock
    private Agent agent;
    @Mock
    private DeviceEntityRepository deviceRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testNotifyContextRequest() throws Exception {
        String eui = "123";
        String unknown = "unknown";
        DeviceEntity device = new DeviceEntity();
        device.setDeviceID(eui);
        device.setType("type");
        device.setPort(1);
        device.setName("name");
        device.setSubscriptionId("123");
        when(deviceRepository.findOne(eui)).thenReturn(device);
        when(deviceRepository.findOne(unknown)).thenReturn(null);
        HttpServletRequest request = mock(HttpServletRequest.class);
        List<ContextElementResponse> contextElementResponseList = new ArrayList<>();
        NotifyContext emptyContext = new NotifyContext();
        NotifyContext context = new NotifyContext();
        context.setContextElementResponseList(contextElementResponseList);
        context.setSubscriptionId("123");
        NotifyContext context2 = new NotifyContext();
        context2.setContextElementResponseList(contextElementResponseList);
        context2.setSubscriptionId("1234");
        NotifyContext unknownSubscriptionId = new NotifyContext();
        unknownSubscriptionId.setContextElementResponseList(contextElementResponseList);
        unknownSubscriptionId.setSubscriptionId("unknown");

        ResponseEntity<NotifyContextResponse> nullContextResponse = ngsiRestController.notifyContextRequest(null, eui, request);
        ResponseEntity<NotifyContextResponse> emptyContextResponse = ngsiRestController.notifyContextRequest(emptyContext, eui, request);
        ResponseEntity<NotifyContextResponse> unknownEUI = ngsiRestController.notifyContextRequest(unknownSubscriptionId, unknown, request);
        ResponseEntity<NotifyContextResponse> oldDevice = ngsiRestController.notifyContextRequest(context2, eui, request);
        ResponseEntity<NotifyContextResponse> rightResponse = ngsiRestController.notifyContextRequest(context, eui, request);

        assertEquals(HttpStatus.OK, nullContextResponse.getStatusCode());
        assertEquals(HttpStatus.OK, emptyContextResponse.getStatusCode());
        assertEquals(HttpStatus.OK, unknownEUI.getStatusCode());
        assertEquals(HttpStatus.OK, oldDevice.getStatusCode());
        assertEquals(HttpStatus.OK, rightResponse.getStatusCode());

        verify(manager, times(1)).unsubscribe("1234");
        verify(manager, times(1)).unsubscribe("unknown");
        verify(manager, times(2)).unsubscribe(anyString());
    }

    @Test
    public void testHandleNotifyContext() throws AgentException {
        String eui = "123";
        ContextElementResponse response = new ContextElementResponse();
        response.setStatusCode(new StatusCode(CodeEnum.CODE_200));
        ContextElement element = new ContextElement();
        element.setEntityId(new EntityId("name", "type", false));
        List<ContextAttribute> attributeList = new ArrayList<>();
        ContextAttribute attribute1 = new ContextAttribute();
        attribute1.setName("test_command");
        attribute1.setType("command");
        attribute1.setValue("value");
        ContextAttribute attribute2 = new ContextAttribute();
        attribute2.setName("test2_command");
        attribute2.setType("command");
        attribute2.setValue("value2");
        attributeList.add(attribute1);
        attributeList.add(attribute2);
        element.setContextAttributeList(attributeList);
        response.setContextElement(element);
        doAnswer(invocation -> {
            BiConsumer<Boolean, Date> callback = invocation.getArgumentAt(3, BiConsumer.class);
            callback.accept(true, new Date());
            return null;
        }).when(agent).executeCommand(anyString(), anyString(), any(ContextAttribute.class), any(BiConsumer.class));
        ReflectionTestUtils.invokeMethod(ngsiRestController, "handleNotifyContext", eui, response);
        verify(agent, times(2)).executeCommand(anyString(), anyString(), any(ContextAttribute.class), any(BiConsumer.class));
        verify(agent, times(1)).executeCommand(eq(eui), eq("test"), any(ContextAttribute.class), any(BiConsumer.class));
        verify(manager, times(1)).updateDeviceAttributes(any(EntityId.class), any(List.class));
    }
}
