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

  function emitEventArray(uint value1, uint value2, bytes32 value3, bytes32 value4) external {
    uint256[] memory uintArray = new uint256[](2);
    uintArray[0] = value1;
    uintArray[1] = value2;

    bytes32[] memory bytesArray = new bytes32[](2);
    bytesArray[0] = value3;
    bytesArray[1] = value4;

    emit DummyEventArray(uintArray, bytesArray);
  }

  function emitEventAdditionalTypes(uint16 uint16Value, int64 int64Value, byte byteValue) external {
    address[] memory addressArray = new address[](2);
    addressArray[0] = msg.sender;
    addressArray[1] = address(this);

    emit DummyEventAdditionalTypes(uint16Value, int64Value, addressArray, byteValue, true);
  }

  event DummyEventBytes16(bytes16 indexed indexedBytes16, bytes16 bytes16Value);

  event DummyEvent(bytes32 indexed indexedBytes, address indexed indexedAddress, uint uintValue, string stringValue, TestEnum enumValue);

  event DummyEventNotOrdered(bytes32 indexed indexedBytes, uint uintValue, address indexed indexedAddress, string stringValue, TestEnum enumValue);

  event DummyEventArray(uint256[] uintArray, bytes32[] bytesArray);

  event DummyEventAdditionalTypes(uint16 indexed uint16Value, int64 indexed int64Value, address[] addressArray, byte byteValue, bool boolValue);
}