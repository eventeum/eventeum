solc EventEmitter.sol --bin --abi --optimize -o compiled/
web3j solidity generate --javaTypes compiled/EventEmitter.bin compiled/EventEmitter.abi -o ../java -p net.consensys.eventeum.integrationtest