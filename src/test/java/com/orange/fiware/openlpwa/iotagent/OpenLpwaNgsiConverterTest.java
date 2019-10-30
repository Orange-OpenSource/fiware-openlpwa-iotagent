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
 * * Created by François SUC on 16/09/2016.
 */

package com.orange.fiware.openlpwa.iotagent;

import com.orange.fiware.openlpwa.provider.model.DeviceIncomingMessage;
import com.orange.ngsi.model.ContextAttribute;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter for the tests with a dummy mode
 */
public class OpenLpwaNgsiConverterTest implements OpenLpwaNgsiConverter {

    private Boolean dummy;

    public OpenLpwaNgsiConverterTest(Boolean dummy) {
        this.dummy = dummy;
    }

    @Override
    public List<ContextAttribute> decodeData(String deviceEUI, String data) {
        if (!dummy) {
            ContextAttribute contextAttribute = new ContextAttribute();
            contextAttribute.setName("testName");
            contextAttribute.setType("testType");
            contextAttribute.setValue("testValue");
            List<ContextAttribute> contextAttributeList = new ArrayList<>();
            contextAttributeList.add(contextAttribute);
            return contextAttributeList;
        }

        return null;
    }

    @Override
    public List<ContextAttribute> decodeData(String deviceEUI, String data, DeviceIncomingMessage incomingMessage) {
        return null;
    }

    @Override
    public String encodeDataForCommand(String deviceEUI, String commandName, ContextAttribute attribute) {
        if (!dummy) {
            return "Oxc0ffee";
        }

        return null;
    }
}
