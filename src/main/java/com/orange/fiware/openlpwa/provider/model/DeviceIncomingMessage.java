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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * Incoming message from MQTT broker for a device
 */
public class DeviceIncomingMessage {

    private String streamId;
    @JsonProperty("timestamp")
    private Date date;
    private String model;
    private List<String> tags;
    private DeviceIncomingMessageValue value;
    private DeviceIncomingMessageMetadata metadata;

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

    public DeviceIncomingMessageValue getValue() {
        return value;
    }

    public void setValue(DeviceIncomingMessageValue value) {
        this.value = value;
    }

    public DeviceIncomingMessageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(DeviceIncomingMessageMetadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Incoming message information
     */
    public class DeviceIncomingMessageValue {

        private Integer port;
        @JsonProperty("fcnt")
        private Integer frameCount;
        private Integer signalLevel;
        @JsonProperty("payload")
        private String data;

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public Integer getFrameCount() {
            return frameCount;
        }

        public void setFrameCount(Integer frameCount) {
            this.frameCount = frameCount;
        }

        public Integer getSignalLevel() {
            return signalLevel;
        }

        public void setSignalLevel(Integer signalLevel) {
            this.signalLevel = signalLevel;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    /**
     * Metadata
     */
    public class DeviceIncomingMessageMetadata {

        private String source;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}