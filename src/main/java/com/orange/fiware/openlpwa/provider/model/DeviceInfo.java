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
 * * Created by François SUC on 28/07/2016.
 */

package com.orange.fiware.openlpwa.provider.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * Device Info in GetDeviceInformation request
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceInfo {

    /**
     * Enum to manage activation type
     */
    public enum DeviceActivationType {

        OTAA("OTAA"),
        ABP("ABP");

        private final String value;

        DeviceActivationType(String value) {
            this.value = value;
        }
    }

    /**
     * Enum to manage device status
     */
    public enum DeviceStatus {

        ACTIVATED("ACTIVATED"),
        DEACTIVATED("DEACTIVATED");

        private final String value;

        DeviceStatus(String value) {
            this.value = value;
        }
    }

    @JsonProperty("devEUI")
    private String deviceEUI;
    private String name;
    private DeviceActivationType activationType;
    private String profile;
    private DeviceStatus deviceStatus;
    private String appEUI;
    private List<String> tags;
    private Date lastActivationTs;
    private Date lastDeactivationTs;
    private Date lastCommunicationTs;
    private Integer lastSignalLevel;
    private Integer lastDlFcnt;
    private Date creationTs;
    private Date updateTs;

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

    public DeviceActivationType getActivationType() {
        return activationType;
    }

    public void setActivationType(DeviceActivationType activationType) {
        this.activationType = activationType;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public DeviceStatus getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public String getAppEUI() {
        return appEUI;
    }

    public void setAppEUI(String appEUI) {
        this.appEUI = appEUI;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Date getLastActivationTs() {
        return lastActivationTs;
    }

    public void setLastActivationTs(Date lastActivationTs) {
        this.lastActivationTs = lastActivationTs;
    }

    public Date getLastDeactivationTs() {
        return lastDeactivationTs;
    }

    public void setLastDeactivationTs(Date lastDeactivationTs) {
        this.lastDeactivationTs = lastDeactivationTs;
    }

    public Date getLastCommunicationTs() {
        return lastCommunicationTs;
    }

    public void setLastCommunicationTs(Date lastCommunicationTs) {
        this.lastCommunicationTs = lastCommunicationTs;
    }

    public Integer getLastSignalLevel() {
        return lastSignalLevel;
    }

    public void setLastSignalLevel(Integer lastSignalLevel) {
        this.lastSignalLevel = lastSignalLevel;
    }

    public Integer getLastDlFcnt() {
        return lastDlFcnt;
    }

    public void setLastDlFcnt(Integer lastDlFcnt) {
        this.lastDlFcnt = lastDlFcnt;
    }

    public Date getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(Date creationTs) {
        this.creationTs = creationTs;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }
}
