# Fiware LoRa®* IoT Agent
This project is a bridge between the Orange LoRa®* network and the OMA NGSIv1 protocol used by the Orion Context Broker as well as by other components of the FIWARE ecosystem.

## Overview
This IoT agent is a library that can be used for example in a SpringBoot application.

This agent allows to :
* retrieve uplink payloads from LoRa®* devices to update a NGSI Context Broker.

This library uses Live Objects® platform ([https://liveobjects.orange-business.com/#/liveobjects](https://liveobjects.orange-business.com/#/liveobjects)) to manage LoRa®* payloads.

NGSI v1 implementation is provided by the [Orange-OpenSource/fiware-ngsi-api](https://github.com/Orange-OpenSource/fiware-ngsi-api) library.

## Architecture
Here's the global architecture of the IoT agent using Live Objects® platform to manage LoRa®* device payloads:

Architecture

## Installation

### Requirements

* JAVA 8
* Maven (for build)
* A Live Objects® account

### From Maven
Create a Maven project with Spring Boot dependencies and our library :

```xml
<dependency>
    <groupId>com.orange.fiware</groupId>
    <artifactId>openlpwa-iotagent</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

where `X.Y.Z` is the version of the library to use (check git tags).

### Configuration
You can modify some application settings either adding :
* a `src/main/resources/application.properties` in your application.
* command line parameters when you launch the application.

This is a list of the application properties:

<table>
    <tr><th>Name</th><th>Description</th><th>Default Value</th></tr>
    <tr><td>contextBroker.localUrl</td><td>public URL to this instance</td><td>http://localhost:8081</td></tr>
    <tr><td>contextBroker.remoteUrl</td><td>URL to the remote broker (Orion)</td><td>http://localhost:8082</td></tr>
    <tr><td>contextBroker.remoteFiwareService</td><td>remote broker Service Name</td><td></td></tr>
    <tr><td>contextBroker.remoteFiwareServicePath</td><td>remote broker Service Path</td><td></td></tr>
    <tr><td>contextBroker.remoteAuthToken</td><td>OAuth token for secured remote broker</td><td></td></tr>
    <tr><td>contextBroker.remoteAuthTokenURI</td><td>Uri used to retrieve the authentication token</td><td></td></tr>
    <tr><td>contextBroker.remoteClientId</td><td>Remote context broker client ID</td><td></td></tr>
    <tr><td>contextBroker.remoteClientSecret</td><td>Remote context broker client secret</td><td></td></tr>
    <tr><td>contextBroker.remoteUserLogin</td><td>Remote context broker user login</td><td></td></tr>
    <tr><td>contextBroker.remoteUserPassword</td><td>Remote context broker user password</td><td></td></tr>
    <tr><td>openLpwaProvider.restUrl</td><td>Live Objects®** API URL</td><td>https://lpwa.liveobjects.orange-business.com</td></tr>
    <tr><td>openLpwaProvider.apiKey</td><td>Live Objects®** API key</td><td></td></tr>
    <tr><td>openLpwaProvider.mqttUri</td><td>Live Objects®** MQTT broker URI</td><td>tcp://liveobjects.orange-business.com:1883</td></tr>
    <tr><td>openLpwaProvider.mqttClientId</td><td>Live Objects®** MQTT broker client identifier</td><td>fiware-iotagent-client</td></tr>
</table>


## Usage
In order to use a LoRa®* device with the agent, please follow this steps:
* Register the device on the Live Objects® platform.
* Decode and encode the device payloads.
* Start the agent.

### Register the device on the Live Objects®** platform
Before starting to use the agent, you must register the device on the LPWA Live Objects® portal ([https://lpwa.liveobjects.orange-business.com](https://lpwa.liveobjects.orange-business.com)).
If your device is correctly registered and active, you should see upcoming payloads on the website.

### Create and feed a queue (FIFO) on the Live Objects®** platform
Data collected by Live Objects can be routed to message queues (fifo).
The creation of a routing rule is done in three steps:
1. Name of the rule
2. Selection criteria for messages to be routed
3. Routing mode (fifo)

You can create your FIFO and define your routing rules on Live Objects® portal ([https://lpwa.liveobjects.orange-business.com](https://lpwa.liveobjects.orange-business.com)).

### Decode and encode the device payloads
Live Objects® platform manages LoRa®* hexadecimal payloads. It's not possible to decode the payload on the platform so, you must do this process when you use the agent.

To do this job, you must implement `OpenLpwaNgsiConverter` interface with the two following methods:

```java
public class MyOpenLpwaNgsiConverter implements OpenLpwaNgsiConverter {
@Override
    public List<ContextAttribute> decodeData(String deviceID, String data) {
      // Decode the hexadecimal payload to build a list of NGSI context attributes
    }
```
The method `decodeData` is called for each uplink payload sent by a device. You must implement this method to decode a hexadecimal payload sent and build a list of NGSI context attributes in order to update the NGSI entity mapped with the device (see [Start the agent](#startAgent)).

```java
@Override
    public String encodeDataForCommand(String deviceID, String commandName, ContextAttribute attribute) {
        String data = attribute.getValue().toString();
        // Build an hexadecimal payload to send a command
    }
```

The method `encodeDataForCommand` is called for each downlink payload to send to a device (see [Send command to a device](#sendCommand)). You must implement this method to retrieve the command in a NGSI context attribute and to encode the corresponding hexadecimal payload to send to a device.

### Authentication with remote context broker

#### Non-authenticated mode 
Without a token or credentials filled in, the agent will send messages without trying to authenticate to the remote context broker.

#### Token authentication mode
With a token filled in, the agent will use this token to authenticate to the remote context broker before sending a message.
If the token is invalid, the agent will try to use the given credentials to generate a new one.

#### Credentials authentication mode
If the credentials are filled in, the agent will use them to generate a token from the given URI. 
As long as this token is valid, the agent will continue to use it. If the token is no longer valid, the agent will try to replace it with a new one, based on the given credentials.

### Start the agent
The library provides an `Agent` class to manage the agent. You should just call the `start` method passing 3 parameters:
* The first parameter is an instance of your custom converter.
* The second parameter is an `AgentSuccessCallback` called when the agent is correctly started.
* The third parameter is an `AgentFailureCallback` called when the agent is not correctly started.

Here's an example to start the agent:

```java
@SpringBootApplication
@ComponentScan("com.orange")
public class Application implements ApplicationListener<ApplicationReadyEvent> {
    private static Logger logger = LoggerFactory.getLogger(Application.class);
    @Autowired
    private Agent agent;

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(Application.class)
                .bannerMode(Banner.Mode.LOG)
                .run(args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        agent.start(new MyOpenLpwaNgsiConverter(),
                () -> {
                    logger.debug("IoT agent started");
                },
                ex -> {
                    logger.error("IoT agent not started", ex);
                }
        );
    }
}
```

Under the hood, the agent will connect to the Live Objects® MQTT broker to retrieve uplink payloads during this step.

## License
This project is under the Apache License version 2.0.

* brand registered by Semtech Corporation
 brand registered by Orange
