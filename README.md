# Eventeum
A bridge between your Ethereum smart contract events and backend microservices.  Eventeum listens for specified event emissions from the Ethereum network, and broadcasts these events into your middleware layer.  This provides a distinct seperation of concerns and means that your microservices do not have to subscribe to events directly to an Ethereum node.

## Features
* Dynamically Configurable - Eventeum exposes a REST api so that smart contract events can be dynamically subscribed / unsubscribed.

* Highly Available - Eventeum instances communicate with each other to ensure that every instance is subscribed to the same collection of smart contract events.

* Resilient

Documentation coming soon!
