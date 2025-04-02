package net.consensys.eventeum.plugin.chain.service.eth;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * Auto generated code.
 *
 * <p><strong>Do not modify!</strong>
 *
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the <a
 * href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.4.1.
 */
public class MyContract extends Contract {
  public static final String BINARY =
      "608060405234801561001057600080fd5b506101ac806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c806354fd4d501461003b5780637b0cb83914610059575b600080fd5b610043610063565b60405161005091906100fb565b60405180910390f35b610061610068565b005b600181565b7f02f83fe6099cbcd06b097fe089934fc7c63df36c0daea35d2fb79e00019c7b133360014260405161009c939291906100c4565b60405180910390a1565b6100af81610140565b82525050565b6100be81610136565b82525050565b60006060820190506100d960008301866100a6565b6100e660208301856100b5565b6100f360408301846100b5565b949350505050565b600060208201905061011060008301846100b5565b92915050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b600061014b82610152565b9050919050565b600061015d82610164565b9050919050565b600061016f82610116565b905091905056fea2646970667358221220e38bd7fc3014861ee9ea82054ba2db8c5b54e9b0faa3678141e06babbeea345f64736f6c63430007060033";

  public static final String FUNC_EMITEVENT = "emitEvent";

  public static final String FUNC_VERSION = "version";

  public static final Event EVENT_EVENT =
      new Event(
          "Event",
          Arrays.asList(
              new TypeReference<Address>() {},
              new TypeReference<Uint256>() {},
              new TypeReference<Uint256>() {}));

  @Deprecated
  protected MyContract(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  protected MyContract(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
  }

  @Deprecated
  protected MyContract(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  protected MyContract(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public List<EventEventResponse> getEventEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(EVENT_EVENT, transactionReceipt);
    ArrayList<EventEventResponse> responses = new ArrayList<EventEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      EventEventResponse typedResponse = new EventEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.sender = (String) eventValues.getNonIndexedValues().get(0).getValue();
      typedResponse.version = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
      typedResponse.otherdata = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<EventEventResponse> eventEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new Function<Log, EventEventResponse>() {
              @Override
              public EventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(EVENT_EVENT, log);
                EventEventResponse typedResponse = new EventEventResponse();
                typedResponse.log = log;
                typedResponse.sender = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.version =
                    (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.otherdata =
                    (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<EventEventResponse> eventEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(EVENT_EVENT));
    return eventEventFlowable(filter);
  }

  public RemoteFunctionCall<TransactionReceipt> emitEvent() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(FUNC_EMITEVENT, List.of(), Collections.emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<BigInteger> version() {
    final org.web3j.abi.datatypes.Function function =
        new org.web3j.abi.datatypes.Function(
            FUNC_VERSION, List.of(), List.of(new TypeReference<Uint256>() {}));
    return executeRemoteCallSingleValueReturn(function, BigInteger.class);
  }

  @Deprecated
  public static MyContract load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new MyContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  @Deprecated
  public static MyContract load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new MyContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  public static MyContract load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    return new MyContract(contractAddress, web3j, credentials, contractGasProvider);
  }

  public static MyContract load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    return new MyContract(contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public static RemoteCall<MyContract> deploy(
      Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
    return deployRemoteCall(MyContract.class, web3j, credentials, contractGasProvider, BINARY, "");
  }

  @Deprecated
  public static RemoteCall<MyContract> deploy(
      Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
    return deployRemoteCall(MyContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
  }

  public static RemoteCall<MyContract> deploy(
      Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
    return deployRemoteCall(
        MyContract.class, web3j, transactionManager, contractGasProvider, BINARY, "");
  }

  @Deprecated
  public static RemoteCall<MyContract> deploy(
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return deployRemoteCall(
        MyContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
  }

  public static class EventEventResponse extends BaseEventResponse {
    public String sender;

    public BigInteger version;

    public BigInteger otherdata;
  }
}
