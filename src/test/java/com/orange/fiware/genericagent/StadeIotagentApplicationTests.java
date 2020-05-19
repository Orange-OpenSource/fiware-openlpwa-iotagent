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
 * * Created by François SUC on 10/04/2020.
 */

package com.orange.fiware.genericagent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.fiware.openlpwa.exception.ConfigurationException;
import com.orange.fiware.openlpwa.provider.OpenLpwaMqttProvider;
import com.orange.fiware.openlpwa.provider.OpenLpwaMqttProviderCallback;
import com.orange.ngsi.model.ContextAttribute;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.HashMap;

import static org.mockito.MockitoAnnotations.initMocks;


class StadeIotagentApplicationTests {

    @Mock
    OpenLpwaMqttProviderCallback clientCallback;

    @Test
    void contextLoads() {
    }

    @Test
    void testMessageArrived() throws MqttException, ConfigurationException {
        initMocks(this);

        String jsonData = "{\n   \"metadata\": {\n      \"connector\": \"lora\",\n      \"source\": \"urn:lo:nsid:lora:70B3D59BA0009832\",\n      \"encoding\": \"sonde_test_num2\",\n      \"group\": {\n         \"path\": \"/Saint-Quentin-Philippe-Roth_1\",\n         \"id\": \"O6BEHg\"\n      },\n      \"network\": {\n         \"lora\": {\n            \"signalLevel\": 5,\n            \"rssi\": -99,\n            \"gatewayCnt\": 1,\n            \"esp\": -99.79,\n            \"sf\": 12,\n            \"messageType\": \"UNCONFIRMED_DATA_UP\",\n            \"port\": 5,\n            \"snr\": 7,\n            \"ack\": false,\n            \"location\": {\n               \"alt\": 0,\n               \"accuracy\": 10000,\n               \"lon\": 3.28297,\n               \"lat\": 49.8527\n            },\n            \"fcnt\": 1,\n            \"devEUI\": \"70B3D59BA0009832\"\n         }\n      }\n   },\n   \"streamId\": \"urn:lo:nsid:lora:70B3D59BA0009832\",\n   \"created\": \"2019-10-23T12:32:48.011Z\",\n   \"extra\": {},\n   \"location\": {\n      \"provider\": \"lora\",\n      \"alt\": 0,\n      \"accuracy\": 10000,\n      \"lon\": 3.28297,\n      \"lat\": 49.8527\n   },\n   \"model\": \"lora_v0\",\n   \"id\": \"5db04870c74a496551742859\",\n   \"value\": {\n      \"temp_sol\": 14,\n      \"payload\": \"6d011f0179\",\n      \"humidity\": 109,\n      \"batterie\": 3.77\n   },\n   \"timestamp\": \"2019-10-23T12:32:44.546Z\",\n   \"tags\": [\n      \"capteur_5\"\n   ]\n}";

        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(jsonData.getBytes());

        OpenLpwaMqttProvider mqttProvider = new OpenLpwaMqttProvider("tcp://localhost:1883", "testClientId", "TESTOpenLpwaProviderMQTT", "fifo/fifo_orange", clientCallback);
        mqttProvider.messageArrived("", mqttMessage);
    }

    @Test
    void testJsonToMap() throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"myName\", \"age\":99}";

        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};

        HashMap<String, Object> map = mapper.readValue(json, typeRef);
        System.out.println(map);
    }

    @Test
    void testJsonCoord() throws JsonProcessingException {
        Float[] arrayCoord = {(float) 49.852673, (float) 3.283033};
        Point point = new Point("Point", arrayCoord);

        ContextAttribute location = new ContextAttribute();
        location.setName("location");
        location.setType("geo:json");
        location.setValue(point);

        ObjectMapper mapper = new ObjectMapper();
        String json;

        json = mapper.writeValueAsString(location);
        System.out.println(json);
    }

    public class Point {
        private  String type;
        private Float[] coordinates;

        public Point(String type, Float[] coordinates) {
            this.type = type;
            this.coordinates = coordinates;
        }

        public String getType() {
            return type;
        }

        public Float[] getCoordinates() {
            return coordinates;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setCoordinates(Float[] coordinates) {
            this.coordinates = coordinates;
        }
    }
}
