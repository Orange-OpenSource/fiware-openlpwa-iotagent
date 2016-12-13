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
 * * Created by François SUC on 10/09/2016.
 */

package com.orange.fiware.openlpwa.exception;

/**
 * Exception triggered when an error occurs in the agent
 */
public class AgentException extends Exception {
    
    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
