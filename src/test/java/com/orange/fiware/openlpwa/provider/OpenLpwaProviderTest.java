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
import com.orange.fiware.openlpwa.provider.exception.OpenLpwaProviderErrorException;
import com.orange.fiware.openlpwa.provider.model.DeviceCommand;
import com.orange.fiware.openlpwa.provider.model.DeviceInfo;
import com.orange.fiware.openlpwa.provider.model.RegisterDeviceCommandParameter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * OpenLpwaProvider unit tests
 */
public class OpenLpwaProviderTest {

    private final static String url = "http://localhost:8080";
    private final static String basePath = "api/v0";
    private final static String apiKey = "TESTOpenLpwaProvider";
    private final static String deviceEUI = "testdevice";
    private OpenLpwaProvider client;
    private MockRestServiceServer mockServer;

    @Before
    public void setup() {
        client = new OpenLpwaProvider(url, apiKey);
        AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
        asyncRestTemplate.setErrorHandler(new OpenLpwaResponseErrorHandler());
        ReflectionTestUtils.setField(client, "asyncRestTemplate", asyncRestTemplate);
        mockServer = MockRestServiceServer.createServer(asyncRestTemplate);
    }

    @Test
    public void testGetDeviceInformation() throws Exception {
        assertEquals(url, client.getUrl());
        assertEquals(apiKey, client.getApiKey());
        String responseBody = "{\"devEUI\":\"testdevice\",\"name\":\"DeviceTest\",\"activationType\":\"OTAA\",\"profile\":\"SMTC/LoRaMoteClassA.2\",\"deviceStatus\":\"ACTIVATED\",\"appEUI\":\"0000000000000000\",\"tags\":[\"Lyon\",\"Test\"],\"lastActivationTs\":\"2016-06-09T08:04:37.971Z\",\"lastDeactivationTs\":\"2016-06-10T00:04:37.971Z\",\"lastCommunicationTs\":\"2016-06-03T15:55:36.944Z\",\"lastDlFcnt\":1,\"lastSignalLevel\":127,\"creationTs\":\"2016-06-03T15:20:53.803Z\",\"updateTs\":\"2016-06-09T08:04:37.971Z\"}";
        mockServer.expect(requestTo(String.format("%1$s/%2$s/vendors/lora/devices/%3$s", url, basePath, deviceEUI)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", apiKey))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        DeviceInfo response = client.getDeviceInformation(deviceEUI).get();

        mockServer.verify();

        assertEquals(deviceEUI, response.getDeviceEUI());
        assertEquals("DeviceTest", response.getName());
        assertEquals(DeviceInfo.DeviceActivationType.OTAA, response.getActivationType());
        assertEquals("SMTC/LoRaMoteClassA.2", response.getProfile());
        assertEquals(DeviceInfo.DeviceStatus.ACTIVATED, response.getDeviceStatus());
        assertEquals("0000000000000000", response.getAppEUI());
        assertEquals(2, response.getTags().size());
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        dateFormat.setTimeZone(timeZone);
        assertEquals("2016-06-09T08:04:37.971Z", dateFormat.format(response.getLastActivationTs()));
        assertEquals("2016-06-10T00:04:37.971Z", dateFormat.format(response.getLastDeactivationTs()));
        assertEquals("2016-06-03T15:55:36.944Z", dateFormat.format(response.getLastCommunicationTs()));
        assertEquals("2016-06-03T15:20:53.803Z", dateFormat.format(response.getCreationTs()));
        assertEquals("2016-06-09T08:04:37.971Z", dateFormat.format(response.getUpdateTs()));
        assertEquals(1, response.getLastDlFcnt().intValue());
        assertEquals(127, response.getLastSignalLevel().intValue());
    }

    @Test(expected = ConfigurationException.class)
    public void testGetDeviceInformationWithoutApiKey() throws Exception {
        client.setApiKey(null);
        client.getDeviceInformation(deviceEUI);
    }

    @Test
    public void testGetDeviceInformationWithoutUrl() throws Exception {
        client.setUrl(null);
        try {
            client.getDeviceInformation(deviceEUI);
        } catch (ConfigurationException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetDeviceInformationWithoutDeviceEUI() throws Exception {
        try {
            client.getDeviceInformation(null);
        } catch (ConfigurationException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetDeviceInformationWithInvalidDeviceEUI() throws Exception {
        String responseBody = "{\"id\":\"uqg3ndxm\",\"code\":4001,\"message\":\"Le DevEui testdevice est invalide (la taille doit être de 16)\"}";
        mockServer.expect(requestTo(String.format("%1$s/%2$s/vendors/lora/devices/%3$s", url, basePath, deviceEUI)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", apiKey))
                .andRespond(new OpenLpwaProviderResponseCreator(HttpStatus.BAD_REQUEST, responseBody, MediaType.APPLICATION_JSON));

        try {
            client.getDeviceInformation(deviceEUI).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(OpenLpwaProviderErrorException.class));
            OpenLpwaProviderErrorException clientErrorException = (OpenLpwaProviderErrorException) e.getCause();
            assertNotNull(clientErrorException.getId());
            assertEquals(HttpStatus.BAD_REQUEST, clientErrorException.getStatusCode());
            assertEquals(4001, clientErrorException.getErrorCode());
            assertNotNull(clientErrorException.getMessage());
        }
        mockServer.verify();
    }

    @Test
    public void testGetDeviceInformationWithDeviceEUINotFound() throws Exception {
        String unknownDeviceEUI = "70B3D59BA0000000";
        String responseBody = "{\"id\":\"uqg3ndxm\",\"code\":40411,\"message\":\"L'équipement identifié par le DevEui 70B3D59BA0000000 n'a pas été trouvé\"}";
        mockServer.expect(requestTo(String.format("%1$s/%2$s/vendors/lora/devices/%3$s", url, basePath, unknownDeviceEUI)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", apiKey))
                .andRespond(new OpenLpwaProviderResponseCreator(HttpStatus.NOT_FOUND, responseBody, MediaType.APPLICATION_JSON));

        try {
            client.getDeviceInformation(unknownDeviceEUI).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(OpenLpwaProviderErrorException.class));
            OpenLpwaProviderErrorException clientErrorException = (OpenLpwaProviderErrorException) e.getCause();
            assertNotNull(clientErrorException.getId());
            assertEquals(HttpStatus.NOT_FOUND, clientErrorException.getStatusCode());
            assertEquals(40411, clientErrorException.getErrorCode());
            assertNotNull(clientErrorException.getMessage());
        }
        mockServer.verify();
    }

    @Test
    public void testGetDeviceInformationWithInternalServerError() throws Exception {
        mockServer.expect(requestTo(String.format("%1$s/%2$s/vendors/lora/devices/%3$s", url, basePath, deviceEUI)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", apiKey))
                .andRespond(withServerError());

        try {
            client.getDeviceInformation(deviceEUI).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(HttpServerErrorException.class));
            HttpServerErrorException serverErrorException = (HttpServerErrorException) e.getCause();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, serverErrorException.getStatusCode());
        }
        mockServer.verify();
    }

    @Test
    public void testGetDeviceInformationWithInvalidJSONError() throws Exception {
        String responseBody = "{\"badf00d\":\"yes\"}";
        mockServer.expect(requestTo(String.format("%1$s/%2$s/vendors/lora/devices/%3$s", url, basePath, deviceEUI)))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-API-Key", apiKey))
                .andRespond(new OpenLpwaProviderResponseCreator(HttpStatus.NOT_FOUND, responseBody, MediaType.APPLICATION_JSON));

        try {
            client.getDeviceInformation(deviceEUI).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(HttpClientErrorException.class));
            HttpClientErrorException clientErrorException = (HttpClientErrorException) e.getCause();
            assertEquals(HttpStatus.NOT_FOUND, clientErrorException.getStatusCode());
        }
        mockServer.verify();
    }

    @Test
    public void testRegisterDeviceCommand() throws Exception {
        String responseBody = "{\"id\":\"5703cfa9e4b0b24cd6862866\",\"data\":\"1234\",\"fcnt\":122,\"port\":1,\"confirmed\":true,\"commandStatus\":\"SENT\",\"creationTs\":\"2016-06-03T15:50:39.669Z\"}";
        mockServer.expect(requestTo(String.format("%1$s/%2$s/vendors/lora/devices/%3$s/commands", url, basePath, deviceEUI)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-API-Key", apiKey))
                .andRespond(new OpenLpwaProviderResponseCreator(HttpStatus.CREATED, responseBody, MediaType.APPLICATION_JSON));

        String data = "1234";
        Integer port = 1;
        RegisterDeviceCommandParameter parameter = new RegisterDeviceCommandParameter();
        parameter.setData(data);
        parameter.setPort(port);
        parameter.setConfirmed(true);
        DeviceCommand response = client.registerDeviceCommand(deviceEUI, parameter).get();

        mockServer.verify();

        assertEquals("5703cfa9e4b0b24cd6862866", response.getId());
        assertEquals(data, response.getData());
        assertEquals(port, response.getPort());
        assertTrue(response.getConfirmed());
        assertEquals(DeviceCommand.DeviceCommandStatus.SENT, response.getCommandStatus());
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        dateFormat.setTimeZone(timeZone);
        assertEquals("2016-06-03T15:50:39.669Z", dateFormat.format(response.getCreationTs()));
    }

    @Test
    public void testRegisterDeviceCommandWithInvalidDeviceEUI() throws Exception {
        String responseBody = "{\"id\":\"uqg3ndxm\",\"code\":4001,\"message\":\"Le DevEui testdevice est invalide (la taille doit être de 16)\"}";
        mockServer.expect(requestTo(String.format("%1$s/%2$s/vendors/lora/devices/%3$s/commands", url, basePath, deviceEUI)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-API-Key", apiKey))
                .andRespond(new OpenLpwaProviderResponseCreator(HttpStatus.BAD_REQUEST, responseBody, MediaType.APPLICATION_JSON));

        try {
            String data = "1234";
            Integer port = 1;
            RegisterDeviceCommandParameter parameter = new RegisterDeviceCommandParameter();
            parameter.setData(data);
            parameter.setPort(port);
            parameter.setConfirmed(true);
            client.registerDeviceCommand(deviceEUI, parameter).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(OpenLpwaProviderErrorException.class));
            OpenLpwaProviderErrorException clientErrorException = (OpenLpwaProviderErrorException) e.getCause();
            assertNotNull(clientErrorException.getId());
            assertEquals(HttpStatus.BAD_REQUEST, clientErrorException.getStatusCode());
            assertEquals(4001, clientErrorException.getErrorCode());
            assertNotNull(clientErrorException.getMessage());
        }
        mockServer.verify();
    }

    @Test
    public void testRegisterDeviceCommandWithInvalidCommand() throws Exception {
        String responseBody = "{\"id\":\"uqg3ndxm\",\"code\":4002,\"message\":\"Le format de la commande est incorrect pour le champ Payload\"}";
        mockServer.expect(requestTo(String.format("%1$s/%2$s/vendors/lora/devices/%3$s/commands", url, basePath, deviceEUI)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-API-Key", apiKey))
                .andRespond(new OpenLpwaProviderResponseCreator(HttpStatus.BAD_REQUEST, responseBody, MediaType.APPLICATION_JSON));

        try {
            String data = "1234";
            Integer port = 1;
            RegisterDeviceCommandParameter parameter = new RegisterDeviceCommandParameter();
            parameter.setData(data);
            parameter.setPort(port);
            parameter.setConfirmed(true);
            client.registerDeviceCommand(deviceEUI, parameter).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
            assertThat(e.getCause(), instanceOf(OpenLpwaProviderErrorException.class));
            OpenLpwaProviderErrorException clientErrorException = (OpenLpwaProviderErrorException) e.getCause();
            assertNotNull(clientErrorException.getId());
            assertEquals(HttpStatus.BAD_REQUEST, clientErrorException.getStatusCode());
            assertEquals(4002, clientErrorException.getErrorCode());
            assertNotNull(clientErrorException.getMessage());
        }
        mockServer.verify();
    }

    @Test(expected = ConfigurationException.class)
    public void testRegisterDeviceCommandWithoutData() throws Exception {
        RegisterDeviceCommandParameter parameter = new RegisterDeviceCommandParameter();
        parameter.setData(null);
        parameter.setPort(1);
        client.registerDeviceCommand(deviceEUI, parameter);
    }

    @Test(expected = ConfigurationException.class)
    public void testRegisterDeviceCommandWithoutPort() throws Exception {
        RegisterDeviceCommandParameter parameter = new RegisterDeviceCommandParameter();
        parameter.setData("1234");
        parameter.setPort(null);
        client.registerDeviceCommand(deviceEUI, parameter);
    }
}
