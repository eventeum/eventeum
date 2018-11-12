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
    private static final String BINARY = "608060405234801561001057600080fd5b50610185806100206000396000f30060806040526004361061004b5763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663eb792e7d8114610050578063f6af6ace1461007a575b600080fd5b34801561005c57600080fd5b50610078600480359060248035916044359182019101356100a2565b005b34801561008657600080fd5b5061007860048035906024803591604435918201910135610116565b60408051848152339186917f79db4a66c74e0ab851510c0a340a3c925ba311aab3aab6b7dc74ae629c792ea99187918791879160019160208201908201835b60ff1681526020018281038252858582818152602001925080828437604051920182900397509095505050505050a350505050565b60408051848152339186917f26c16d5e1e9b37f9f69f6ac44adef332c80d1503ea39ae2abf256335886302ec9187918791879160019160208201908201836100e15600a165627a7a72305820e30ba1ce0a10f6380220dcc9467e1aeac5e920f7f03b38d88666a858e06b0ad30029";

    public static final String FUNC_EMITNOTORDERED = "emitNotOrdered";

    public static final String FUNC_EMIT = "emit";

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

    public RemoteCall<TransactionReceipt> emitNotOrdered(byte[] value1, BigInteger value2, String value3) {
        final Function function = new Function(
                FUNC_EMITNOTORDERED, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(value1), 
                new org.web3j.abi.datatypes.generated.Uint256(value2), 
                new org.web3j.abi.datatypes.Utf8String(value3)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> emit(byte[] value1, BigInteger value2, String value3) {
        final Function function = new Function(
                FUNC_EMIT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(value1), 
                new org.web3j.abi.datatypes.generated.Uint256(value2), 
                new org.web3j.abi.datatypes.Utf8String(value3)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
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
