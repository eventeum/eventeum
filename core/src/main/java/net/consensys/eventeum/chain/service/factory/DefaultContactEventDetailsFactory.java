package net.consensys.eventeum.chain.service.factory;

import net.consensys.eventeum.chain.config.EventConfirmationConfig;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.chain.converter.EventParameterConverter;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import net.consensys.eventeum.dto.event.parameter.EventParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class DefaultContactEventDetailsFactory implements ContractEventDetailsFactory {

    private EventParameterConverter<Type> parameterConverter;
    private EventConfirmationConfig eventConfirmationConfig;

    @Autowired
    DefaultContactEventDetailsFactory(EventParameterConverter<Type> parameterConverter,
                                      EventConfirmationConfig eventConfirmationConfig) {
        this.parameterConverter = parameterConverter;
        this.eventConfirmationConfig = eventConfirmationConfig;
    }

    @Override
    public ContractEventDetails createEventDetails(ContractEventFilter eventFilter, Log log) {
        final ContractEventSpecification eventSpec = eventFilter.getEventSpecification();

        final List<EventParameter> nonIndexed = typeListToParameterList(getNonIndexedParametersFromLog(eventSpec, log));
        final List<EventParameter> indexed = typeListToParameterList(getIndexedParametersFromLog(eventSpec, log));

        final ContractEventDetails eventDetails = new ContractEventDetails();
        eventDetails.setName(eventSpec.getEventName());
        eventDetails.setFilterId(eventFilter.getId());
        eventDetails.setNonIndexedParameters(nonIndexed);
        eventDetails.setIndexedParameters(indexed);
        eventDetails.setAddress(log.getAddress());
        eventDetails.setLogIndex(log.getLogIndex());
        eventDetails.setTransactionHash(log.getTransactionHash());
        eventDetails.setBlockNumber(log.getBlockNumber());
        eventDetails.setBlockHash(log.getBlockHash());
        eventDetails.setEventSpecificationSignature(Web3jUtil.getSignature(eventSpec));

        if (log.isRemoved()) {
            eventDetails.setStatus(ContractEventStatus.INVALIDATED);
        } else if (eventConfirmationConfig.getBlocksToWaitForConfirmation().equals(BigInteger.ZERO)) {
            //Set to confirmed straight away if set to zero confirmations
            eventDetails.setStatus(ContractEventStatus.CONFIRMED);
        } else {
            eventDetails.setStatus(ContractEventStatus.UNCONFIRMED);
        }

        return eventDetails;
    }

    private List<EventParameter> typeListToParameterList(List<Type> typeList) {
        return typeList
                .stream()
                .map(type -> parameterConverter.convert(type))
                .collect(Collectors.toList());
    }

    private List<Type> getNonIndexedParametersFromLog(ContractEventSpecification eventSpec, Log log) {
        return FunctionReturnDecoder.decode(
                log.getData(),
                Utils.convert(Web3jUtil.getTypeReferencesFromParameterDefinitions(
                        eventSpec.getNonIndexedParameterDefinitions())));
    }

    private List<Type> getIndexedParametersFromLog(ContractEventSpecification eventSpec, Log log) {
        final List<String> encodedParameters = log.getTopics().subList(1, log.getTopics().size());
        final List<ParameterDefinition> definitions = eventSpec.getIndexedParameterDefinitions();

        return IntStream.range(0, encodedParameters.size())
                .mapToObj(i -> FunctionReturnDecoder.decodeIndexedValue(encodedParameters.get(i),
                        Web3jUtil.getTypeReferenceFromParameterType(definitions.get(i).getType())))
                .collect(Collectors.toList());
    }
}