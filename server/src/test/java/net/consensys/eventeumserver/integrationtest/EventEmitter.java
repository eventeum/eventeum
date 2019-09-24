package net.consensys.eventeumserver.integrationtest;

import io.reactivex.Flowable;
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
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.5.0.
 */
public class EventEmitter extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b5061039e806100206000396000f3fe608060405234801561001057600080fd5b506004361061005e576000357c01000000000000000000000000000000000000000000000000000000009004806321584986146100635780635a73130f146100f0578063b2deda5214610131575b600080fd5b6100ee6004803603606081101561007957600080fd5b810190808035906020019092919080359060200190929190803590602001906401000000008111156100aa57600080fd5b8201836020820111156100bc57600080fd5b803590602001918460018302840111640100000000831117156100de57600080fd5b90919293919293905050506101be565b005b61012f6004803603602081101561010657600080fd5b8101908080356fffffffffffffffffffffffffffffffff1916906020019092919050505061025e565b005b6101bc6004803603606081101561014757600080fd5b8101908080359060200190929190803590602001909291908035906020019064010000000081111561017857600080fd5b82018360208201111561018a57600080fd5b803590602001918460018302840111640100000000831117156101ac57600080fd5b90919293919293905050506102d2565b005b3373ffffffffffffffffffffffffffffffffffffffff16847f26c16d5e1e9b37f9f69f6ac44adef332c80d1503ea39ae2abf256335886302ec8585856001604051808581526020018060200183600281111561021657fe5b60ff1681526020018281038252858582818152602001925080828437600081840152601f19601f8201169050808301925050509550505050505060405180910390a350505050565b806fffffffffffffffffffffffffffffffff19167f2ece6db06b5a01973109c046552420c8ab4002ec19be630471727967655574d28260405180826fffffffffffffffffffffffffffffffff19166fffffffffffffffffffffffffffffffff1916815260200191505060405180910390a250565b3373ffffffffffffffffffffffffffffffffffffffff16847f79db4a66c74e0ab851510c0a340a3c925ba311aab3aab6b7dc74ae629c792ea98585856001604051808581526020018060200183600281111561032a57fe5b60ff1681526020018281038252858582818152602001925080828437600081840152601f19601f8201169050808301925050509550505050505060405180910390a35050505056fea165627a7a72305820dcd452b8001f20377421ced830be152102601a0899d044f69eea623b0699bf0e0029";

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

    public RemoteFunctionCall<TransactionReceipt> emitEvent(byte[] value1, BigInteger value2, String value3) {
        final Function function = new Function(
                FUNC_EMITEVENT, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(value1), 
                new org.web3j.abi.datatypes.generated.Uint256(value2), 
                new org.web3j.abi.datatypes.Utf8String(value3)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> emitEventBytes16(byte[] bytes16Value) {
        final Function function = new Function(
                FUNC_EMITEVENTBYTES16, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes16(bytes16Value)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> emitEventNotOrdered(byte[] value1, BigInteger value2, String value3) {
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

    public Flowable<DummyEventBytes16EventResponse> dummyEventBytes16EventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, DummyEventBytes16EventResponse>() {
            @Override
            public DummyEventBytes16EventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(DUMMYEVENTBYTES16_EVENT, log);
                DummyEventBytes16EventResponse typedResponse = new DummyEventBytes16EventResponse();
                typedResponse.log = log;
                typedResponse.indexedBytes16 = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.bytes16Value = (byte[]) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<DummyEventBytes16EventResponse> dummyEventBytes16EventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DUMMYEVENTBYTES16_EVENT));
        return dummyEventBytes16EventFlowable(filter);
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

    public Flowable<DummyEventEventResponse> dummyEventEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, DummyEventEventResponse>() {
            @Override
            public DummyEventEventResponse apply(Log log) {
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

    public Flowable<DummyEventEventResponse> dummyEventEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DUMMYEVENT_EVENT));
        return dummyEventEventFlowable(filter);
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

    public Flowable<DummyEventNotOrderedEventResponse> dummyEventNotOrderedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new io.reactivex.functions.Function<Log, DummyEventNotOrderedEventResponse>() {
            @Override
            public DummyEventNotOrderedEventResponse apply(Log log) {
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

    public Flowable<DummyEventNotOrderedEventResponse> dummyEventNotOrderedEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(DUMMYEVENTNOTORDERED_EVENT));
        return dummyEventNotOrderedEventFlowable(filter);
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
}
