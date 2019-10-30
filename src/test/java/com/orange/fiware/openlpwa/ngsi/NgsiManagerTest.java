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
 * * Created by Christophe AZEMAR on 05/07/2016.
 */

package com.orange.fiware.openlpwa.ngsi;

import com.orange.fiware.openlpwa.domain.DeviceEntity;
import com.orange.fiware.openlpwa.exception.AgentException;
import com.orange.fiware.openlpwa.iotagent.Agent;
import com.orange.fiware.openlpwa.iotagent.Device;
import com.orange.fiware.openlpwa.repository.DeviceEntityRepository;
import com.orange.ngsi.client.NgsiClient;
import com.orange.ngsi.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * NgsiManger unit tests
 */
public class NgsiManagerTest {

    @InjectMocks
    private NgsiManager ngsiManager;
    @Mock
    private Agent agent;
    @Mock
    private NgsiClient ngsiClient;
    @Mock
    private DeviceEntityRepository deviceRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSubscribeToCommands() throws AgentException {
        String eui = "123";
        String unknown = "unknown";
        DeviceEntity registeredDevice = new DeviceEntity();
        registeredDevice.setDeviceID(eui);
        registeredDevice.setType("type");
        registeredDevice.setPort(1);
        registeredDevice.setName("name");
        registeredDevice.setSubscriptionId("123");
        Device device = new Device();
        device.setDeviceID(eui);
        device.setEntityType("type");
        device.setEntityName("name");
        device.setPort(1);
        List<String> commands = new ArrayList<>();
        commands.add("test");
        commands.add("test2");
        device.setCommands(commands);
        Device unknownDevice = new Device();
        unknownDevice.setDeviceID(unknown);
        unknownDevice.setCommands(commands);
        when(deviceRepository.findOne(eui)).thenReturn(registeredDevice);
        when(deviceRepository.findOne(unknown)).thenReturn(null);
        ReflectionTestUtils.setField(ngsiManager, "contextBrokerLocalUrl", "http://local");
        ngsiManager.subscribeToCommands(unknownDevice);
        ngsiManager.subscribeToCommands(device);

        ArgumentCaptor<SubscribeContext> captor = ArgumentCaptor.forClass(SubscribeContext.class);
        verify(ngsiClient, times(1)).unsubscribeContext(anyString(), any(HttpHeaders.class), eq("123"));
        verify(ngsiClient, times(2)).subscribeContext(anyString(), any(HttpHeaders.class), captor.capture());
        SubscribeContext subscribe = captor.getValue();
        assertEquals("P1M", subscribe.getDuration());
        assertEquals("name", subscribe.getEntityIdList().get(0).getId());
        assertEquals("type", subscribe.getEntityIdList().get(0).getType());
        assertEquals(2, subscribe.getAttributeList().size());
        assertTrue(subscribe.getAttributeList().contains("test_command"));
        assertTrue(subscribe.getAttributeList().contains("test2_command"));
        assertEquals(2, subscribe.getNotifyConditionList().get(0).getCondValues().size());
        assertTrue(subscribe.getNotifyConditionList().get(0).getCondValues().contains("test_command"));
        assertTrue(subscribe.getNotifyConditionList().get(0).getCondValues().contains("test2_command"));
        assertEquals(NotifyConditionEnum.ONCHANGE, subscribe.getNotifyConditionList().get(0).getType());
        assertEquals("http://local/v1/notifyContext/" + eui, subscribe.getReference().toString());
    }

    @Test(expected = AgentException.class)
    public void testSubscribeToCommandsWithDeviceNull() throws AgentException {
        ngsiManager.subscribeToCommands(null);
    }

    @Test(expected = AgentException.class)
    public void testSubscribeToCommandsWithoutDeviceCommands() throws AgentException {
        Device device = new Device();
        device.setDeviceID("123");
        device.setEntityType("type");
        device.setEntityName("name");
        device.setPort(1);
        ngsiManager.subscribeToCommands(device);
    }

    @Test(expected = AgentException.class)
    public void testUpdateSubscription() throws AgentException {
        ngsiManager.updateSubscription(null);
        ngsiManager.updateSubscription("123");
        ArgumentCaptor<UpdateContextSubscription> captor = ArgumentCaptor.forClass(UpdateContextSubscription.class);
        verify(ngsiClient, times(1)).updateContextSubscription(anyString(), any(HttpHeaders.class), captor.capture());
        assertEquals("P1M", captor.getValue().getDuration());
        assertEquals("123", captor.getValue().getSubscriptionId());
    }

    @Test
    public void testUnsubscribe() throws AgentException {
        ngsiManager.unsubscribe("123");
        verify(ngsiClient, times(1)).unsubscribeContext(anyString(), any(HttpHeaders.class), anyString());
    }

    @Test(expected = AgentException.class)
    public void testUnsubscribeWithSubscriptionIdNull() throws AgentException {
        ngsiManager.unsubscribe(null);
    }

    @Test
    public void testUpdateDeviceAttributesWithEntityId() throws AgentException {
        List<ContextAttribute> list = new ArrayList<>();
        ContextAttribute attribute = new ContextAttribute();
        attribute.setName("attribute");
        attribute.setType("type");
        attribute.setValue("value");
        list.add(attribute);
        EntityId id = new EntityId();
        id.setType("idType");
        id.setId("id");
        id.setIsPattern(true);
        ngsiManager.updateDeviceAttributes(id, list);
        ArgumentCaptor<UpdateContext> captor = ArgumentCaptor.forClass(UpdateContext.class);
        verify(ngsiClient, times(1)).updateContext(anyString(), any(HttpHeaders.class), captor.capture());
        assertEquals(list, captor.getValue().getContextElements().get(0).getContextAttributeList());
        assertEquals(UpdateAction.APPEND, captor.getValue().getUpdateAction());
    }

    @Test(expected = AgentException.class)
    public void testUpdateDeviceAttributesWithEntityIdNull() throws AgentException {
        EntityId idNull = null;
        ngsiManager.updateDeviceAttributes(idNull, new ArrayList<>());
    }

    @Test(expected = AgentException.class)
    public void testUpdateDeviceAttributesWithEntityIdNullList() throws AgentException {
        EntityId id = new EntityId();
        id.setType("idType");
        id.setId("id");
        id.setIsPattern(true);
        ngsiManager.updateDeviceAttributes(id, null);
    }

    @Test(expected = AgentException.class)
    public void testUpdateDeviceAttributesWithEntityIdEmptyList() throws AgentException {
        EntityId id = new EntityId();
        id.setType("idType");
        id.setId("id");
        id.setIsPattern(true);
        ngsiManager.updateDeviceAttributes(id, new ArrayList<>());
    }

    @Test
    public void testUpdateDeviceAttributesWithDevice() throws AgentException {
        List<ContextAttribute> list = new ArrayList<>();
        ContextAttribute attribute = new ContextAttribute();
        attribute.setName("attribute");
        attribute.setType("type");
        attribute.setValue("value");
        list.add(attribute);
        DeviceEntity device = new DeviceEntity();
        device.setDeviceID("1234");
        device.setType("type");
        device.setName("name");
        device.setPort(1);
        device.setCommands(new ArrayList<>());
        ngsiManager.updateDeviceAttributes(device, list);
        ArgumentCaptor<UpdateContext> captor = ArgumentCaptor.forClass(UpdateContext.class);
        verify(ngsiClient, times(1)).updateContext(anyString(), any(HttpHeaders.class), captor.capture());
        assertEquals(list, captor.getValue().getContextElements().get(0).getContextAttributeList());
        assertEquals(UpdateAction.APPEND, captor.getValue().getUpdateAction());
        assertEquals("name", captor.getValue().getContextElements().get(0).getEntityId().getId());
        assertEquals("type", captor.getValue().getContextElements().get(0).getEntityId().getType());
        assertFalse(captor.getValue().getContextElements().get(0).getEntityId().getIsPattern());
    }

    @Test(expected = AgentException.class)
    public void testUpdateDeviceAttributesWithDeviceNull() throws AgentException {
        DeviceEntity deviceNull = null;
        ngsiManager.updateDeviceAttributes(deviceNull, new ArrayList<>());
    }

    @Test(expected = AgentException.class)
    public void testUpdateDeviceAttributesWithDeviceNullList() throws AgentException {
        DeviceEntity device = new DeviceEntity();
        device.setDeviceID("1234");
        ngsiManager.updateDeviceAttributes(device, null);
    }

    @Test(expected = AgentException.class)
    public void testUpdateDeviceAttributesWithDeviceEmptyList() throws AgentException {
        DeviceEntity device = new DeviceEntity();
        device.setDeviceID("1234");
        ngsiManager.updateDeviceAttributes(device, new ArrayList<>());
    }

    @Test
    public void testRemoteHeaders() throws AgentException {
        ngsiManager.updateSubscription("test");
        ReflectionTestUtils.setField(ngsiManager, "contextBrokerRemoteAuthToken", "");
        ReflectionTestUtils.setField(ngsiManager, "contextBrokerRemoteFiwareService", "");
        ReflectionTestUtils.setField(ngsiManager, "contextBrokerRemoteFiwareServicePath", "");
        ngsiManager.updateSubscription("test");
        ReflectionTestUtils.setField(ngsiManager, "contextBrokerRemoteAuthToken", "RemoteAuthToken");
        ReflectionTestUtils.setField(ngsiManager, "contextBrokerRemoteFiwareService", "Service");
        ReflectionTestUtils.setField(ngsiManager, "contextBrokerRemoteFiwareServicePath", "ServicePath");
        ngsiManager.updateSubscription("test");

        ArgumentCaptor<HttpHeaders> captor = ArgumentCaptor.forClass(HttpHeaders.class);
        verify(ngsiClient, times(3)).updateContextSubscription(anyString(), captor.capture(), any(UpdateContextSubscription.class));
        assertNull(captor.getAllValues().get(0).get("X-Auth-Token"));
        assertNull(captor.getAllValues().get(0).get("Fiware-Service"));
        assertNull(captor.getAllValues().get(0).get("Fiware-ServicePath"));
        assertEquals("application/json", captor.getAllValues().get(0).get("Accept").get(0));
        assertEquals("application/json", captor.getAllValues().get(0).get("Content-Type").get(0));

        assertEquals("", captor.getAllValues().get(1).get("X-Auth-Token").get(0));
        assertNull(captor.getAllValues().get(1).get("Fiware-Service"));
        assertNull(captor.getAllValues().get(1).get("Fiware-ServicePath"));
        assertEquals("application/json", captor.getAllValues().get(1).get("Accept").get(0));
        assertEquals("application/json", captor.getAllValues().get(1).get("Content-Type").get(0));

        assertEquals("RemoteAuthToken", captor.getAllValues().get(2).get("X-Auth-Token").get(0));
        assertEquals("Service", captor.getAllValues().get(2).get("Fiware-Service").get(0));
        assertEquals("ServicePath", captor.getAllValues().get(2).get("Fiware-ServicePath").get(0));
        assertEquals("application/json", captor.getAllValues().get(2).get("Accept").get(0));
        assertEquals("application/json", captor.getAllValues().get(2).get("Content-Type").get(0));
    }
}
