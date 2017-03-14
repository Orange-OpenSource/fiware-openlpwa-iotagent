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
 * * Created by François SUC on 28/06/2016.
 */

package com.orange.fiware.openlpwa.provider.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Command of a device in the GetDeviceCommands request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceCommand {

    /**
     * Enum to manage command status
     */
    public enum DeviceCommandStatus {

        SENT("SENT"),
        ERROR("ERROR");

        private final String value;

        DeviceCommandStatus(String value) {
            this.value = value;
        }
    }

    private String id;
    private String data;
    private Integer port;
    private Boolean confirmed;
    private DeviceCommandStatus commandStatus;
    private Date creationTs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public DeviceCommandStatus getCommandStatus() {
        return commandStatus;
    }

    public void setCommandStatus(DeviceCommandStatus commandStatus) {
        this.commandStatus = commandStatus;
    }

    public Date getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(Date creationTs) {
        this.creationTs = creationTs;
    }
}
