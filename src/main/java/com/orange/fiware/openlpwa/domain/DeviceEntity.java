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

package com.orange.fiware.openlpwa.domain;

import com.orange.fiware.openlpwa.iotagent.Device;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Represents a devices collection
 */
@Document(collection = "devices")
public class DeviceEntity {

    @Id
    private String deviceEUI;
    private String name;
    private String type;
    private String subscriptionId;
    private int port;
    private List<String> commands;

    public DeviceEntity() {
    }

    public DeviceEntity(Device device, String subscriptionId) {
        this.deviceEUI = device.getDeviceEUI();
        this.name = device.getEntityName();
        this.type = device.getEntityType();
        this.port = device.getPort();
        this.commands = device.getCommands();
        this.subscriptionId = subscriptionId;
    }

    public String getDeviceEUI() {
        return deviceEUI;
    }

    public void setDeviceEUI(String deviceEUI) {
        this.deviceEUI = deviceEUI;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
