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
// * * Created by François SUC on 14/09/2016.
// */
//
//package com.orange.fiware.openlpwa.iotagent;
//
//import com.orange.fiware.openlpwa.domain.DeviceEntity;
//import com.orange.fiware.openlpwa.exception.AgentException;
//import com.orange.fiware.openlpwa.exception.ConfigurationException;
//import com.orange.fiware.openlpwa.provider.OpenLpwaMqttProvider;
//import com.orange.fiware.openlpwa.provider.OpenLpwaProvider;
//import com.orange.fiware.openlpwa.provider.model.DeviceCommand;
//import com.orange.fiware.openlpwa.provider.model.DeviceInfo;
//import com.orange.fiware.openlpwa.provider.model.RegisterDeviceCommandParameter;
//import com.orange.fiware.openlpwa.ngsi.NgsiManager;
//import com.orange.fiware.openlpwa.repository.DeviceEntityRepository;
//import com.orange.ngsi.model.*;
//import org.hamcrest.CoreMatchers;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Matchers;
//import org.mockito.Mock;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.util.concurrent.FailureCallback;
//import org.springframework.util.concurrent.ListenableFuture;
//import org.springframework.util.concurrent.SuccessCallback;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import static com.orange.ngsi.model.CodeEnum.CODE_200;
//import static com.orange.ngsi.model.CodeEnum.CODE_500;
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.*;
//import static org.mockito.MockitoAnnotations.initMocks;
//
///**
// * Agent unit tests
// */
//public class AgentTest {
//
//    private final static String subscriptionId = "abcdef1234567890";
//    private final static String commandName = "testCommand";
//    @InjectMocks
//    Agent agent;
//    @Mock
//    private DeviceEntityRepository mockDeviceRepository;
//    @Mock
//    private NgsiManager mockNgsiManager;
//    @Mock
//    private OpenLpwaProvider mockLpwaProvider;
//    @Mock
//    private OpenLpwaMqttProvider mockOpenLpwaMqttProvider;
//    @Mock
//    private SuccessCallback<Boolean> resultCallback;
//    private Device device;
//    private ContextAttribute commandAttribute;
//
//    @Before
//    public void setup() {
//        initMocks(this);
//        device = new Device();
//        device.setDeviceID("testDevEUI");
//        device.setPort(2);
//        device.setEntityName("testEntityName");
//        device.setEntityType("testEntityType");
//        List<String> commands = new ArrayList<>();
//        commands.add("testCommand");
//        device.setCommands(commands);
//
//        commandAttribute = new ContextAttribute();
//        commandAttribute.setName("testCommand_command");
//        commandAttribute.setType("command");
//        commandAttribute.setValue("value");
//    }
//
//    @After
//    public void teardown() {
//        reset(mockDeviceRepository, mockNgsiManager,
//                mockLpwaProvider, mockOpenLpwaMqttProvider, resultCallback);
//    }
//
//    @Test
//    public void testStartAgentWithoutConverter() {
//        simulateMqttConnectionSuccess();
//        simulateMqttSubscriptionSuccess();
//
//        agent.start(null,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    assertThat(exception, CoreMatchers.instanceOf(AgentException.class));
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testStartAgentWithSuccess() {
//        simulateMqttConnectionSuccess();
//        simulateMqttSubscriptionSuccess();
//
//        agent.start(new OpenLpwaNgsiConverterTest(false),
//                () -> resultCallback.onSuccess(true),
//                exception -> fail("Failed callback unexpected call")
//        );
//
//        verify(resultCallback).onSuccess(true);
//    }
//
//    @Test
//    public void testStartAgentWithConnectionFailure() {
//        simulateMqttConnectionFailure();
//        simulateMqttSubscriptionSuccess();
//
//        agent.start(new OpenLpwaNgsiConverterTest(false),
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testStartAgentWithSubscriptionFailure() {
//        simulateMqttConnectionSuccess();
//        simulateMqttSubscriptionFailure();
//
//        agent.start(new OpenLpwaNgsiConverterTest(false),
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testStopAgentWithSuccess() {
//        simulateMqttDisconnectionSuccess();
//
//        agent.stop(
//                () -> resultCallback.onSuccess(true),
//                exception -> fail("Failed callback unexpected call")
//        );
//
//        verify(resultCallback).onSuccess(true);
//    }
//
//    @Test
//    public void testStopAgentWithDisconnectionFailure() {
//        simulateMqttDisconnectionFailure();
//
//        agent.stop(
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithoutDevice() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsSuccess(false);
//
//        agent.register(null,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    assertThat(exception, CoreMatchers.instanceOf(AgentException.class));
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithoutDeviceEUI() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsSuccess(false);
//
//        Device invalidDevice = new Device();
//        invalidDevice.setPort(2);
//        invalidDevice.setEntityName("testEntityName");
//        invalidDevice.setEntityType("testEntityType");
//        agent.register(invalidDevice,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    assertThat(exception, CoreMatchers.instanceOf(AgentException.class));
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithoutDevicePort() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsSuccess(false);
//
//        Device invalidDevice = new Device();
//        invalidDevice.setDeviceID("testDeviceEUI");
//        invalidDevice.setEntityName("testEntityName");
//        invalidDevice.setEntityType("testEntityType");
//        agent.register(invalidDevice,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    assertThat(exception, CoreMatchers.instanceOf(AgentException.class));
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithoutEntityName() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsSuccess(false);
//
//        Device invalidDevice = new Device();
//        invalidDevice.setDeviceID("testDeviceEUI");
//        invalidDevice.setPort(2);
//        invalidDevice.setEntityType("testEntityType");
//        agent.register(invalidDevice,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    assertThat(exception, CoreMatchers.instanceOf(AgentException.class));
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithoutEntityType() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsSuccess(false);
//
//        Device invalidDevice = new Device();
//        invalidDevice.setDeviceID("testDeviceEUI");
//        invalidDevice.setPort(2);
//        invalidDevice.setEntityName("testEntityName");
//        agent.register(invalidDevice,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    assertThat(exception, CoreMatchers.instanceOf(AgentException.class));
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithSuccess() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsSuccess(false);
//
//        assertNotNull(device.toString());
//
//        agent.register(device,
//                () -> resultCallback.onSuccess(true),
//                exception -> fail("Failed callback unexpected call")
//        );
//
//        verify(resultCallback).onSuccess(true);
//    }
//
//    @Test
//    public void testRegisterDeviceWithGetDeviceInfoFailure() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationFailure();
//        simulateNgsiManagerSubscribeToCommandsSuccess(false);
//
//        agent.register(device,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithNotActivatedDevice() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.DEACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsSuccess(false);
//
//        agent.register(device,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithSubscribeToCommandsFailure() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsFailure();
//
//        agent.register(device,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithSubscribeToCommandsErrorCode() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsSuccess(true);
//
//        agent.register(device,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testRegisterDeviceWithoutCommand() throws Exception {
//        device.setCommands(null);
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//
//        agent.register(device,
//                () -> {
//                    try {
//                        verify(mockNgsiManager, never()).subscribeToCommands(any(Device.class));
//                    } catch (AgentException e) {
//                    }
//                    resultCallback.onSuccess(true);
//                },
//                exception -> fail("Failed callback unexpected call")
//        );
//
//        verify(resultCallback).onSuccess(true);
//    }
//
//    @Test
//    public void testRegisterDeviceWithSubscribeToCommandsException() throws Exception {
//        simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus.ACTIVATED);
//        simulateNgsiManagerSubscribeToCommandsException();
//
//        agent.register(device,
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testUnregisterDeviceWithSuccess() throws AgentException {
//        simulateNgsiManagerUnsubscribeSuccess(false);
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.unregister(device.getDeviceID(),
//                () -> resultCallback.onSuccess(true),
//                exception -> fail("Failed callback unexpected call")
//        );
//
//        verify(resultCallback).onSuccess(true);
//    }
//
//    @Test
//    public void testUnregisterDeviceWithUnknownDevice() throws AgentException {
//        simulateNgsiManagerUnsubscribeSuccess(false);
//        when(mockDeviceRepository.findById(anyString())).thenReturn(null);
//
//        agent.unregister(device.getDeviceID(),
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testUnregisterDeviceWithUnsubscriptionErrorCode() throws AgentException {
//        simulateNgsiManagerUnsubscribeSuccess(true);
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.unregister(device.getDeviceID(),
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testUnregisterDeviceWithUnsubscriptionFailure() throws AgentException {
//        simulateNgsiManagerUnsubscribeFailure();
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.unregister(device.getDeviceID(),
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testUnregisterDeviceWithoutSubscriptionId() throws Exception {
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, null)));
//
//        agent.unregister(device.getDeviceID(),
//                () -> {
//                    try {
//                        verify(mockNgsiManager, never()).unsubscribe(anyString());
//                    } catch (AgentException e) {
//                    }
//                    resultCallback.onSuccess(true);
//                },
//                exception -> fail("Failed callback unexpected call")
//        );
//
//        verify(resultCallback).onSuccess(true);
//    }
//
//    @Test
//    public void testUnregisterDeviceWithUnsubscribeToCommandsException() throws Exception {
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//        simulateNgsiManagerUnsubscribeException();
//
//        agent.unregister(device.getDeviceID(),
//                () -> fail("Success callback unexpected call"),
//                exception -> {
//                    assertNotNull("exception is null", exception);
//                    resultCallback.onSuccess(false);
//                }
//        );
//
//        verify(resultCallback).onSuccess(false);
//    }
//
//    @Test
//    public void testExecuteCommandsWithUnregisteredDevice() {
//        when(mockDeviceRepository.findById(anyString())).thenReturn(null);
//
//        agent.executeCommand(device.getDeviceID(), commandName, commandAttribute,
//                (success, creationDate) -> assertEquals(false, success));
//    }
//
//    @Test
//    public void testExecuteCommandsWithoutConverter() {
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.executeCommand(device.getDeviceID(), commandName, commandAttribute,
//                (success, creationDate) -> assertEquals(false, success));
//    }
//
//    @Test
//    public void testExecuteCommandsWithoutPayload() {
//        simulateMqttConnectionSuccess();
//        simulateMqttSubscriptionSuccess();
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.start(new OpenLpwaNgsiConverterTest(true),
//                () -> agent.executeCommand(device.getDeviceID(), commandName, commandAttribute,
//                        (success, creationDate) -> assertEquals(false, success)),
//                exception -> fail("Failed callback unexpected call")
//        );
//    }
//
//    @Test
//    public void testExecuteCommandsWithSuccess() throws Exception {
//        simulateMqttConnectionSuccess();
//        simulateMqttSubscriptionSuccess();
//        simulateOpenLpwaProviderRegisterDeviceCommandSuccess(DeviceCommand.DeviceCommandStatus.SENT);
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.start(new OpenLpwaNgsiConverterTest(false),
//                () -> agent.executeCommand(device.getDeviceID(), commandName, commandAttribute,
//                        (success, creationDate) -> {
//                            assertEquals(true, success);
//                            assertNotNull(creationDate);
//                        }
//                ),
//                exception -> fail("Failed callback unexpected call")
//        );
//    }
//
//    @Test
//    public void testExecuteCommandsWithDeviceCommandStatusError() throws Exception {
//        simulateMqttConnectionSuccess();
//        simulateMqttSubscriptionSuccess();
//        simulateOpenLpwaProviderRegisterDeviceCommandSuccess(DeviceCommand.DeviceCommandStatus.ERROR);
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.start(new OpenLpwaNgsiConverterTest(false),
//                () -> agent.executeCommand(device.getDeviceID(), commandName, commandAttribute,
//                        (success, creationDate) -> assertEquals(false, success)),
//                exception -> fail("Failed callback unexpected call")
//        );
//    }
//
//    @Test
//    public void testExecuteCommandsWithRegisterDeviceCommandFailure() throws Exception {
//        simulateMqttConnectionSuccess();
//        simulateMqttSubscriptionSuccess();
//        simulateOpenLpwaProviderRegisterDeviceCommandFailure();
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.start(new OpenLpwaNgsiConverterTest(false),
//                () -> agent.executeCommand(device.getDeviceID(), commandName, commandAttribute,
//                        (success, creationDate) -> assertEquals(false, success)),
//                exception -> fail("Failed callback unexpected call")
//        );
//    }
//
//    @Test
//    public void testExecuteCommandsWithRegisterDeviceCommandException() throws Exception {
//        simulateMqttConnectionSuccess();
//        simulateMqttSubscriptionSuccess();
//        simulateOpenLpwaProviderRegisterDeviceCommandException();
//        when(mockDeviceRepository.findById(anyString())).thenReturn(java.util.Optional.of(new DeviceEntity(device, subscriptionId)));
//
//        agent.start(new OpenLpwaNgsiConverterTest(false),
//                () -> agent.executeCommand(device.getDeviceID(), commandName, commandAttribute,
//                        (success, creationDate) -> assertEquals(false, success)),
//                exception -> fail("Failed callback unexpected call")
//        );
//    }
//
//    @Test
//    public void testUpdateSubscriptions() throws AgentException {
//        List<DeviceEntity> devicesList = new ArrayList<>();
//        devicesList.add(new DeviceEntity(device, subscriptionId));
//        DeviceEntity secondDevice = new DeviceEntity();
//        secondDevice.setDeviceID("secondDevice");
//        secondDevice.setPort(2);
//        secondDevice.setSubscriptionId("secondDeviceSubscriptionId");
//        devicesList.add(secondDevice);
//        when(mockDeviceRepository.findAll()).thenReturn(devicesList);
//
//        ReflectionTestUtils.invokeMethod(agent, "updateSubcriptions");
//
//        verify(mockNgsiManager, times(2)).updateSubscription(anyString());
//        verify(mockNgsiManager, times(1)).updateSubscription(subscriptionId);
//        verify(mockNgsiManager, times(1)).updateSubscription(secondDevice.getSubscriptionId());
//    }
//
//    private void simulateMqttConnectionSuccess() {
//        doAnswer(invocationOnMock -> {
//            SuccessCallback<String> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
//            originalArgument.onSuccess("connectedClientId");
//            return null;
//        }).when(mockOpenLpwaMqttProvider).connect(Matchers.any(), any(FailureCallback.class));
//    }
//
//    private void simulateMqttConnectionFailure() {
//        doAnswer(invocationOnMock -> {
//            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
//            originalArgument.onFailure(new Exception("simulateMqttConnectionFailure"));
//            return null;
//        }).when(mockOpenLpwaMqttProvider).connect(Matchers.any(), any(FailureCallback.class));
//    }
//
//    private void simulateMqttDisconnectionSuccess() {
//        doAnswer(invocationOnMock -> {
//            SuccessCallback<String> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
//            originalArgument.onSuccess("disconnectedClientId");
//            return null;
//        }).when(mockOpenLpwaMqttProvider).disconnect(Matchers.any(), any(FailureCallback.class));
//    }
//
//    private void simulateMqttDisconnectionFailure() {
//        doAnswer(invocationOnMock -> {
//            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
//            originalArgument.onFailure(new Exception("simulateMqttDisconnectionFailure"));
//            return null;
//        }).when(mockOpenLpwaMqttProvider).disconnect(Matchers.any(), any(FailureCallback.class));
//    }
//
//    private void simulateMqttSubscriptionSuccess() {
//        doAnswer(invocationOnMock -> {
//            SuccessCallback<String> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
//            originalArgument.onSuccess("subscribedClientId");
//            return null;
//        }).when(mockOpenLpwaMqttProvider).subscribe(Matchers.any(), any(FailureCallback.class));
//    }
//
//    private void simulateMqttSubscriptionFailure() {
//        doAnswer(invocationOnMock -> {
//            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
//            originalArgument.onFailure(new Exception("simulateMqttSubscriptionFailure"));
//            return null;
//        }).when(mockOpenLpwaMqttProvider).subscribe(Matchers.any(), any(FailureCallback.class));
//    }
//
//    private void simulateOpenLpwaProviderGetDeviceInformationSuccess(DeviceInfo.DeviceStatus deviceStatus) throws ConfigurationException {
//        ArgumentCaptor<SuccessCallback> successArg = ArgumentCaptor.forClass(SuccessCallback.class);
//        ListenableFuture<DeviceInfo> responseFuture = mock(ListenableFuture.class);
//        when(mockLpwaProvider.getDeviceInformation(anyString())).thenReturn(responseFuture);
//        doAnswer(invocationOnMock -> {
//            SuccessCallback<DeviceInfo> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
//            DeviceInfo deviceInfo = new DeviceInfo();
//            deviceInfo.setDeviceStatus(deviceStatus);
//            originalArgument.onSuccess(deviceInfo);
//            return null;
//        }).when(responseFuture).addCallback(successArg.capture(), any());
//    }
//
//    private void simulateOpenLpwaProviderGetDeviceInformationFailure() throws ConfigurationException {
//        ArgumentCaptor<FailureCallback> failureArg = ArgumentCaptor.forClass(FailureCallback.class);
//        ListenableFuture<DeviceInfo> responseFuture = mock(ListenableFuture.class);
//        when(mockLpwaProvider.getDeviceInformation(anyString())).thenReturn(responseFuture);
//        doAnswer(invocationOnMock -> {
//            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
//            originalArgument.onFailure(new Exception());
//            return null;
//        }).when(responseFuture).addCallback(any(), failureArg.capture());
//    }
//
//    private void simulateNgsiManagerSubscribeToCommandsSuccess(Boolean responseError) throws AgentException {
//        ArgumentCaptor<SuccessCallback> successArg = ArgumentCaptor.forClass(SuccessCallback.class);
//        ListenableFuture<SubscribeContextResponse> responseFuture = mock(ListenableFuture.class);
//        when(mockNgsiManager.subscribeToCommands(any(Device.class))).thenReturn(responseFuture);
//        doAnswer(invocationOnMock -> {
//            SuccessCallback<SubscribeContextResponse> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
//            SubscribeContextResponse subscribeContextResponse = new SubscribeContextResponse();
//            subscribeContextResponse.setSubscribeError(responseError ? new SubscribeError() : null);
//            SubscribeResponse subscribeResponse = new SubscribeResponse();
//            subscribeResponse.setSubscriptionId(subscriptionId);
//            subscribeContextResponse.setSubscribeResponse(subscribeResponse);
//            originalArgument.onSuccess(subscribeContextResponse);
//            return null;
//        }).when(responseFuture).addCallback(successArg.capture(), any());
//    }
//
//    private void simulateNgsiManagerSubscribeToCommandsFailure() throws AgentException {
//        ArgumentCaptor<FailureCallback> failureArg = ArgumentCaptor.forClass(FailureCallback.class);
//        ListenableFuture<SubscribeContextResponse> responseFuture = mock(ListenableFuture.class);
//        when(mockNgsiManager.subscribeToCommands(any(Device.class))).thenReturn(responseFuture);
//        doAnswer(invocationOnMock -> {
//            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
//            originalArgument.onFailure(new Exception());
//            return null;
//        }).when(responseFuture).addCallback(any(), failureArg.capture());
//    }
//
//    private void simulateNgsiManagerSubscribeToCommandsException() throws AgentException {
//        when(mockNgsiManager.subscribeToCommands(any(Device.class)))
//                .thenThrow(new AgentException("testAgentException"));
//    }
//
//    private void simulateNgsiManagerUnsubscribeSuccess(Boolean responseError) throws AgentException {
//        ArgumentCaptor<SuccessCallback> successArg = ArgumentCaptor.forClass(SuccessCallback.class);
//        ListenableFuture<UnsubscribeContextResponse> responseFuture = mock(ListenableFuture.class);
//        when(mockNgsiManager.unsubscribe(anyString())).thenReturn(responseFuture);
//        doAnswer(invocationOnMock -> {
//            SuccessCallback<UnsubscribeContextResponse> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
//            UnsubscribeContextResponse unsubscribeContextResponse = new UnsubscribeContextResponse();
//            StatusCode statusCode = new StatusCode(responseError ? CODE_500 : CODE_200, "");
//            unsubscribeContextResponse.setStatusCode(statusCode);
//            originalArgument.onSuccess(unsubscribeContextResponse);
//            return null;
//        }).when(responseFuture).addCallback(successArg.capture(), any());
//    }
//
//    private void simulateNgsiManagerUnsubscribeFailure() throws AgentException {
//        ArgumentCaptor<FailureCallback> failureArg = ArgumentCaptor.forClass(FailureCallback.class);
//        ListenableFuture<UnsubscribeContextResponse> responseFuture = mock(ListenableFuture.class);
//        when(mockNgsiManager.unsubscribe(anyString())).thenReturn(responseFuture);
//        doAnswer(invocationOnMock -> {
//            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
//            originalArgument.onFailure(new Exception());
//            return null;
//        }).when(responseFuture).addCallback(any(), failureArg.capture());
//    }
//
//    private void simulateNgsiManagerUnsubscribeException() throws AgentException {
//        when(mockNgsiManager.unsubscribe(anyString()))
//                .thenThrow(new AgentException("testAgentException"));
//    }
//
//    private void simulateOpenLpwaProviderRegisterDeviceCommandSuccess(DeviceCommand.DeviceCommandStatus deviceCommandStatus) throws ConfigurationException {
//        ArgumentCaptor<SuccessCallback> successArg = ArgumentCaptor.forClass(SuccessCallback.class);
//        ListenableFuture<DeviceCommand> responseFuture = mock(ListenableFuture.class);
//        when(mockLpwaProvider.registerDeviceCommand(anyString(), any(RegisterDeviceCommandParameter.class))).thenReturn(responseFuture);
//        doAnswer(invocationOnMock -> {
//            SuccessCallback<DeviceCommand> originalArgument = invocationOnMock.getArgumentAt(0, SuccessCallback.class);
//            DeviceCommand deviceCommand = new DeviceCommand();
//            deviceCommand.setCommandStatus(deviceCommandStatus);
//            deviceCommand.setCreationTs(new Date());
//            originalArgument.onSuccess(deviceCommand);
//            return null;
//        }).when(responseFuture).addCallback(successArg.capture(), any());
//    }
//
//    private void simulateOpenLpwaProviderRegisterDeviceCommandFailure() throws ConfigurationException {
//        ArgumentCaptor<FailureCallback> failureArg = ArgumentCaptor.forClass(FailureCallback.class);
//        ListenableFuture<DeviceCommand> responseFuture = mock(ListenableFuture.class);
//        when(mockLpwaProvider.registerDeviceCommand(anyString(), any(RegisterDeviceCommandParameter.class))).thenReturn(responseFuture);
//        doAnswer(invocationOnMock -> {
//            FailureCallback originalArgument = invocationOnMock.getArgumentAt(1, FailureCallback.class);
//            originalArgument.onFailure(new Exception());
//            return null;
//        }).when(responseFuture).addCallback(any(), failureArg.capture());
//    }
//
//    private void simulateOpenLpwaProviderRegisterDeviceCommandException() throws ConfigurationException {
//        when(mockLpwaProvider.registerDeviceCommand(anyString(), any(RegisterDeviceCommandParameter.class)))
//        .thenThrow(new ConfigurationException("testConfigurationException"));
//    }
//}
