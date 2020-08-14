# fiware-openlpwa-genericagent

The fiware agent implements the OpenLpwaNgsiConverter interface provided by the Fiware LoRa®* IoT Agent library. It allows to genetically decode LoRa®* hexadecimal payloads.
## Configuration
### Using environment variables in GenericAgent configuration

Fiware OpenLpwa Generic Agent does support environment variables. Please configure these variables to make the agent work according to your needs.

| Name                  | Description                               | Default Value                                |
|-----------------------|-------------------------------------------|----------------------------------------------|
| ORION_URL             | URL to the remote broker (Orion)          | http://localhost:8082                        |
| LIVEOBJECTS_REST_URL  | Live Objects®** API URL                   | https://liveobjects.orange-business.com      |
| LIVEOBJECTS_APIKEY    | Live Objects®** API key                   |                                              |
| LIVEOBJECTS_MQTT_URI  | Live Objects®** MQTT broker URI           | ws://liveobjects.orange-business.com:80/mqtt |
| LIVEOBJECTS_TOPICPATH | OpenLpwa provider topic path subscription |                                              |
| REMOTE_AUTH_TOKEN     | OAuth token for secured remote broker     ||
| REMOTE_AUTH_TOKEN_URI | Uri used to retrieve the authentication token ||
| REMOTE_CLIENT_ID      | Remote context broker client ID           ||
| REMOTE_CLIENT_SECRET  | Remote context broker client secret       ||
| REMOTE_USER_LOGIN     | Remote context broker user login          ||
| REMOTE_USER_PASSWORD  | Remote context broker user password       ||
  
## Docker 

### How to generate a docker image 

In order to generate a docker image, you must use the build command by providing it the Dockerfile.

Inside the directory containing the docker-compose-.yml file ( usually at the root of the project), use the command :
```
docker-compose build
```



### How to user docker image 
In order to use the generic fiware agent, you have to fill in the environment variable properties as best you can.

Use this command :
```
docker run --name name
--env ORION_URL= 
--env LIVEOBJECTS_REST_URL= 
--env LIVEOBJECTS_APIKEY= 
--env LIVEOBJECTS_MQTT_URI= 
--env LIVEOBJECTS_TOPICPATH=
--env REMOTE_FIWARE_SERVICE= 
--env REMOTE_FIWARE_SERVICEPATH= 
--env REMOTE_AUTH_TOKEN= 
--env REMOTE_AUTH_TOKEN_URI=
--env REMOTE_CLIENT_ID=
--env REMOTE_CLIENT_SECRET=
--env REMOTE_USER_LOGIN=
--env REMOTE_USER_SECRET=
image
```

### Docker compose

Here is an example using docker-compose.yml:

version: '3.2'
services:
  fiware-openlpwa-genericagent:
    build: .
    working_dir: /
    volumes:
      - .:/fiware-openlpwa-genericagent
      - ~/.m2:/root/.m2
    environment:
      - ORION_URL=${ORION_URL:-http://localhost:8082}
      - LIVEOBJECTS_REST_URL=${LIVEOBJECTS_REST_URL:-https://liveobjects.orange-business.com}
      - REMOTE_AUTH_TOKEN=${REMOTE_AUTH_TOKEN}
      - REMOTE_AUTH_TOKEN_URI=${REMOTE_AUTH_TOKEN_URI}
      - REMOTE_CLIENT_ID=${REMOTE_CLIENT_ID}
      - REMOTE_CLIENT_SECRET=${REMOTE_CLIENT_SECRET}
      - REMOTE_USER_LOGIN=${REMOTE_USER_LOGIN}
      - REMOTE_USER_PASSWORD=${REMOTE_USER_PASSWORD}
      - REMOTE_FIWARE_SERVICE=${REMOTE_FIWARE_SERVICE}
      - REMOTE_FIWARE_SERVICEPATH=${REMOTE_FIWARE_SERVICEPATH}
      - LIVEOBJECTS_APIKEY=${LIVEOBJECTS_APIKEY}
      - LIVEOBJECTS_MQTT_URI=${LIVEOBJECTS_MQTT_URI:-ws://liveobjects.orange-business.com:80/mqtt}
      - LIVEOBJECTS_TOPICPATH=${LIVEOBJECTS_TOPICPATH}
    expose:
      - 8080
    command: mvn clean spring-boot:run```

