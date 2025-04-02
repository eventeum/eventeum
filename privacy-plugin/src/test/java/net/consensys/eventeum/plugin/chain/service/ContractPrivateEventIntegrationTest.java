package net.consensys.eventeum.plugin.chain.service;

import static net.consensys.eventeum.plugin.chain.service.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.config.BaseConfiguration;
import net.consensys.eventeum.config.DatabaseConfiguration;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.plugin.chain.service.eth.MyContract;
import net.consensys.eventeum.service.EventStoreService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/*
* This class is used to execute integration tests. This tests executes the privacy plugin module, and connects it to a
* real infrastructure. Then, deploy a contract, invoke it and check if the contract is received.
* There are several steps required before executing this tests:
* 1- Deploy a private and public contract. Methods testDeployPrivateContract and testDeployPublicContract can be used.
* 2- Write the contract event filter specifications in the file src/main/resources/application.yml. An example is provided.
* 3- Update the constants in TestUtils (privateFrom, privacyGroupId, credentials, event signature, contracts addresses,etc)
* 4- Set up the infrastructure. Two steps are required:
*   4.1- You must launch a Besu blockchain network on your own.
*   4.2- You must execute a DD.BB and a Kafka broker. For this, the scripts setup_environment and stop_environment can be used.
*        Keep in mind that if you use a sql database, there must exist a database called eventeum.
*
* The source code of the deployed contract, MyContract, is the following:
*
    pragma solidity ^0.7.5;
    pragma experimental ABIEncoderV2;

    contract Contract {

        uint256 public constant version = 1;

        event Event(address sender, uint256 version, uint256 otherdata);

        function emitEvent() external {
            emit Event(msg.sender, version, block.timestamp);
        }
    }
*
* */
@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {BaseConfiguration.class, DatabaseConfiguration.class},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Disabled("We not need to have a local network running")
public class ContractPrivateEventIntegrationTest {

  @Autowired private EventStoreService evStore;

  @Test
  public void endToEndPrivateContractEventFilterTest() throws Exception {
    MyContract contract = loadPrivateContract();
    testContract(PRIVATE_CONTRACT_ADDRESS, contract);
  }

  @Test
  public void endToEndPublicContractEventFilterTest() throws Exception {
    MyContract contract = loadPublicContract();
    testContract(PUBLIC_CONTRACT_ADDRESS, contract);
  }

  private void testContract(String contractAddress, MyContract contract) throws Exception {
    long testInitTime = Instant.now().getEpochSecond();
    assertNotNull(contract);
    assertEquals(TestUtils.CONTRACT_VERSION, contract.version().send());
    TransactionReceipt receipt = contract.emitEvent().send();
    log.debug("Waiting for contract to be processed");
    assertTrue("Tx not ok", receipt.isStatusOK());
    assertFalse("Tx has not events", contract.getEventEvents(receipt).isEmpty());

    Thread.sleep(TestUtils.BLOCK_TIME_MS * 5);

    Optional<ContractEventDetails> latestContractEvent =
        evStore.getLatestContractEvent(EVENT_SPEC_HASH, contractAddress);

    assertTrue("Event not present", latestContractEvent.isPresent());
    assertEquals(
        "Sender in event is not correct",
        Keys.toChecksumAddress(TestUtils.creds.getAddress()),
        latestContractEvent.get().getNonIndexedParameters().get(0).getValueString());
    assertEquals(
        Long.valueOf(1),
        Long.valueOf(latestContractEvent.get().getNonIndexedParameters().get(1).getValueString()));
    assertTrue(
        "Timestamp of event is not correct",
        latestContractEvent.get().getTimestamp().longValueExact() > testInitTime);
  }

  @Test
  public void testDeployPrivateContract() throws Exception {
    MyContract contract =
        MyContract.deploy(TestUtils.besu, TestUtils.privateManager, TestUtils.gasProvider).send();
    assertNotNull(contract);
    assertNotNull(contract.getContractAddress());
    assertEquals(TestUtils.CONTRACT_VERSION, contract.version().send());
    log.debug("Contract deployed at: {}", contract.getContractAddress());
  }

  @Test
  public void testDeployPublicContract() throws Exception {
    MyContract contract =
        MyContract.deploy(TestUtils.besu, TestUtils.publicManager, TestUtils.gasProvider).send();
    assertNotNull(contract);
    assertNotNull(contract.getContractAddress());
    assertTrue("", isValidBytecode(contract.getContractAddress()));
    assertEquals(TestUtils.CONTRACT_VERSION, contract.version().send());
    log.debug("Contract deployed at: {}", contract.getContractAddress());
  }

  private boolean isValidBytecode(String contractAddress) throws IOException {
    String code =
        besu.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send().getCode();
    return code != null && !code.equals("0x0") && !code.isEmpty();
  }

  private MyContract loadPrivateContract() {
    return MyContract.load(
        TestUtils.PRIVATE_CONTRACT_ADDRESS,
        TestUtils.besu,
        TestUtils.privateManager,
        TestUtils.gasProvider);
  }

  private MyContract loadPublicContract() {
    return MyContract.load(
        TestUtils.PUBLIC_CONTRACT_ADDRESS,
        TestUtils.besu,
        TestUtils.publicManager,
        TestUtils.gasProvider);
  }
}
