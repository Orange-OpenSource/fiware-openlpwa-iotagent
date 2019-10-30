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
 * * Created by Christophe AZEMAR on 15/09/2016.
 */

package com.orange.fiware.openlpwa.iotagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Manages convenience operations to register and unregister a device
 */
@RestController
@RequestMapping(value = {"/agent", "/AGENT"})
public class AgentRestController {

    private static final Logger logger = LoggerFactory.getLogger(AgentRestController.class);
    @Autowired
    private Agent agent;

    /**
     * Register a device
     * @param device Device to register
     * @return A <code>DeferredResult</code> which contains the response
     */
    @RequestMapping(method = RequestMethod.POST,
            value = "/devices",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    final public DeferredResult<ResponseEntity<?>> register(@RequestBody final Device device) {
        final DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        agent.register(device,
                () -> {
                    logger.debug("Device correctly registered ({})", device);
                    URI resourceLocation = null;
                    try {
                        if (device != null) {
                            resourceLocation = new URI(String.format("/devices/%s", device.getDeviceID()));
                        }
                    } catch (URISyntaxException ex) {
                        logger.warn("Invalid URI Location ({})", device);
                    }
                    if (resourceLocation != null) {
                        deferredResult.setResult(ResponseEntity.created(resourceLocation).build());
                    } else {
                        deferredResult.setResult(ResponseEntity.ok(""));
                    }
                },
                ex -> {
                    logger.error("Device not registered ({})", device, ex);
                    deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex));
                });
        return deferredResult;
    }

    /**
     * Unregister a device
     * @param deviceEUI Device EUI of the device to unregister
     * @return A <code>DeferredResult</code> which contains the response
     */
    @RequestMapping(method = RequestMethod.DELETE,
            value = "/devices/{deviceEUI}")
    final public DeferredResult<ResponseEntity<?>> unregister(@PathVariable String deviceEUI) {
        final DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();

        agent.unregister(deviceEUI,
                () -> {
                    logger.debug("Device correctly unregistered ({})", deviceEUI);
                    deferredResult.setResult(ResponseEntity.noContent().build());
                },
                ex -> {
                    logger.error("Device not unregistered ({})", deviceEUI, ex);
                    deferredResult.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex));
                });
        return deferredResult;
    }
}
