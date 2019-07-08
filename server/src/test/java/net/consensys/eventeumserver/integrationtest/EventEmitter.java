package net.consensys.eventeumserver.integrationtest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes16;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.6.0.
 */
public class EventEmitter extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b506102f7806100206000396000f3fe608060405234801561001057600080fd5b506004361061005d577c010000000000000000000000000000000000000000000000000000000060003504632158498681146100625780635a73130f146100e0578063b2deda5214610110575b600080fd5b6100de6004803603606081101561007857600080fd5b81359160208101359181019060608101604082013564010000000081111561009f57600080fd5b8201836020820111156100b157600080fd5b803590602001918460018302840111640100000000831117156100d357600080fd5b50909250905061018c565b005b6100de600480360360208110156100f657600080fd5b50356fffffffffffffffffffffffffffffffff1916610229565b6100de6004803603606081101561012657600080fd5b81359160208101359181019060608101604082013564010000000081111561014d57600080fd5b82018360208201111561015f57600080fd5b8035906020019184600183028401116401000000008311171561018157600080fd5b509092509050610273565b3373ffffffffffffffffffffffffffffffffffffffff16847f26c16d5e1e9b37f9f69f6ac44adef332c80d1503ea39ae2abf256335886302ec858585600160405180858152602001806020018360028111156101e457fe5b60ff1681526020018281038252858582818152602001925080828437600083820152604051601f909101601f191690920182900397509095505050505050a350505050565b604080516fffffffffffffffffffffffffffffffff19831680825291517f2ece6db06b5a01973109c046552420c8ab4002ec19be630471727967655574d29181900360200190a250565b3373ffffffffffffffffffffffffffffffffffffffff16847f79db4a66c74e0ab851510c0a340a3c925ba311aab3aab6b7dc74ae629c792ea9858585600160405180858152602001806020018360028111156101e457fefea165627a7a723058206febfafa3c5d806657676bdcc563083143686fb81f768e60215d6fd99b1cd9ad0029";

    public static final String FUNC_EMITEVENT = "emitEvent";

    public static final String FUNC_EMITEVENTBYTES16 = "emitEventBytes16";

    public static final String FUNC_EMITEVENTNOTORDERED = "emitEventNotOrdered";

    public static final Event DUMMYEVENTBYTES16_EVENT = new Event("DummyEventBytes16", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes16>(true) {}, new TypeReference<Bytes16>() {}));
    ;

    public static final Event DUMMYEVENT_EVENT = new Event("DummyEvent", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint8>() {}));
    ;

    public static final Event DUMMYEVENTNOTORDERED_EVENT = new Event("DummyEventNotOrdered", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>(true) {}, new TypeReference<Uint256>() {}, new TypeReference<Address>(true) {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint8>() {}));
    ;

    @Deprecated
    protected EventEmitter(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected EventEmitter(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected EventEmitter(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected EventEmitter(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public RemoteCall<TransactionReceipt> emitEvent(byte[] value1, BigInteger value2, String value3) {
        final Function function = new Function(
                FUNC_EMITEVENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(value1), 
                new org.web3j.abi.datatypes.generated.Uint256(value2), 
                new org.web3j.abi.datatypes.Utf8String(value3)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> emitEventBytes16(byte[] bytes16Value) {
        final Function function = new Function(
                FUNC_EMITEVENTBYTES16, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(bytes16Value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> emitEventNotOrdered(byte[] value1, BigInteger value2, String value3) {
        final Function function = new Function(
                FUNC_EMITEVENTNOTORDERED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(value1), 
                new org.web3j.abi.datatypes.generated.Uint256(value2), 
                new org.web3j.abi.datatypes.Utf8String(value3)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public List<DummyEventBytes16EventResponse> getDummyEventBytes16Events(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DUMMYEVENTBYTES16_EVENT, transactionReceipt);
        ArrayList<DummyEventBytes16EventResponse> responses = new ArrayList<DummyEventBytes16EventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            DummyEventBytes16EventResponse typedResponse = new DummyEventBytes16EventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.indexedBytes16 = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.bytes16Value = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<DummyEventBytes16EventResponse> dummyEventBytes16EventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, DummyEventBytes16EventResponse>() {
            @Override
            public DummyEventBytes16EventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DUMMYEVENTBYTES16_EVENT, log);
                DummyEventBytes16EventResponse typedResponse = new DummyEventBytes16EventResponse();
                typedResponse.log = log;
                typedResponse.indexedBytes16 = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.bytes16Value = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Observable<DummyEventBytes16EventResponse> dummyEventBytes16EventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DUMMYEVENTBYTES16_EVENT));
        return dummyEventBytes16EventObservable(filter);
    }

    public List<DummyEventEventResponse> getDummyEventEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DUMMYEVENT_EVENT, transactionReceipt);
        ArrayList<DummyEventEventResponse> responses = new ArrayList<DummyEventEventResponse>(valueList.size());
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

    public Observable<DummyEventEventResponse> dummyEventEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, DummyEventEventResponse>() {
            @Override
            public DummyEventEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DUMMYEVENT_EVENT, log);
                DummyEventEventResponse typedResponse = new DummyEventEventResponse();
                typedResponse.log = log;
                typedResponse.indexedBytes = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.indexedAddress = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.uintValue = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.stringValue = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.enumValue = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Observable<DummyEventEventResponse> dummyEventEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DUMMYEVENT_EVENT));
        return dummyEventEventObservable(filter);
    }

    public List<DummyEventNotOrderedEventResponse> getDummyEventNotOrderedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(DUMMYEVENTNOTORDERED_EVENT, transactionReceipt);
        ArrayList<DummyEventNotOrderedEventResponse> responses = new ArrayList<DummyEventNotOrderedEventResponse>(valueList.size());
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

    public Observable<DummyEventNotOrderedEventResponse> dummyEventNotOrderedEventObservable(EthFilter filter) {
        return web3j.ethLogObservable(filter).map(new Func1<Log, DummyEventNotOrderedEventResponse>() {
            @Override
            public DummyEventNotOrderedEventResponse call(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DUMMYEVENTNOTORDERED_EVENT, log);
                DummyEventNotOrderedEventResponse typedResponse = new DummyEventNotOrderedEventResponse();
                typedResponse.log = log;
                typedResponse.indexedBytes = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.indexedAddress = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.uintValue = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.stringValue = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.enumValue = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Observable<DummyEventNotOrderedEventResponse> dummyEventNotOrderedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DUMMYEVENTNOTORDERED_EVENT));
        return dummyEventNotOrderedEventObservable(filter);
    }

    public static RemoteCall<EventEmitter> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(EventEmitter.class, web3j, credentials, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<EventEmitter> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(EventEmitter.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<EventEmitter> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(EventEmitter.class, web3j, transactionManager, contractGasProvider, BINARY, "");
    }

    @Deprecated
    public static RemoteCall<EventEmitter> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(EventEmitter.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    @Deprecated
    public static EventEmitter load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new EventEmitter(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static EventEmitter load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new EventEmitter(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static EventEmitter load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new EventEmitter(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static EventEmitter load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new EventEmitter(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static class DummyEventBytes16EventResponse {
        public Log log;

        public byte[] indexedBytes16;

        public byte[] bytes16Value;
    }

    public static class DummyEventEventResponse {
        public Log log;

        public byte[] indexedBytes;

        public String indexedAddress;

        public BigInteger uintValue;

        public String stringValue;

        public BigInteger enumValue;
    }

    public static class DummyEventNotOrderedEventResponse {
        public Log log;

        public byte[] indexedBytes;

        public String indexedAddress;

        public BigInteger uintValue;

        public String stringValue;

        public BigInteger enumValue;
    }
}
