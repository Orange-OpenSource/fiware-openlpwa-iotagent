/**
 * Copyright (C) 2016 Orange
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * * Created by Christophe AZEMAR on 03/08/2016.
 */

package com.orange.fiware.openlpwa.iotagent;

import com.orange.fiware.openlpwa.provider.model.DeviceIncomingMessage;
import com.orange.ngsi.model.ContextAttribute;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.List;
import java.util.Map;

/**
 * Interface to convert Open LPWA payloads into Ngsi
 */
public interface OpenLpwaNgsiConverter {

    /**
     * Decode an uplink payload
     * @param deviceID DeviceID
     * @param data Hexadecimal payload
     * @return List of <code>ContextAttribute</code> to update Ngsi context broker
     */
    List<ContextAttribute> decodeData(String deviceID, String data);

    List<ContextAttribute> decodeData(String deviceID, String data, DeviceIncomingMessage incomingMessage);

    /**
     * Encode a downlink command
     * @param deviceID DeviceID
     * @param commandName Command name
     * @param attribute Attribute which represents the command in Ngsi context broker
     * @return Hexadecimal payload to send
     */
    String encodeDataForCommand(String deviceID, String commandName, ContextAttribute attribute);
}
