pragma solidity ^0.5.4;

contract EventEmitter {

  enum TestEnum {
    ZERO,
    ONE,
    TWO
  }

  function emitEvent(bytes32 value1, uint value2, string calldata value3) external {
    emit DummyEvent(value1, msg.sender, value2, value3, TestEnum.ONE);
  }

  function emitEventNotOrdered(bytes32 value1, uint value2, string calldata value3) external {
    emit DummyEventNotOrdered(value1, value2, msg.sender, value3, TestEnum.ONE);
  }

  function emitEventBytes16(bytes16 bytes16Value) external {
    emit DummyEventBytes16(bytes16Value, bytes16Value);
  }

  event DummyEventBytes16(bytes16 indexed indexedBytes16, bytes16 bytes16Value);

  event DummyEvent(bytes32 indexed indexedBytes, address indexed indexedAddress, uint uintValue, string stringValue, TestEnum enumValue);

  event DummyEventNotOrdered(bytes32 indexed indexedBytes, uint uintValue, address indexed indexedAddress, string stringValue, TestEnum enumValue);
}