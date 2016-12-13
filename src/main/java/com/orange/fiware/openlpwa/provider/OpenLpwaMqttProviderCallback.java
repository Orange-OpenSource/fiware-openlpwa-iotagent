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

import com.orange.fiware.openlpwa.provider.model.DeviceIncomingMessage;

/**
 * Provides interface to launch Mqtt events
 */
public interface OpenLpwaMqttProviderCallback {

    /**
     * The connection between the client and the server is lost
     * @param throwable     The reason behind the loss of connection
     */
    void connectionLost(Throwable throwable);

    /**
     * A new message is arrived
     * @param deviceEUI         End device Identifier of the device concerned by the message
     * @param incomingMessage   Message (may be null if the deserialization fails)
     */
    void newMessageArrived(String deviceEUI, DeviceIncomingMessage incomingMessage);
}
