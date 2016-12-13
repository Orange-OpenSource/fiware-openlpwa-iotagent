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
 * * Created by Christophe AZEMAR on 28/06/2016.
 */

package com.orange.fiware.openlpwa.iotagent;

import com.orange.fiware.openlpwa.domain.DeviceEntity;
import com.orange.fiware.openlpwa.exception.AgentException;
import com.orange.fiware.openlpwa.ngsi.NgsiManager;
import com.orange.fiware.openlpwa.repository.DeviceEntityRepository;
import com.orange.ngsi.model.*;
import com.orange.ngsi.server.NgsiRestBaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Manages notifyContext callback
 */
@RestController
@RequestMapping(value = {"/v1", "/ngsi10", "/NGSI10"})
public class NgsiRestController extends NgsiRestBaseController {

    private static Logger logger = LoggerFactory.getLogger(NgsiRestController.class);
    @Autowired
    private NgsiManager manager;
    @Autowired
    private Agent agent;
    @Autowired
    private DeviceEntityRepository deviceRepository;

    /**
     * Call /notifyContext
     * @param notify NotifyContext
     * @param deviceEUI Device EUI of the device
     * @param httpServletRequest Servlet request
     * @return Response
     * @throws Exception
     */
    @RequestMapping(value = "/notifyContext/{deviceEUI}", method = RequestMethod.POST, consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    final public ResponseEntity<NotifyContextResponse> notifyContextRequest(@RequestBody final NotifyContext notify, @PathVariable String deviceEUI, HttpServletRequest httpServletRequest) throws Exception {
        logger.debug("Receive /notifyContext for deviceEUI:{} with content:{}", deviceEUI, notify);
        NotifyContextResponse response = new NotifyContextResponse();
        if (notify == null || notify.getContextElementResponseList() == null) {
            response.setResponseCode(new StatusCode(CodeEnum.CODE_400));
        } else {
            response.setResponseCode(new StatusCode(CodeEnum.CODE_200));
            // Check if subscriptionId is still valid for agent and device
            DeviceEntity deviceRegistered = deviceRepository.findOne(deviceEUI);
            if (deviceRegistered != null
                    && !deviceRegistered.getSubscriptionId().equalsIgnoreCase(notify.getSubscriptionId())) {
                logger.debug("Receive a notify for a non-valid subscriptionId, call unsubscribe (subscriptionId:{})", notify.getSubscriptionId());
                manager.unsubscribe(notify.getSubscriptionId());
            } else if (deviceRegistered != null) {
                notify.getContextElementResponseList()
                        .stream()
                        .filter(elementResponse -> elementResponse.getStatusCode().getCode().equals(CodeEnum.CODE_200.getLabel()))
                        .forEach(elementResponse -> handleNotifyContext(deviceEUI, elementResponse));
            } else {
                logger.warn("Receive a notify for a non registered device (EUI:{}, notify:{})", deviceEUI, notify);
                logger.debug("Unsubscribe for a non registered device (EUI:{}, subscriptionId:{})", deviceEUI, notify.getSubscriptionId());
                manager.unsubscribe(notify.getSubscriptionId());
            }
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Treat a notifyContext for a device
     * @param deviceEUI Device EUI of the device
     * @param contextElementResponse Response
     * @throws Exception
     */
    private void handleNotifyContext(String deviceEUI,
                                     ContextElementResponse contextElementResponse) {
        List<ContextAttribute> commandAttributes =
                contextElementResponse.getContextElement().getContextAttributeList().stream()
                        .filter(contextAttribute -> contextAttribute.getName().endsWith(NgsiManager.COMMAND_SUFFIX))
                        .collect(Collectors.toList());
        commandAttributes.forEach(contextAttribute -> contextAttribute.setName(contextAttribute.getName().replaceAll(NgsiManager.COMMAND_SUFFIX, "")));

        // Build list of commands to launch
        List<ContextAttribute> commandsToLaunch = new ArrayList<>();
        for (ContextAttribute attribute : commandAttributes) {
            if (attribute.getName() != null && attribute.getValue() != null) {
                commandsToLaunch.add(attribute);
            } else {
                logger.warn("Ignore notify because of a not valid attribute(name:{}, value:{})", attribute.getName(), attribute.getValue());
            }
        }
        // Launch Commands
        List<ContextAttribute> copyCommands = new ArrayList<>(commandsToLaunch);
        List<ContextAttribute> attributeList = new ArrayList<>();

        for (ContextAttribute attribute : copyCommands) {
            agent.executeCommand(deviceEUI, attribute.getName(), attribute,
                    (success, creationDate) -> {
                        // create context attribute ngsi
                        ContextAttribute responseAttribute = new ContextAttribute();
                        responseAttribute.setName(attribute.getName() + NgsiManager.COMMAND_STATUS_SUFFIX);
                        responseAttribute.setValue(success ? NgsiManager.COMMAND_SENT : NgsiManager.COMMAND_ERROR);
                        responseAttribute.setType("commandStatus");
                        ContextMetadata metadata = new ContextMetadata();
                        metadata.setName("commandDate");
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        metadata.setValue(dateFormat.format(creationDate));
                        metadata.setType("date");
                        responseAttribute.addMetadata(metadata);
                        attributeList.add(responseAttribute);
                        // remove commands
                        commandsToLaunch.remove(attribute);
                        if (commandsToLaunch.isEmpty()) {
                            logger.debug("All commands launched, call updateContext.");
                            try {
                                manager.updateDeviceAttributes(contextElementResponse.getContextElement().getEntityId(), attributeList);
                            } catch (AgentException e) {
                                logger.error("Unable to send update command state to broker (entityId:{}, attributes:{})",
                                        contextElementResponse.getContextElement().getEntityId(),
                                        attributeList);
                            }
                        }
                    });
        }
    }
}
