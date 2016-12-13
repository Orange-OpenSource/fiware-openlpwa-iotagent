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
 * * Created by François SUC on 21/07/2016.
 */

package com.orange.fiware.openlpwa.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orange.fiware.openlpwa.provider.exception.OpenLpwaProviderErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

/**
 * Class to manage OpenLpwa provider API errors with a specific Json stream
 */
public class OpenLpwaResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
        HttpStatus statusCode = clientHttpResponse.getStatusCode();
        // OpenLpwa provider API only returns JSON with error when the status code is 400 or 404
        if (statusCode == HttpStatus.BAD_REQUEST || statusCode == HttpStatus.NOT_FOUND) {
            // Get the response body to deserialize it in OpenLpwaProviderError object
            OpenLpwaProviderError error = null;
            try {
                error = new ObjectMapper().readValue(clientHttpResponse.getBody(), OpenLpwaProviderError.class);
            } catch (Exception e) {
            }

            if (error != null) {
                throw new OpenLpwaProviderErrorException(statusCode, error);
            }
        }

        // If it's not a 4xx error or the serialization has failed, fallback to the default handler process
        super.handleError(clientHttpResponse);
    }
}
