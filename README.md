# Eventeum
An Ethereum event listener that bridges your smart contract events and backend microservices. Eventeum listens for specified event emissions from the Ethereum network, and broadcasts these events into your middleware layer. This provides a distinct separation of concerns and means that your microservices do not have to subscribe to events directly to an Ethereum node.

[![Gitter](https://badges.gitter.im/eventeum/community.svg)](https://gitter.im/eventeum/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)

**Master**

[![CircleCI](https://circleci.com/gh/ConsenSys/eventeum/tree/master.svg?style=svg)](https://circleci.com/gh/ConsenSys/eventeum/tree/master)

**Development**

[![CircleCI](https://circleci.com/gh/ConsenSys/eventeum/tree/development.svg?style=svg)](https://circleci.com/gh/ConsenSys/eventeum/tree/development)

## Features
* Dynamically Configurable - Eventeum exposes a REST api so that smart contract events can be dynamically subscribed / unsubscribed.

* Highly Available - Eventeum instances communicate with each other to ensure that every instance is subscribed to the same collection of smart contract events.

* Resilient - Node failures are detected and event subscriptions will continue from the failure block once the node comes back online.

* Fork Tolerance - Eventeum can be configured to wait a certain amount of blocks before an event is considered 'Confirmed'.  If a fork occurs during this time, a message is broadcast to the network, allowing your services to react to the forked/removed event.

## Supported Broadcast Mechanisms
* Kafka
* HTTP Post
* [RabbitMQ](https://www.rabbitmq.com/)
* [Pulsar](https://pulsar.apache.org)


For RabbitMQ, you can configure the following extra values

* rabbitmq.blockNotification. true|false
* rabbitmq.routingKey.contractEvents
* rabbitmq.routingKey.blockEvents
* rabbitmq.routingKey.transactionEvents

## Eventeum Tutorials
- [Listening to Ethereum Events](https://kauri.io/article/90dc8d911f1c43008c7d0dfa20bde298/listening-to-ethereum-events-with-eventeum)
- [Listening for Ethereum Transactions](https://kauri.io/article/3e31587c96a74d24b5cdd17952d983e9/v1/listening-for-ethereum-transactions-with-eventeum)
- [Using Eventeum to Build a Java Smart Contract Data Cache](https://kauri.io/article/fe81ee9612eb4e5a9ab72790ef24283d/using-eventeum-to-build-a-java-smart-contract-data-cache)

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

### Run

a. If you have a running instance of MongoDB, Kafka, Zookeeper and an Ethereum node:

**Executable JAR:**

```sh
$ cd server
$ export SPRING_DATA_MONGODB_HOST=<mongodb-host:port>
$ export ETHEREUM_NODE_URL=http://<node-host:port>
$ export ZOOKEEPER_ADDRESS=<zookeeper-host:port>
$ export KAFKA_ADDRESSES=<kafka-host:port>
$ export RABBIT_ADDRESSES=<rabbit-host:port>

$ java -jar target/eventeum-server.jar
```

**Docker:**

```sh
$ cd server
$ docker build  . -t kauri/eventeum:latest

$ export SPRING_DATA_MONGODB_HOST=<mongodb-host:port>
$ export ETHEREUM_NODE_URL=http://<node-host:port>
$ export ZOOKEEPER_ADDRESS=<zookeeper-host:port>
$ export KAFKA_ADDRESSES=<kafka-host:port>
$ export RABBIT_ADDRESSES=<rabbit-host:port>

$ docker run -p 8060:8060 kauri/eventeum
```

b. If you prefer build an all-in-one test environment with a parity dev node, use docker-compose:

```sh
$ cd server
$ docker-compose -f docker-compose.yml build
$ docker-compose -f docker-compose.yml up
```

## SQL Support
Eventeum now supports a SQL database as well as the default MongoDB.  To use a SQL database (only SQL Server has currently been tested but others should be supported with the correct config), set the `database.type` property to `SQL` and ensure you have all required additional properties in your properties file. See `config-examples/application-template-sqlserver.yml` for a sample SQLServer configuration.

### Upgrading to 0.8.0

When upgrading Eventeum to **0.8.0**, changes in the schema are required. In order to perform the migration follow these steps:

1. Stop all Evnteum instances
2. Backup your database
3. Apply the [tools/potgres-upgrade-to-v0.8.0.sql](tools/postgres-upgrade-to-v0.8.0.sql) sql script. Note that this script is written for Postgres, syntax may differ if using other database system.
4. Restart Eventeum instances


## Configuring Nodes
Listening for events from multiple different nodes is supported in Eventeum, and these nodes can be configured in the properties file.

```yaml
ethereum:
  nodes:
    - name: default
      url: http://mainnet:8545
    - name: sidechain
      url: wss://sidechain/ws
```

If an event does not specify a node, then it will be registered against the 'default' node.

That is the simplest node configuration, but there is other custom flags you can activate per node:


- `maxIdleConnections`: Maximum number of connections to the node. (default: 5)
- `keepAliveDuration`: Duration of the keep alive http in milliseconds (default: 10000)
- `connectionTimeout`: Http connection timeout to the node in milliseconds (default: 5000)
- `readTimeout`: Http read timeout to the node in milliseconds (default: 60000)
- `addTransactionRevertReason`: Enables receiving the revert reason when a transaction fails.  (default: false)
- `pollInterval`: Polling interval of the rpc request to the node (default: 10000)
- `healthcheckInterval`: Polling interval of that evenreum will use to check if the node is active (default: 10000)
- `numBlocksToWait`: Blocks to wait until we decide event is confirmed (default: 1). Overrides broadcaster config
- `numBlocksToWaitBeforeInvalidating`:  Blocks to wait until we decide event is invalidated (default: 1).  Overrides broadcaster config
- `numBlocksToWaitForMissingTx`: Blocks to wait until we decide tx is missing (default: 1)  Overrides broadcaster config

This will be an example with a complex configuration:

```yaml
ethereum:
  nodes:
  - name: default
    url: http://mainnet:8545
    pollInterval: 1000
    maxIdleConnections: 10
    keepAliveDuration: 15000
    connectionTimeout: 7000
    readTimeout: 35000
    healthcheckInterval: 3000
    addTransactionRevertReason: true
    numBlocksToWait: 1
    numBlocksToWaitBeforeInvalidating: 1
    numBlocksToWaitForMissingTx: 1
  blockStrategy: POLL

```

## Registering Events

### REST
Eventeum exposes a REST api that can be used to register events that should be subscribed to / broadcast.

-   **URL:** `/api/rest/v1/event-filter`    
-   **Method:** `POST`
-   **Headers:**  

| Key | Value |
| -------- | -------- |
| content-type | application/json |

-   **URL Params:** `N/A`
-   **Body:**

```json
{
	"id": "event-identifier",
	"contractAddress": "0x1fbBeeE6eC2B7B095fE3c5A572551b1e260Af4d2",
	"eventSpecification": {
		"eventName": "TestEvent",
		"indexedParameterDefinitions": [
		  {"position": 0, "type": "UINT256"},
		  {"position": 1, "type": "ADDRESS"}],
		"nonIndexedParameterDefinitions": [
		  {"position": 2, "type": "BYTES32"},
		  {"position": 3, "type": "STRING"}] },
	"correlationIdStrategy": {
		"type": "NON_INDEXED_PARAMETER",
		"parameterIndex": 0 }
}
```
| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| id | String | no | Autogenerated | A unique identifier for the event. |
| contractAddress | String | yes |  | The address of the smart contract that the address will be emitted from. |
| eventSpecification | json | yes |  | The event specification |
| correlationIdStrategy | json | no | null | Define a correlation id for the event (only used with the Kafka broadcaster).  See the advanced section for details. |

**eventSpecification**:

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| eventName | String | yes | | The event name within the smart contract |
| indexedParameterTypes | String array | no | null | The array of indexed parameter types for the event. |
| nonIndexedParameterTypes | String array | no | null | The array of non-indexed parameter types for the event. |

**parameterDefinition**:

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| position | Number | yes | | The zero indexed position of the parameter within the event specification |
| type | String | yes | | The type of the event parameter. |

Currently supported parameter types: `UINT8-256`, `INT8-256`, `ADDRESS`, `BYTES1-32`, `STRING`, `BOOL`.

Dynamically sized arrays are also supported by suffixing the type with `[]`, e.g. `UINT256[]`.

**correlationIdStrategy**:

| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| type | String | yes | | The correlation id strategy type. |
| parameterIndex | Number | yes | | The parameter index to use within the correlation strategy. |

-   **Success Response:**
    -   **Code:** 200  
        **Content:**

```json
{
    "id": "event-identifier"
}
```

### Hard Coded Configuration
Static events can be configured within the application.yml file of Eventeum.

```yaml
eventFilters:
  - id: RequestCreated
    contractAddress: ${CONTRACT_ADDRESS:0x4aecf261541f168bb3ca65fa8ff5012498aac3b8}
    eventSpecification:
      eventName: RequestCreated
      indexedParameterDefinitions:
        - position: 0
          type: BYTES32
        - position: 1
          type: ADDRESS
      nonIndexedParameterDefinitions:
        - position: 2
          type: BYTES32
    correlationId:
      type: NON_INDEXED_PARAMETER
      index: 0
```

## Un-Registering Events

### REST

-   **URL:** `/api/rest/v1/event-filter/{event-id}`    
-   **Method:** `DELETE`
-   **Headers:**  `N/A`
-   **URL Params:** `N/A`
-   **Body:** `N/A`

-   **Success Response:**
    -   **Code:** 200
        **Content:** `N/A`

## Listing Registered Events

### REST

-   **URL:** `/api/rest/v1/event-filter`    
-   **Method:** `GET`
-   **Headers:**  

| Key | Value |
| -------- | -------- |
| accept | application/json |

-   **URL Params:** `N/A`

-   **Response:** List of contract event filters:
```json
[{
	"id": "event-identifier-1",
	"contractAddress": "0x1fbBeeE6eC2B7B095fE3c5A572551b1e260Af4d2",
	"eventSpecification": {
		"eventName": "TestEvent",
		"indexedParameterDefinitions": [
		  {"position": 0, "type": "UINT256"},
		  {"position": 1, "type": "ADDRESS"}],
		"nonIndexedParameterDefinitions": [
		  {"position": 2, "type": "BYTES32"},
		  {"position": 3, "type": "STRING"}] },
	"correlationIdStrategy": {
		"type": "NON_INDEXED_PARAMETER",
		"parameterIndex": 0 }
},
....
{
	"id": "event-identifier-N",
	"contractAddress": "0x1fbBeeE6eC2B7B095fE3c5A572551b1e260Af4d2",
	"eventSpecification": {
		"eventName": "TestEvent",
		"indexedParameterDefinitions": [
		  {"position": 0, "type": "UINT256"},
		  {"position": 1, "type": "ADDRESS"}],
		"nonIndexedParameterDefinitions": [
		  {"position": 2, "type": "BYTES32"},
		  {"position": 3, "type": "STRING"}] },
	"correlationIdStrategy": {
		"type": "NON_INDEXED_PARAMETER",
		"parameterIndex": 0 }
}
]
```

## Registering a Transaction Monitor

From version 0.6.2, eventeum supports monitoring and broadcasting transactions. The matching criteria can be:

- HASH: Monitor a single transaction hash. The monitoring will be removed once is notified.
- FROM_ADDRESS: Monitor all transactions that are sent from a specific address.
- TO_ADDRESS: Monitor all transactions that are received for a specific address.


Besides on that, it can monitor the transaction for specific statuses: 

- FAILED: It will notify if the transaction has failed
- CONFIRMED: It will notify if the transaction is confirmed.
- UNCONFIRMED: In case the network is configured to wait for a certain number of confirmations, this will notify when is mined and not confirmed.

### REST

To register a transaction monitor, use the below REST endpoint:

-   **URL:** `/api/rest/v1/transaction?identifier=<txHash>&nodeName=<nodeName>`
-   **Method:** `POST`
-   **Headers:**  `N/A`
-   **URL Params:** `N/A`
    - identifier - The transaction hash to monitor
    - nodeName - The node name that should be monitored
-   **Body:**

An example with type `HASH`:

```json
{
	"type": "HASH",
	"transactionIdentifierValue": "0x2e8e0f98be22aa1251584e23f792d43c634744340eb274473e01a48db939f94d",
	"nodeName": "defaultNetwork",
	"statuses": ["FAIlED", "CONFIRMATION"]
}
```


Example filtering by `FROM_ADDRES`, this will notify when a transactions fails with origin the address specified in the field `transactionIdentifierValue`

```json
{
	"type": "FROM_ADDRESS" ,
	"transactionIdentifierValue": "0x1fbBeeE6eC2B7B095fE3c5A572551b1e260Af4d2",
	"nodeName": "defaultNetwork",
	"statuses": ["FAIlED"]
}
```


| Name | Type | Mandatory | Default | Description |
| -------- | -------- | -------- | -------- | -------- |
| type | String | yes | | The type of the filter you want to create: `HASH`, `FROM_ADDRESS`, `TO_ADDRESS` |
| transactionIdentifierValue | String | yes |  | The value associated with the type. It should be the tx hash for `HASH` and the address of the contract in the other cases. |
| nodeName | String | yes | default | The identifier of the node you want to listen the transaction |
| statuses | List | no | ['FAILED', 'CONFIRMED'] | It will specify the statuses you want to be notified. The default is failed and confirmed transactions. The options are: `FAILED`, `CONFIRMED`, `UNCONFIRMED`, `INVALIDATED` |


-   **Success Response:**
    -   **Code:** 200
        **Content:**

```json
{
    "id": "transaction-monitor-identifier"
}
```

## Un-Registering a Transaction Monitor

### REST

-   **URL:** `/api/rest/v1/transaction/{monitor-id}`
-   **Method:** `DELETE`
-   **Headers:**  `N/A`
-   **URL Params:** `N/A`
-   **Body:** `N/A`

-   **Success Response:**
    -   **Code:** 200  
        **Content:** `N/A`

## Broadcast Messages Format

###  Contract Events
When a subscribed event is emitted, a JSON message is broadcast to the configured kafka topic or rabbit exchange (contract-events by default), with the following format:

```json
{
	"id":"unique-event-id",
	"type":"CONTRACT_EVENT",
	"details":{
		"name":"DummyEvent",
		"filterId":"63da468c-cec6-49aa-bea4-eeba64fb1df4",
		"indexedParameters":[{"type":"bytes32","value":"BytesValue"},
			{"type":"address","value":"0x00a329c0648769a73afac7f9381e08fb43dbea72"}],
		"nonIndexedParameters":[{"type":"uint256","value":10},
			{"type":"string","value":"StringValue"}],
		"transactionHash":"0xe4fd0f095990ec471cdf40638336a73636d2e88fc1a240c20b45101b9cce9438",
		"logIndex":0,
		"blockNumber":258,
		"blockHash":"0x65d1956c2850677f75ec9adcd7b2cfab89e31ad1e7a5ba93b6fad11e6cd15e4a",
		"address":"0x9ec580fa364159a09ea15cd39505fc0a926d3a00",
		"status":"UNCONFIRMED",
		"eventSpecificationSignature":"0x46aca551d5bafd01d98f8cadeb9b50f1b3ee44c33007f2a13d969dab7e7cf2a8",
		"id":"unique-event-id"},
		"retries":0
}

```

### Block Events
When a new block is mined, a JSON message is broadcast to the configured kafka topic or rabbit exchange (block-events by default), with the following format:

```json
 {
 	"id":"0x79799054d1782eb4f246b3055b967557148f38344fbd7020febf7b2d44faa4f8",
	"type":"BLOCK",
	"details":{
		"number":257,
		"hash":"0x79799054d1782eb4f246b3055b967557148f38344fbd7020febf7b2d44faa4f8",
		"timestamp":12345678},
	"retries":0
}
```


### Transaction Events
When a new transaction that matches a transaction monitor is mined, a JSON message is broadcast to the configured kafka topic or rabbit exchange (transaction-events by default), with the following format:

```json
 {
 	"id":"0x1c0482642861779703a34f4539b3ba18a0fddfb16558f3be7157fdafcaf2c030",
	"type":"TRANSACTION",
	"details":{
		"hash":"0x1c0482642861779703a34f4539b3ba18a0fddfb16558f3be7157fdafcaf2c030",
		"nonce":"0xf",
		"blockHash":"0x6a68edf369ba4ddf93aa31cf5871ad51b5f7988a69f1ddf9ed09ead8b626db48",
		"blockNumber":"0x1e1",
		"transactionIndex":"0x0",
		"from":"0xf17f52151ebef6c7334fad080c5704d77216b732",
		"to":"0xc5fdf4076b8f3a5357c5e395ab970b5b54098fef",
		"value":"0x16345785d8a0000",
		"nodeName":"default",
		"status":"CONFIRMED"},
	"retries":0
}
```

#### Contract Creation Transaction
If the transaction is a contract creation transaction, then the `contractAddress` value will be set to the address of the newly deployed smart contract.

#### Transaction Event Statuses

A broadcast transaction event can have the following statuses:

| Status | Description |
| -------- | -------- |
| UNCONFIRMED | Transaction has been mined and we're now waiting for the configured number of blocks |
| CONFIRMED | The configured number of blocks have been mined since the transaction has been mined |
| INVALIDATED | The blockchain has forked since the initially broadcast UNCONFIRMED transaction was broadcast |
| FAILED | The transaction has been mined but the tx execution failed |

## Configuration
Eventeum can either be configured by:

1. storing an `application.yml` next to the built JAR (copy one from `config-examples`). This overlays the defaults from `server/src/main/resources/application.yml`.
2. Setting the associated environment variables.

| Env Variable | Default | Description |
| -------- | -------- | -------- |
| SERVER_PORT | 8060 | The port for the eventeum instance. |
| ETHEREUM_BLOCKSTRATEGY | POLL | The strategy for obtaining block events from an ethereum node (POLL or PUBSUB). It will be overwritten by the specific node configuration. |
| ETHEREUM_NODE_URL | http://localhost:8545 | The default ethereum node url. |
| ETHEREUM_NODE_BLOCKSTRATEGY | POLL | The strategy for obtaining block events for the ethereum node (POLL or PUBSUB).
| ETHEREUM_NODE_HEALTHCHECK_POLLINTERVAL | 2000 | The interval time in ms, in which a request is made to the ethereum node, to ensure that the node is running and functional. |
| ETHEREUM_NODE_ADD_TRANSACTION_REVERT_REASON | false | In case of a failing transaction it indicates if Eventeum should get the revert reason. Currently not working for Ganache and Parity.
| ETHEREUM_NUMBLOCKSTOREPLAY | 12 | Number of blocks to replay on node or service failure (ensures no blocks / events are missed on chain reorg) |
| POLLING_INTERVAL | 10000 | The polling interval used by Web3j to get events from the blockchain. |
| EVENTSTORE_TYPE | DB | The type of eventstore used in Eventeum. (See the Advanced section for more details) |
| BROADCASTER_TYPE | KAFKA | The broadcast mechanism to use.  (KAFKA or HTTP or RABBIT) |
| BROADCASTER_CACHE_EXPIRATIONMILLIS | 6000000 | The eventeum broadcaster has an internal cache of sent messages, which ensures that duplicate messages are not broadcast.  This is the time that a message should live within this cache. |
| BROADCASTER_EVENT_CONFIRMATION_NUMBLOCKSTOWAIT | 12 | The number of blocks to wait (after the initial mined block) before broadcasting a CONFIRMED event |
| BROADCASTER_EVENT_CONFIRMATION_NUMBLOCKSTOWAITFORMISSINGTX | 200 | After a fork, a transaction may disappear, and this is the number of blocks to wait on the new fork, before assuming that an event emitted during this transaction has been INVALIDATED |
| BROADCASTER_EVENT_CONFIRMATION_NUMBLOCKSTOWAITBEFOREINVALIDATING | 2 | Number of blocks to wait before considering a block as invalid. |
| BROADCASTER_MULTIINSTANCE | false | If multiple instances of eventeum are to be deployed in your system, this should be set to true so that the eventeum communicates added/removed filters to other instances, via kafka. |
| BROADCASTER_HTTP CONTRACTEVENTSURL | | The http url for posting contract events (for HTTP broadcasting) |
| BROADCASTER_HTTP BLOCKEVENTSURL | | The http url for posting block events (for HTTP broadcasting) |
| BROADCASTER_BYTESTOASCII | false | If any bytes values within events should be converted to ascii (default is hex) |
| BROADCASTER_ENABLE_BLOCK_NOTIFICATION | true | Boolean that indicates if want to receive block notifications or not. Set false to not receive that event. |
| ZOOKEEPER_ADDRESS | localhost:2181 | The zookeeper address |
| KAFKA_ADDRESSES | localhost:9092 | Comma seperated list of kafka addresses |
| KAFKA_TOPIC_CONTRACT_EVENTS | contract-events | The topic name for broadcast contract event messages |
| KAFKA_TOPIC_BLOCK_EVENTS | block-events | The topic name for broadcast block event messages |
| KAFKA_TOPIC_TRANSACTION_EVENTS | transaction-events | The topic name for broadcast trasaction messages |
| KAFKA_REQUEST_TIMEOUT_MS | 20000 | The duration after which a request timeouts |
| KAFKA_ENDPOINT_IDENTIFICATION_ALGORITHM | null | The endpoint identification algorithm to validate server hostname using server certificate |
| KAFKA_SASL_MECHANISM | PLAIN | The mechanism used for SASL authentication |
| KAFKA_USERNAME | "" | The username used to connect to a SASL secured Kafka cluster |
| KAFKA_PASSWORD | "" | The password used to connect to a SASL secured Kafka cluster |
| KAFKA_SECURITY_PROTOCOL | PLAINTEXT | Protocol used to communicate with Kafka brokers |
| KAFKA_RETRIES | 10 | The number of times a Kafka consumer will try to publish a message before throwing an error |
| KAFKA_RETRY_BACKOFF_MS | 500 | The duration between each retry |
| KEEP_ALIVE_DURATION | 15000 | Rpc http idle threads keep alive timeout in ms |
| MAX_IDLE_CONNECTIONS| 10 | The max number of HTTP rpc idle threads at the pool |
| SYNCINC_THRESHOLD | 60 | Number of blocks of difference to consider that eventeum is "syncing" with a node
| SPRING_DATA_MONGODB_HOST | localhost | The mongoDB host (used when event store is set to DB) |
| SPRING_DATA_MONGODB_PORT | 27017 | The mongoDB post (used when event store is set to DB) |
| RABBIT_ADDRESS | localhost:5672 | property spring.rabbitmq.host (The rabbitmq address) |
| RABBIT_EXCHANGE | ThisIsAExchange | property rabbitmq.exchange |
| RABBIT_ROUTING_KEY | thisIsRoutingKey | property rabbitmq.routingKeyPrefix |
| DATABASE_TYPE | MONGO | The database to use.  Either MONGO or SQL. |
| CONNECTION_TIMEOUT | 7000 | RPC, http connection timeout in millis |
| READ_TIMEOUT | 35000 | RPC, http read timeout in millis |

### INFURA Support Configuration
Connecting to an INFURA node is only supported if connecting via websockets (`wss://<...>` node url).  The blockstrategy must also be set to PUBSUB.

## Advanced
### Correlation Id Strategies (Kafka Broadcasting)

Each subscribed event can have a correlation id strategy association with it, during subscription.  A correlation id strategy defines what the kafka message key for a broadcast event should be, and allows the system to be configured so that events with particular parameter values are always sent to the same partition.

Currently supported correlation id strategies are:

**Indexed Parameter Strategy** - An indexed parameter within the event is used as the message key when broadcasting.
**Non Indexed Parameter Strategy** - An non-indexed parameter within the event is used as the message key when broadcasting.

### Event Store

Eventeum utilises an event store in order to establish the block number to start event subscriptions from, in the event of a failover.  For example, if the last event broadcast for event with id X had a block number of 123, then on a failover, eventeum will subscribe to events from block 124.

There are currently 2 supported event store implementations:

#### MongoDB

Broadcast events are saved and retrieved from a mongoDB database.

**Required Configuration**

| Env Variable | Default | Description |
| -------- | -------- | -------- |
| EVENTSTORE_TYPE | DB | MongoDB event store enabled |
| SPRING_DATA_MONGODB_HOST | localhost | The mongoDB host |
| SPRING_DATA_MONGODB_PORT | 27017 | The mongoDB post |

#### REST Service

Eventeum polls an external REST service in order to obtain a list of events broadcast for a specific event specification.  It is assumed that this REST service listens for broadcast events on the kafka topic and updates its internal state...broadcast events are not directly sent to the REST service by eventeum.

The implemented REST service should have a pageable endpoint which accepts a request with the following specification:

-   **URL:** Configurable, defaults to `/api/rest/v1/event`    
-   **Method:** `GET`
-   **Headers:**  

| Key | Value |
| -------- | -------- |
| content-type | application/json |

-   **URL Params:**

| Key | Value |
| -------- | -------- |
| page | The page number |
| size | The page size |
| sort | The results sort field |
| dir | The results sort direction |
| signature | Retrieve events with the specified event signature |

-   **Body:** `N/A`

-   **Success Response:**
    -   **Code:** 200  
        **Content:**

```json
{
	"content":[
		{"blockNumber":10,"id":<unique event id>}],
	"page":1,
	"size":1,
	"totalElements":1,
	"first":false,
	"last":true,
	"totalPages":1,
	"numberOfElements":1,
	"hasContent":true
}
```

**Required Configuration**

| Env Variable | Default | Description |
| -------- | -------- | -------- |
| EVENTSTORE_TYPE | REST | REST event store enabled |
| EVENTSTORE_URL  | http://localhost:8081/api/rest/v1 | The REST endpoint url |
| EVENTSTORE_EVENTPATH | /event | The path to the event REST endpoint |

### Integrating Eventeum into Third Party Spring Application

Eventeum can be embedded into an existing Spring Application via an annotation.

#### Steps to Embed

1. Add the Eventeum Artifactory repository into your `pom.xml` file:

```xml
<repositories>
  <repository>
    <id>eventeum-artifactory</id>
    <url>https://eventeum.jfrog.io/artifactory/eventeum</url>
  </repository>
</repositories>
```

2. Add the eventeum-core dependency to your `pom.xml` file:

```xml
<dependency>
  <groupId>io.eventeum</groupId>
  <artifactId>eventeum-core</artifactId>
  <version>*LATEST_EVENTEUM_VERSION*</version>
</dependency>
```

3. Within your Application class or a `@Configuration` annotated class, add the `@EnableEventeum` annotation.

#### Health check endpoint

Eventeum offers a healthcheck url where you can ask for the status of the systems you are using. It will look like:

```
{
   "status":"UP",
   "details":{
      "rabbit":{
         "status":"UP",
         "details":{
            "version":"3.7.13"
         }
      },
      "mongo":{
         "status":"UP",
         "details":{
            "version":"4.0.8"
         }
      }
   }
}
```

Returning this information it is very easy to create alerts over the status of the system.

The endpoint is: GET /monitoring/health

## Metrics: Prometheus

Eventeum includes a prometheus metrics export endpoint.

It includes standard jvm, tomcat metrics enabled by spring-boot https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-metrics-export-prometheus https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-metrics-meter.

Added to the standard metrics, custom metrics have been added:

* eventeum_%Network%_syncing: 1 if node is syncing (latestBlock + syncingThreshols < currentBlock). 0 if not syncing
* eventeum_%Network%_latestBlock: latest block read by Eventeum
* eventeum_%Network%_currentBlock: Current node block
* eventeum_%Network%_status: Current node status. 0 = Suscribed, 1 = Connected, 2 = Down

All  metrics include application="Eventeum",environment="local" tags.

The endpoint is: GET /monitoring/prometheus


## Known Caveats / Issues
* In multi-instance mode, where there is more than one Eventeum instance in a system, your services are required to handle duplicate messages gracefully, as each instance will broadcast the same events.
