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
 * * Created by François SUC on 03/07/2016.
 */

package com.orange.fiware.openlpwa.configuration;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB configuration
 */
@Configuration
@EnableMongoRepositories("com.orange.fiware.openlpwa.repository")
class MongoConfiguration extends AbstractMongoConfiguration {

    @Value("${mongodb.host}")
    private String mongoHost;
    @Value("${mongodb.port}")
    private int mongoPort;
    @Value("${mongodb.databasename}")
    private String mongoDatabasename;
    @Value("${mongodb.username}")
    private String username;
    @Value("${mongodb.password}")
    private String password;

    @Override
    protected String getDatabaseName() {
        return mongoDatabasename;
    }

    @Bean
    public MongoClient mongoClient() {
        if (username != null && password != null && username.length() > 0) {
            ServerAddress address = new ServerAddress(mongoHost, mongoPort);
            MongoCredential credential = MongoCredential.createCredential(username, mongoDatabasename, password.toCharArray());
            List<MongoCredential> credentials = new ArrayList<>();
            credentials.add(credential);
            return new MongoClient(address, credentials);
        }
        return new MongoClient(mongoHost, mongoPort);
    }
}
