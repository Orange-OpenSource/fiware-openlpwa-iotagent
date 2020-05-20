/**
 * Copyright (C) 2020 Orange
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
 */

package com.orange.fiware.genericagent;

import com.orange.fiware.openlpwa.iotagent.OpenLpwaNgsiConverter;
import com.orange.fiware.openlpwa.provider.model.DeviceIncomingMessage;
import com.orange.ngsi.model.ContextAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GenericOpenLpwaNgsiConverter implements OpenLpwaNgsiConverter {

    // logs display
    private static Logger logger = LoggerFactory.getLogger(GenericOpenLpwaNgsiConverter.class);

    @Override
    public List<ContextAttribute> decodeData(String deviceEUI, String data) {
        return null;
    }

    @Override
    public List<ContextAttribute> decodeData(String deviceEUI, String data, DeviceIncomingMessage incomingMessage) {

        List<ContextAttribute> contextAttributeList = new ArrayList<>();
        Map<String, Object> mapValues = incomingMessage.getValue();

        // (Date) CreatedAt
        ContextAttribute createdAt = new ContextAttribute(
                "createdAt",
                "DateTime",
                incomingMessage.getDate().toInstant().toString());
        contextAttributeList.add(createdAt);

        // (Date) ModifiedAt
        ContextAttribute modifiedAt = new ContextAttribute(
                "modifiedAt",
                "DateTime",
                new Date().toInstant().toString());
        contextAttributeList.add(modifiedAt);

        // Location
        if (incomingMessage.getLocation() != null) {
            ContextAttribute location = new ContextAttribute();
            location.setName("location");
            location.setType("geo:json");
            location.setValue(buildJsonLocation(incomingMessage.getLocation()));
            contextAttributeList.add(location);
        }

        // Name
        if (!incomingMessage.getTags().isEmpty()) {
            ContextAttribute name = new ContextAttribute(
                    "name",
                    "Text",
                    incomingMessage.getTags().get(0));
        contextAttributeList.add(name);
    }


        // --- VALUES DATA ---

        for (Map.Entry<String, Object> entry : mapValues.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String valueType = null;

            if (value instanceof Integer)
                valueType = "Integer";
            else if (value instanceof Double) {
                valueType = "Integer";
                value = ((Double) value).intValue() ;
            }
            else if (value instanceof String)
                valueType = "Text";
            else if (value instanceof Boolean)
                valueType = "Boolean";
            else
                logger.debug("Unsupported value type for value: " + key + "=" + value);
            contextAttributeList.add(new ContextAttribute(key, valueType, value));
        }

        return contextAttributeList;
    }

    @Override
    public String encodeDataForCommand(String deviceEUI, String commandName, ContextAttribute contextAttribute) {
        return contextAttribute.getValue().toString();
    }

    private Point buildJsonLocation(DeviceIncomingMessage.DeviceIncomingMessageLocation location) {
        Float[] arrayCoord = {location.getLat(), location.getLon()};
        return new Point("Point", arrayCoord);
    }

    public class Point {
        private String type;
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
