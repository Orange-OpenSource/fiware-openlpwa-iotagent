///**
// * Copyright (C) 2016 Orange
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *          http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// * * Created by Christophe AZEMAR on 28/09/2016.
// */
//
//package com.orange.fiware.openlpwa.iotagent;
//
//import com.orange.fiware.openlpwa.domain.DeviceEntity;
//import com.orange.fiware.openlpwa.exception.AgentException;
//import com.orange.fiware.openlpwa.provider.model.DeviceIncomingMessage;
//import com.orange.fiware.openlpwa.ngsi.NgsiManager;
//import com.orange.fiware.openlpwa.repository.DeviceEntityRepository;
//import com.orange.ngsi.model.ContextAttribute;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Mockito.*;
//import static org.mockito.MockitoAnnotations.initMocks;
//
///**
// * AgentMqttProviderCallback unit tests
// */
//public class AgentMqttClientCallbackTest {
//
//    private Agent.AgentMqttProviderCallback callback;
//    @Mock
//    private Agent agent;
//    @Mock
//    private DeviceEntityRepository deviceRepository;
//    @Mock
//    private NgsiManager ngsiManager;
//    @Mock
//    private AgentConnectionLostCallback lostCallback;
//    @Mock
//    private OpenLpwaNgsiConverter converter;
//
//    @Before
//    public void setup() {
//        initMocks(this);
//        callback = agent.new AgentMqttProviderCallback();
//        ReflectionTestUtils.setField(agent, "deviceRepository", deviceRepository);
//        ReflectionTestUtils.setField(agent, "ngsiManager", ngsiManager);
//        ReflectionTestUtils.setField(callback, "reconnectingDelay", 10);
//        ReflectionTestUtils.setField(agent, "connectionLostCallback", lostCallback);
//        ReflectionTestUtils.setField(agent, "converter", converter);
//    }
//
//    @Test
//    public void testConnectionLostOk() {
//        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
//        ArgumentCaptor<AgentSuccessCallback> successCallback = ArgumentCaptor.forClass(AgentSuccessCallback.class);
//        callback.connectionLost(new Exception());
//        assertTrue((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
//        verify(agent, times(1)).start(any(OpenLpwaNgsiConverter.class), successCallback.capture(), any(AgentFailureCallback.class));
//        successCallback.getValue().onSuccess();
//        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
//    }
//
//    @Test
//    public void testConnectionLostStartFailed() {
//        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
//        ArgumentCaptor<AgentFailureCallback> failure = ArgumentCaptor.forClass(AgentFailureCallback.class);
//        callback.connectionLost(new Exception());
//        assertTrue((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
//        verify(agent, times(1)).start(any(OpenLpwaNgsiConverter.class), any(AgentSuccessCallback.class), failure.capture());
//        failure.getValue().onFailure(new Exception());
//        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
//        verify(lostCallback, times(1)).onConnectionLost();
//    }
//
//    @Test
//    public void testConnectionLostAlreadyConnecting() {
//        assertFalse((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
//        callback.connectionLost(new Exception());
//        assertTrue((boolean) ReflectionTestUtils.getField(agent, "reconnecting"));
//        callback.connectionLost(new Exception());
//        verify(agent, times(1)).start(any(OpenLpwaNgsiConverter.class), any(AgentSuccessCallback.class), any(AgentFailureCallback.class));
//    }
//
//    @Test
//    public void testNewMessageArrivedOk() throws AgentException {
//        String eui = "123";
//        DeviceEntity device = new DeviceEntity();
//        device.setDeviceID(eui);
//        device.setType("type");
//        device.setPort(1);
//        device.setName("name");
//        device.setSubscriptionId("123");
//        when(deviceRepository.findById(eui)).thenReturn(java.util.Optional.of(device));
//        DeviceIncomingMessage message = new DeviceIncomingMessage();
//        message.setData("123");
//        callback.newMessageArrived(eui, message);
//        verify(ngsiManager, times(1)).updateDeviceAttributes(eq(device), anyListOf(ContextAttribute.class));
//    }
//
//    @Test
//    public void testNewMessageArrivedBadMessage() throws AgentException {
//        String eui = "123";
//        DeviceEntity device = new DeviceEntity();
//        device.setDeviceID(eui);
//        device.setType("type");
//        device.setPort(1);
//        device.setName("name");
//        device.setSubscriptionId("123");
//        when(deviceRepository.findById(eui)).thenReturn(java.util.Optional.of(device));
//        DeviceIncomingMessage message = new DeviceIncomingMessage();
//        message.setData(null);
//        callback.newMessageArrived(eui, message);
//        message.setValue(null);
//        callback.newMessageArrived(eui, message);
//        callback.newMessageArrived(eui, null);
//        verify(ngsiManager, times(0)).updateDeviceAttributes(eq(device), anyListOf(ContextAttribute.class));
//    }
//
//    @Test
//    public void testNewMessageArrivedForUnknownDevice() throws AgentException {
//        String unknown = "unknown";
//        when(deviceRepository.findById(unknown)).thenReturn(null);
//        DeviceIncomingMessage message = new DeviceIncomingMessage();
//        message.setData("123");
//        callback.newMessageArrived(unknown, message);
//        verify(ngsiManager, times(0)).updateDeviceAttributes(any(DeviceEntity.class), anyListOf(ContextAttribute.class));
//    }
//
//    @Test
//    public void testNewMessageArrivedNullConverter() throws AgentException {
//        ReflectionTestUtils.setField(agent, "converter", null);
//        String eui = "123";
//        DeviceEntity device = new DeviceEntity();
//        device.setDeviceID(eui);
//        device.setType("type");
//        device.setPort(1);
//        device.setName("name");
//        device.setSubscriptionId("123");
//        when(deviceRepository.findById(eui)).thenReturn(java.util.Optional.of(device));
//        DeviceIncomingMessage message = new DeviceIncomingMessage();
//        message.setData("123");
//        callback.newMessageArrived(eui, message);
//        verify(ngsiManager, times(0)).updateDeviceAttributes(eq(device), anyListOf(ContextAttribute.class));
//        ReflectionTestUtils.setField(agent, "converter", converter);
//    }
//}
