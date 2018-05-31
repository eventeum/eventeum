# Eventeum
A bridge between your Ethereum smart contract events and backend microservices.  Eventeum listens for specified event emissions from the Ethereum network, and broadcasts these events into your middleware layer.  This provides a distinct seperation of concerns and means that your microservices do not have to subscribe to events directly to an Ethereum node.

## Features
* Dynamically Configurable - Eventeum exposes a REST api so that smart contract events can be dynamically subscribed / unsubscribed.

* Highly Available - Eventeum instances communicate with each other to ensure that every instance is subscribed to the same collection of smart contract events.

* Resilient - Node failures are detected and event subscriptions will continue from the failure block once the node comes back online.

* Fork Tolerance - Eventeum can be configured to wait a certain amount of blocks before an event is considered 'Confirmed'.  If a fork occurs during this time, a message is broadcast to the network, allowing your services to react to the forked/removed event.

## Supported Broadcast Mechanisims
* Kafka

## Getting Started
Follow the instructions below in order to run Eventeum on your local machine for development and testing purposes.

### Prerequisites
* Java 8
* Maven
* Docker (optional)

### Build
1. After checking out the code, navigate to the root directory
```
$ cd /path/to/eventeum/
```

2. Compile, test and package the project
```
$ mvn clean package
```

3. Run the project

a. If you have a running instance of MongoDB, Kafka, Zookeeper and an Ethereum node:

**Executable JAR:**
```
$ export SPRING_DATA_MONGODB_HOST=<mongodb-host:port>
$ export ETHEREUM_NODE_URL=http://<node-host:port>
$ export ZOOKEEPER_ADDRESS=<zookeeper-host:port>
$ export KAFKA_ADDRESSES=<kafka-host:port>

$ java -jar target/eventeum.jar
```

**Docker:**

```
$ docker build  . -t kauri/eventeum:latest

$ export SPRING_DATA_MONGODB_HOST=<mongodb-host:port>
$ export ETHEREUM_NODE_URL=http://<node-host:port>
$ export ZOOKEEPER_ADDRESS=<zookeeper-host:port>
$ export KAFKA_ADDRESSES=<kafka-host:port>

$ docker run -p 8060:8060 kauri/eventeum
```

b. If you prefer build an all-in-one test environment with a parity dev node, use docker-compose:
```
$ docker-compose -f docker-compose.yml build
$ docker-compose -f docker-compose.yml up
```
