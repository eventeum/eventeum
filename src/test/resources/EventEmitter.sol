pragma solidity ^0.4.8;

contract EventEmitter {

  function emit(bytes32 value1, uint value2, string value3) external {
    DummyEvent(value1, msg.sender, value2, value3);
  }

  function emitNotOrdered(bytes32 value1, uint value2, string value3) external {
      DummyEventNotOrdered(value1, value2, msg.sender, value3);
    }

  event DummyEvent(bytes32 indexed indexedBytes, address indexed indexedAddress, uint uintValue, string stringValue);

  event DummyEventNotOrdered(bytes32 indexed indexedBytes, uint uintValue, address indexed indexedAddress, string stringValue);
}