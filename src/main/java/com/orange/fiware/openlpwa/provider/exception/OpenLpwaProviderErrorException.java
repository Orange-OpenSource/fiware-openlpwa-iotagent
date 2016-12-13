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

package com.orange.fiware.openlpwa.provider.exception;

import com.orange.fiware.openlpwa.provider.OpenLpwaProviderError;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Exception triggered when the OpenLpwa provider API has returned an error
 */
public class OpenLpwaProviderErrorException extends HttpClientErrorException {

    private OpenLpwaProviderError error;
    private int errorCode;

    public OpenLpwaProviderErrorException(HttpStatus statusCode, OpenLpwaProviderError error) {
        super(statusCode);
        this.error = error;
    }

    public String getId() {
        if (error != null) {
            return error.getId();
        }
        return null;
    }

    public int getErrorCode() {
        if (error != null) {
            return error.getCode();
        }
        return -1;
    }

    @Override
    public String getMessage() {
        if (error != null) {
            return error.getMessage();
        }
        return null;
    }
}
