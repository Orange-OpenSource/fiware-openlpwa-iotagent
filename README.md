# fiware-openlpwa-genericagent
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

### Docker compose

Here is an example using docker-compose.yml:

```
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
      - LIVEOBJECTS_APIKEY=${LIVEOBJECTS_APIKEY}
      - LIVEOBJECTS_MQTT_URI=${LIVEOBJECTS_MQTT_URI:-ws://liveobjects.orange-business.com:80/mqtt}
      - LIVEOBJECTS_TOPICPATH=${LIVEOBJECTS_TOPICPATH}
    expose:
      - 8080
    command: mvn clean spring-boot:run```

