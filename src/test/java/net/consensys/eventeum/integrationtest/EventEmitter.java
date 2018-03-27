package net.consensys.eventeum.integrationtest;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.0.2.
 */
public final class EventEmitter extends Contract {
    private static final String BINARY = "606060405234610000575b60e8806100186000396000f300606060405263ffffffff60e060020a600035041663f6af6ace81146022575b6000565b34600057603f600480359060248035916044359182019101356041565b005b3373ffffffffffffffffffffffffffffffffffffffff1684600019167f46aca551d5bafd01d98f8cadeb9b50f1b3ee44c33007f2a13d969dab7e7cf2a8858585604051808481526020018060200182810382528484828181526020019250808284376040519201829003965090945050505050a35b505050505600a165627a7a723058201595c818962a620329f853291cdfc765cca7d39088055909dbd1dd4e15af3e960029";

    private EventEmitter(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private EventEmitter(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<DummyEventEventResponse> getDummyEventEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("DummyEvent", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<DummyEventEventResponse> responses = new ArrayList<DummyEventEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            DummyEventEventResponse typedResponse = new DummyEventEventResponse();
            typedResponse.indexedBytes = (byte[]) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.indexedAddress = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.uintValue = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.stringValue = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<DummyEventEventResponse> dummyEventEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("DummyEvent", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, DummyEventEventResponse>() {
            @Override
            public DummyEventEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                DummyEventEventResponse typedResponse = new DummyEventEventResponse();
                typedResponse.indexedBytes = (byte[]) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.indexedAddress = (String) eventValues.getIndexedValues().get(1).getValue();
                typedResponse.uintValue = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.stringValue = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<TransactionReceipt> emit(byte[] value1, BigInteger value2, String value3) {
        Function function = new Function(
                "emit", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(value1), 
                new org.web3j.abi.datatypes.generated.Uint256(value2), 
                new org.web3j.abi.datatypes.Utf8String(value3)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<EventEmitter> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(EventEmitter.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<EventEmitter> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(EventEmitter.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static EventEmitter load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new EventEmitter(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static EventEmitter load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new EventEmitter(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class DummyEventEventResponse {
        public byte[] indexedBytes;

        public String indexedAddress;

        public BigInteger uintValue;

        public String stringValue;
    }
}
