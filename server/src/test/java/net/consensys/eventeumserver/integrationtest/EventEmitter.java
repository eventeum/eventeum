/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.consensys.eventeumserver.integrationtest;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.*;
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
 * <p>Generated with web3j version 4.5.0.
 */
public class EventEmitter extends Contract {
  private static final String BINARY =
      "608060405234801561001057600080fd5b506107c3806100206000396000f3fe608060405234801561001057600080fd5b5060043610610074576000357c01000000000000000000000000000000000000000000000000000000009004806321584986146100795780633a26d391146101065780635a73130f14610152578063b2deda5214610193578063bf38f62a14610220575b600080fd5b6101046004803603606081101561008f57600080fd5b810190808035906020019092919080359060200190929190803590602001906401000000008111156100c057600080fd5b8201836020820111156100d257600080fd5b803590602001918460018302840111640100000000831117156100f457600080fd5b909192939192939050505061028b565b005b6101506004803603608081101561011c57600080fd5b810190808035906020019092919080359060200190929190803590602001909291908035906020019092919050505061032b565b005b6101916004803603602081101561016857600080fd5b8101908080356fffffffffffffffffffffffffffffffff191690602001909291905050506104d6565b005b61021e600480360360608110156101a957600080fd5b810190808035906020019092919080359060200190929190803590602001906401000000008111156101da57600080fd5b8201836020820111156101ec57600080fd5b8035906020019184600183028401116401000000008311171561020e57600080fd5b909192939192939050505061054a565b005b6102896004803603606081101561023657600080fd5b81019080803561ffff169060200190929190803560070b906020019092919080357effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff191690602001909291905050506105ea565b005b3373ffffffffffffffffffffffffffffffffffffffff16847f26c16d5e1e9b37f9f69f6ac44adef332c80d1503ea39ae2abf256335886302ec858585600160405180858152602001806020018360028111156102e357fe5b60ff1681526020018281038252858582818152602001925080828437600081840152601f19601f8201169050808301925050509550505050505060405180910390a350505050565b6060600260405190808252806020026020018201604052801561035d5781602001602082028038833980820191505090505b5090508481600081518110151561037057fe5b90602001906020020181815250508381600181518110151561038e57fe5b9060200190602002018181525050606060026040519080825280602002602001820160405280156103ce5781602001602082028038833980820191505090505b509050838160008151811015156103e157fe5b9060200190602002018181525050828160018151811015156103ff57fe5b90602001906020020181815250507f16123bfaebb8c2005d09a269c1e3e3fdebc91b713bb049cbf5e3af8b6de80cd78282604051808060200180602001838103835285818151815260200191508051906020019060200280838360005b8381101561047757808201518184015260208101905061045c565b50505050905001838103825284818151815260200191508051906020019060200280838360005b838110156104b957808201518184015260208101905061049e565b5050505090500194505050505060405180910390a1505050505050565b806fffffffffffffffffffffffffffffffff19167f2ece6db06b5a01973109c046552420c8ab4002ec19be630471727967655574d28260405180826fffffffffffffffffffffffffffffffff19166fffffffffffffffffffffffffffffffff1916815260200191505060405180910390a250565b3373ffffffffffffffffffffffffffffffffffffffff16847f79db4a66c74e0ab851510c0a340a3c925ba311aab3aab6b7dc74ae629c792ea9858585600160405180858152602001806020018360028111156105a257fe5b60ff1681526020018281038252858582818152602001925080828437600081840152601f19601f8201169050808301925050509550505050505060405180910390a350505050565b6060600260405190808252806020026020018201604052801561061c5781602001602082028038833980820191505090505b5090503381600081518110151561062f57fe5b9060200190602002019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff16815250503081600181518110151561067b57fe5b9060200190602002019073ffffffffffffffffffffffffffffffffffffffff16908173ffffffffffffffffffffffffffffffffffffffff16815250508260070b8461ffff167fd2c396174d640efb6507dd0f3de15df2a56ebbebcff1d112258b965bb876cc4b838560016040518080602001847effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff19167effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916815260200183151515158152602001828103825285818151815260200191508051906020019060200280838360005b8381101561077c578082015181840152602081019050610761565b5050505090500194505050505060405180910390a35050505056fea165627a7a72305820dc4f813bd1f00a951fb35397a59ffedc47e5a5b165e580b0af0806b03a36100e0029";

  public static final String FUNC_EMITEVENT = "emitEvent";

  public static final String FUNC_EMITEVENTARRAY = "emitEventArray";

  public static final String FUNC_EMITEVENTBYTES16 = "emitEventBytes16";

  public static final String FUNC_EMITEVENTNOTORDERED = "emitEventNotOrdered";

  public static final String FUNC_EMITEVENTADDITIONALTYPES = "emitEventAdditionalTypes";

  public static final Event DUMMYEVENTBYTES16_EVENT =
      new Event(
          "DummyEventBytes16",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Bytes16>(true) {}, new TypeReference<Bytes16>() {}));
  ;

  public static final Event DUMMYEVENT_EVENT =
      new Event(
          "DummyEvent",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Bytes32>(true) {},
              new TypeReference<Address>(true) {},
              new TypeReference<Uint256>() {},
              new TypeReference<Utf8String>() {},
              new TypeReference<Uint8>() {}));
  ;

  public static final Event DUMMYEVENTNOTORDERED_EVENT =
      new Event(
          "DummyEventNotOrdered",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Bytes32>(true) {},
              new TypeReference<Uint256>() {},
              new TypeReference<Address>(true) {},
              new TypeReference<Utf8String>() {},
              new TypeReference<Uint8>() {}));
  ;

  public static final Event DUMMYEVENTARRAY_EVENT =
      new Event(
          "DummyEventArray",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<DynamicArray<Uint256>>() {},
              new TypeReference<DynamicArray<Bytes32>>() {}));
  ;

  public static final Event DUMMYEVENTADDITIONALTYPES_EVENT =
      new Event(
          "DummyEventAdditionalTypes",
          Arrays.<TypeReference<?>>asList(
              new TypeReference<Uint16>(true) {},
              new TypeReference<Int64>(true) {},
              new TypeReference<DynamicArray<Address>>() {},
              new TypeReference<Bytes1>() {},
              new TypeReference<Bool>() {}));
  ;

  @Deprecated
  protected EventEmitter(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  protected EventEmitter(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
  }

  @Deprecated
  protected EventEmitter(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  protected EventEmitter(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public RemoteFunctionCall<TransactionReceipt> emitEvent(
      byte[] value1, BigInteger value2, String value3) {
    final Function function =
        new Function(
            FUNC_EMITEVENT,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Bytes32(value1),
                new org.web3j.abi.datatypes.generated.Uint256(value2),
                new org.web3j.abi.datatypes.Utf8String(value3)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> emitEventArray(
      BigInteger value1, BigInteger value2, byte[] value3, byte[] value4) {
    final Function function =
        new Function(
            FUNC_EMITEVENTARRAY,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint256(value1),
                new org.web3j.abi.datatypes.generated.Uint256(value2),
                new org.web3j.abi.datatypes.generated.Bytes32(value3),
                new org.web3j.abi.datatypes.generated.Bytes32(value4)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> emitEventBytes16(byte[] bytes16Value) {
    final Function function =
        new Function(
            FUNC_EMITEVENTBYTES16,
            Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(bytes16Value)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> emitEventNotOrdered(
      byte[] value1, BigInteger value2, String value3) {
    final Function function =
        new Function(
            FUNC_EMITEVENTNOTORDERED,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Bytes32(value1),
                new org.web3j.abi.datatypes.generated.Uint256(value2),
                new org.web3j.abi.datatypes.Utf8String(value3)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public RemoteFunctionCall<TransactionReceipt> emitEventAdditionalTypes(
      BigInteger uint16Value, BigInteger int64Value, byte[] byteValue) {
    final Function function =
        new Function(
            FUNC_EMITEVENTADDITIONALTYPES,
            Arrays.<Type>asList(
                new org.web3j.abi.datatypes.generated.Uint16(uint16Value),
                new org.web3j.abi.datatypes.generated.Int64(int64Value),
                new org.web3j.abi.datatypes.generated.Bytes1(byteValue)),
            Collections.<TypeReference<?>>emptyList());
    return executeRemoteCallTransaction(function);
  }

  public List<DummyEventBytes16EventResponse> getDummyEventBytes16Events(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(DUMMYEVENTBYTES16_EVENT, transactionReceipt);
    ArrayList<DummyEventBytes16EventResponse> responses =
        new ArrayList<DummyEventBytes16EventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      DummyEventBytes16EventResponse typedResponse = new DummyEventBytes16EventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.indexedBytes16 = (byte[]) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.bytes16Value = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<DummyEventBytes16EventResponse> dummyEventBytes16EventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, DummyEventBytes16EventResponse>() {
              @Override
              public DummyEventBytes16EventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(DUMMYEVENTBYTES16_EVENT, log);
                DummyEventBytes16EventResponse typedResponse = new DummyEventBytes16EventResponse();
                typedResponse.log = log;
                typedResponse.indexedBytes16 =
                    (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.bytes16Value =
                    (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<DummyEventBytes16EventResponse> dummyEventBytes16EventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(DUMMYEVENTBYTES16_EVENT));
    return dummyEventBytes16EventFlowable(filter);
  }

  public List<DummyEventEventResponse> getDummyEventEvents(TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(DUMMYEVENT_EVENT, transactionReceipt);
    ArrayList<DummyEventEventResponse> responses =
        new ArrayList<DummyEventEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      DummyEventEventResponse typedResponse = new DummyEventEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.indexedBytes = (byte[]) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.indexedAddress = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.uintValue = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      typedResponse.stringValue = (String) eventValues.getNonIndexedValues().get(1).getValue();
      typedResponse.enumValue = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<DummyEventEventResponse> dummyEventEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, DummyEventEventResponse>() {
              @Override
              public DummyEventEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(DUMMYEVENT_EVENT, log);
                DummyEventEventResponse typedResponse = new DummyEventEventResponse();
                typedResponse.log = log;
                typedResponse.indexedBytes =
                    (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.indexedAddress =
                    (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.uintValue =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.stringValue =
                    (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.enumValue =
                    (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<DummyEventEventResponse> dummyEventEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(DUMMYEVENT_EVENT));
    return dummyEventEventFlowable(filter);
  }

  public List<DummyEventNotOrderedEventResponse> getDummyEventNotOrderedEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(DUMMYEVENTNOTORDERED_EVENT, transactionReceipt);
    ArrayList<DummyEventNotOrderedEventResponse> responses =
        new ArrayList<DummyEventNotOrderedEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      DummyEventNotOrderedEventResponse typedResponse = new DummyEventNotOrderedEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.indexedBytes = (byte[]) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.indexedAddress = (String) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.uintValue = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
      typedResponse.stringValue = (String) eventValues.getNonIndexedValues().get(1).getValue();
      typedResponse.enumValue = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<DummyEventNotOrderedEventResponse> dummyEventNotOrderedEventFlowable(
      EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, DummyEventNotOrderedEventResponse>() {
              @Override
              public DummyEventNotOrderedEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(DUMMYEVENTNOTORDERED_EVENT, log);
                DummyEventNotOrderedEventResponse typedResponse =
                    new DummyEventNotOrderedEventResponse();
                typedResponse.log = log;
                typedResponse.indexedBytes =
                    (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.indexedAddress =
                    (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.uintValue =
                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.stringValue =
                    (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.enumValue =
                    (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<DummyEventNotOrderedEventResponse> dummyEventNotOrderedEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(DUMMYEVENTNOTORDERED_EVENT));
    return dummyEventNotOrderedEventFlowable(filter);
  }

  public List<DummyEventArrayEventResponse> getDummyEventArrayEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(DUMMYEVENTARRAY_EVENT, transactionReceipt);
    ArrayList<DummyEventArrayEventResponse> responses =
        new ArrayList<DummyEventArrayEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      DummyEventArrayEventResponse typedResponse = new DummyEventArrayEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.uintArray =
          (List<BigInteger>) eventValues.getNonIndexedValues().get(0).getValue();
      typedResponse.bytesArray = (List<byte[]>) eventValues.getNonIndexedValues().get(1).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<DummyEventArrayEventResponse> dummyEventArrayEventFlowable(EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, DummyEventArrayEventResponse>() {
              @Override
              public DummyEventArrayEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(DUMMYEVENTARRAY_EVENT, log);
                DummyEventArrayEventResponse typedResponse = new DummyEventArrayEventResponse();
                typedResponse.log = log;
                typedResponse.uintArray =
                    (List<BigInteger>) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.bytesArray =
                    (List<byte[]>) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<DummyEventArrayEventResponse> dummyEventArrayEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(DUMMYEVENTARRAY_EVENT));
    return dummyEventArrayEventFlowable(filter);
  }

  public List<DummyEventAdditionalTypesEventResponse> getDummyEventAdditionalTypesEvents(
      TransactionReceipt transactionReceipt) {
    List<Contract.EventValuesWithLog> valueList =
        extractEventParametersWithLog(DUMMYEVENTADDITIONALTYPES_EVENT, transactionReceipt);
    ArrayList<DummyEventAdditionalTypesEventResponse> responses =
        new ArrayList<DummyEventAdditionalTypesEventResponse>(valueList.size());
    for (Contract.EventValuesWithLog eventValues : valueList) {
      DummyEventAdditionalTypesEventResponse typedResponse =
          new DummyEventAdditionalTypesEventResponse();
      typedResponse.log = eventValues.getLog();
      typedResponse.uint16Value = (BigInteger) eventValues.getIndexedValues().get(0).getValue();
      typedResponse.int64Value = (BigInteger) eventValues.getIndexedValues().get(1).getValue();
      typedResponse.addressArray =
          (List<String>) eventValues.getNonIndexedValues().get(0).getValue();
      typedResponse.byteValue = (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
      typedResponse.boolValue = (Boolean) eventValues.getNonIndexedValues().get(2).getValue();
      responses.add(typedResponse);
    }
    return responses;
  }

  public Flowable<DummyEventAdditionalTypesEventResponse> dummyEventAdditionalTypesEventFlowable(
      EthFilter filter) {
    return web3j
        .ethLogFlowable(filter)
        .map(
            new io.reactivex.functions.Function<Log, DummyEventAdditionalTypesEventResponse>() {
              @Override
              public DummyEventAdditionalTypesEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues =
                    extractEventParametersWithLog(DUMMYEVENTADDITIONALTYPES_EVENT, log);
                DummyEventAdditionalTypesEventResponse typedResponse =
                    new DummyEventAdditionalTypesEventResponse();
                typedResponse.log = log;
                typedResponse.uint16Value =
                    (BigInteger) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.int64Value =
                    (BigInteger) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.addressArray =
                    (List<String>) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.byteValue =
                    (byte[]) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.boolValue =
                    (Boolean) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
              }
            });
  }

  public Flowable<DummyEventAdditionalTypesEventResponse> dummyEventAdditionalTypesEventFlowable(
      DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
    EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
    filter.addSingleTopic(EventEncoder.encode(DUMMYEVENTADDITIONALTYPES_EVENT));
    return dummyEventAdditionalTypesEventFlowable(filter);
  }

  @Deprecated
  public static EventEmitter load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new EventEmitter(contractAddress, web3j, credentials, gasPrice, gasLimit);
  }

  @Deprecated
  public static EventEmitter load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return new EventEmitter(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
  }

  public static EventEmitter load(
      String contractAddress,
      Web3j web3j,
      Credentials credentials,
      ContractGasProvider contractGasProvider) {
    return new EventEmitter(contractAddress, web3j, credentials, contractGasProvider);
  }

  public static EventEmitter load(
      String contractAddress,
      Web3j web3j,
      TransactionManager transactionManager,
      ContractGasProvider contractGasProvider) {
    return new EventEmitter(contractAddress, web3j, transactionManager, contractGasProvider);
  }

  public static RemoteCall<EventEmitter> deploy(
      Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
    return deployRemoteCall(
        EventEmitter.class, web3j, credentials, contractGasProvider, BINARY, "");
  }

  @Deprecated
  public static RemoteCall<EventEmitter> deploy(
      Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
    return deployRemoteCall(EventEmitter.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
  }

  public static RemoteCall<EventEmitter> deploy(
      Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
    return deployRemoteCall(
        EventEmitter.class, web3j, transactionManager, contractGasProvider, BINARY, "");
  }

  @Deprecated
  public static RemoteCall<EventEmitter> deploy(
      Web3j web3j,
      TransactionManager transactionManager,
      BigInteger gasPrice,
      BigInteger gasLimit) {
    return deployRemoteCall(
        EventEmitter.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
  }

  public static class DummyEventBytes16EventResponse extends BaseEventResponse {
    public byte[] indexedBytes16;

    public byte[] bytes16Value;
  }

  public static class DummyEventEventResponse extends BaseEventResponse {
    public byte[] indexedBytes;

    public String indexedAddress;

    public BigInteger uintValue;

    public String stringValue;

    public BigInteger enumValue;
  }

  public static class DummyEventNotOrderedEventResponse extends BaseEventResponse {
    public byte[] indexedBytes;

    public String indexedAddress;

    public BigInteger uintValue;

    public String stringValue;

    public BigInteger enumValue;
  }

  public static class DummyEventArrayEventResponse extends BaseEventResponse {
    public List<BigInteger> uintArray;

    public List<byte[]> bytesArray;
  }

  public static class DummyEventAdditionalTypesEventResponse extends BaseEventResponse {
    public BigInteger uint16Value;

    public BigInteger int64Value;

    public List<String> addressArray;

    public byte[] byteValue;

    public Boolean boolValue;
  }
}
