pragma solidity ^0.4.8;

contract EventEmitter {

  enum TestEnum {
    ZERO,
    ONE,
    TWO
  }
  function emit(bytes32 value1, uint value2, string value3) external {
    DummyEvent(value1, msg.sender, value2, value3, TestEnum.ONE);
  }

  function emitNotOrdered(bytes32 value1, uint value2, string value3) external {
      DummyEventNotOrdered(value1, value2, msg.sender, value3, TestEnum.ONE);
  }

  event DummyEvent(bytes32 indexed indexedBytes, address indexed indexedAddress, uint uintValue, string stringValue, TestEnum enumValue);

  event DummyEventNotOrdered(bytes32 indexed indexedBytes, uint uintValue, address indexed indexedAddress, string stringValue, TestEnum enumValue);
}