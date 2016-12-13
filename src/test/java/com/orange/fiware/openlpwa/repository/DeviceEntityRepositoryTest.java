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
 * * Created by François SUC on 03/07/2016.
 */

package com.orange.fiware.openlpwa.repository;

import com.mongodb.util.JSON;
import com.orange.fiware.openlpwa.domain.DeviceEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * DeviceEntityRepository unit tests
 */
@ContextConfiguration(classes = {FakeMongoConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class DeviceEntityRepositoryTest {

    private final static String deviceEUI = "testdevice";
    @Autowired
    private DeviceEntityRepository deviceRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Before
    public void setup() throws Exception {
        insertDevice();
    }

    @Test
    public void testFindByDeviceEUI() {
        DeviceEntity device = deviceRepository.findOne(deviceEUI);
        checkDevice(device);
    }

    private void insertDevice() {
        mongoTemplate.save(JSON.parse("{_id:\"testdevice\",name:\"OpenSpace\",type:\"Room\",subscriptionId:\"51c0ac9ed714fb3b37d7d5a8\",port:1,commands:[\"led\",\"thermostat\"]}"), "devices");
    }

    private void checkDevice(DeviceEntity device) {
        assertEquals(deviceEUI, device.getDeviceEUI());
        assertEquals("OpenSpace", device.getName());
        assertEquals("Room", device.getType());
        assertEquals("51c0ac9ed714fb3b37d7d5a8", device.getSubscriptionId());
        assertEquals(2, device.getCommands().size());
        assertEquals("led", device.getCommands().get(0));
        assertEquals("thermostat", device.getCommands().get(1));
        assertEquals(1, device.getPort());
    }
}
