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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Incoming message from MQTT broker for a device
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceIncomingMessage {

    private String streamId;
    @JsonProperty("timestamp")
    private Date date;
    private String model;
    private List<String> tags;
    private DeviceIncomingMessageLocation location;
    private DeviceIncomingMessageMetadata metadata;
    private Map<String, Object> value;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getValue() {
        return value;
    }

    public void setValue(Map<String, Object> value) {
        this.value = value;
    }

    public DeviceIncomingMessageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DeviceIncomingMessageMetadata metadata) {
        this.metadata = metadata;
    }

    public DeviceIncomingMessageLocation getLocation() {
        return location;
    }

    public void setLocation(DeviceIncomingMessageLocation location) {
        this.location = location;
    }

    public String getData() {
        return (String) value.get("payload");
    }

    public void setData(String data) {
        this.value.put("patload", data);
    }

    /**
     * Metadata
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class DeviceIncomingMessageMetadata {

        private String source;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }

    /**
     * Location
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class DeviceIncomingMessageLocation {

        private Float lat;
        private Float lon;
        private int alt;
        private int accuracy;
        private String provider;

        public Float getLat() {
            return lat;
        }

        public Float getLon() {
            return lon;
        }

        public int getAlt() {
            return alt;
        }

        public int getAccuracy() {
            return accuracy;
        }

        public String getProvider() {
            return provider;
        }

        public void setLat(Float lat) {
            this.lat = lat;
        }

        public void setLon(Float lon) {
            this.lon = lon;
        }

        public void setAlt(int alt) {
            this.alt = alt;
        }

        public void setAccuracy(int accuracy) {
            this.accuracy = accuracy;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }
}