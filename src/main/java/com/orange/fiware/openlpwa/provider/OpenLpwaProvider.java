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

package com.orange.fiware.openlpwa.provider;

import com.orange.fiware.openlpwa.exception.ConfigurationException;
import com.orange.fiware.openlpwa.provider.model.DeviceCommand;
import com.orange.fiware.openlpwa.provider.model.DeviceInfo;
import com.orange.fiware.openlpwa.provider.model.RegisterDeviceCommandParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

/**
 * Provides methods to request OpenLpwa provider API
 */
@Service
public class OpenLpwaProvider {

    private final static String basePath = "api/v0";
    private final static String HTTP_HEADER_APIKEY = "X-API-Key";
    private String url;
    private String apiKey;
    private AsyncRestTemplate asyncRestTemplate;

    public OpenLpwaProvider() {
        asyncRestTemplate = new AsyncRestTemplate();
        // Replace the default response handler to manage OpenLpwa provider API errors
        asyncRestTemplate.setErrorHandler(new OpenLpwaResponseErrorHandler());
    }

    @Autowired
    public OpenLpwaProvider(@Value("${openLpwaProvider.restUrl}") String url, @Value("${openLpwaProvider.apiKey}") String apiKey) {
        this();
        this.url = url;
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get device information
     *
     * @param deviceEUI End device identifier
     * @return A future for DeviceInfo
     * @throws ConfigurationException A configuration problem occurred
     */
    public ListenableFuture<DeviceInfo> getDeviceInformation(String deviceEUI) throws ConfigurationException {
        checkOpenLpwaProviderInitialization();
        checkDeviceEUIParameter(deviceEUI);

        UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromUriString(url);
        componentsBuilder.path(String.format("%1$s/data/streams/urn:lo:nsid:lora:%2$s", basePath, deviceEUI));
        componentsBuilder.build();

        return request(HttpMethod.GET, componentsBuilder.toUriString(), null, DeviceInfo.class);
    }

    /**
     * Register a command for a device
     *
     * @param deviceEUI              End device identifier
     * @param deviceCommandParameter Command parameters (data, port, confirmed)
     * @return A future for DeviceCommand
     * @throws ConfigurationException A configuration problem occurred
     */
    public ListenableFuture<DeviceCommand> registerDeviceCommand(String deviceEUI, RegisterDeviceCommandParameter deviceCommandParameter) throws ConfigurationException {
        checkOpenLpwaProviderInitialization();
        checkRegisterDeviceCommandParameters(deviceEUI, deviceCommandParameter);

        UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromUriString(url);
        componentsBuilder.path(String.format("%1$s/vendors/lora/devices/%2$s/commands", basePath, deviceEUI));
        componentsBuilder.build();

        return request(HttpMethod.POST, componentsBuilder.toUriString(), deviceCommandParameter, DeviceCommand.class);
    }

    /**
     * Build common Http headers
     *
     * @param requestHasBody <code>true</code> if a request has a body
     * @return http's headers
     */
    private HttpHeaders getHttpHeaders(Boolean requestHasBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (requestHasBody) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        headers.add(HTTP_HEADER_APIKEY, apiKey);
        return headers;
    }

    /**
     * Build an Http request
     *
     * @param method       Http method
     * @param url          Url
     * @param body         Request body
     * @param responseType Response type
     * @param <T>          Type to deserialize for the response
     * @param <U>          Type to serialize for the body
     * @return              Listenable for request
     */
    private <T, U> ListenableFuture<T> request(HttpMethod method, String url, U body, Class<T> responseType) {
        HttpEntity<U> requestEntity = new HttpEntity<>(body, getHttpHeaders(body != null));
        ListenableFuture<ResponseEntity<T>> future = asyncRestTemplate.exchange(url, method, requestEntity, responseType);
        return new ListenableFutureAdapter<T, ResponseEntity<T>>(future) {
            @Override
            protected T adapt(ResponseEntity<T> result) throws ExecutionException {
                return result.getBody();
            }
        };
    }

    /**
     * Check mandatory properties to use the class properly
     *
     * @throws ConfigurationException The class is not initialized properly
     */
    private void checkOpenLpwaProviderInitialization() throws ConfigurationException {
        if (url == null || url.isEmpty()) {
            throw new ConfigurationException("OpenLpwa provider URL is missing.");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new ConfigurationException("OpenLpwa provider API key is missing.");
        }
    }

    /**
     * Check deviceEUI parameter
     *
     * @throws ConfigurationException A mandatory method parameter is missing
     */
    private void checkDeviceEUIParameter(String deviceEUI) throws ConfigurationException {
        if (deviceEUI == null || deviceEUI.isEmpty()) {
            throw new ConfigurationException("deviceEUI parameter is missing.");
        }
    }

    /**
     * Check parameters for registerDeviceCommand method
     *
     * @throws ConfigurationException A mandatory method parameter is missing
     */
    private void checkRegisterDeviceCommandParameters(String deviceEUI, RegisterDeviceCommandParameter parameter) throws ConfigurationException {
        checkDeviceEUIParameter(deviceEUI);

        if (parameter.getData() == null || parameter.getData().isEmpty()) {
            throw new ConfigurationException("data parameter is missing.");
        }

        if (parameter.getPort() == null) {
            throw new ConfigurationException("port parameter is missing.");
        }
    }
}
