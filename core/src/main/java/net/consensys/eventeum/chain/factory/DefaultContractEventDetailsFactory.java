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

package net.consensys.eventeum.chain.factory;

import java.util.ArrayList;
import java.util.Collections;
import net.consensys.eventeum.chain.converter.EventParameterConverter;
import net.consensys.eventeum.chain.settings.Node;
import net.consensys.eventeum.chain.util.Web3jUtil;
import net.consensys.eventeum.chain.converter.EventParameterConverter;
import net.consensys.eventeum.dto.event.ContractEventDetails;
import net.consensys.eventeum.dto.event.ContractEventStatus;
import net.consensys.eventeum.dto.event.filter.ContractEventFilter;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.parameter.EventParameter;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultContractEventDetailsFactory implements ContractEventDetailsFactory {

    private EventParameterConverter<Type> parameterConverter;
    private Node node;
    private String networkName;

    public DefaultContractEventDetailsFactory(EventParameterConverter<Type> parameterConverter,
                                              Node node,
                                              String networkName) {
        this.parameterConverter = parameterConverter;
        this.node = node;
        this.networkName = networkName;
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
        eventDetails.setAddress(Keys.toChecksumAddress(log.getAddress()));
        eventDetails.setLogIndex(log.getLogIndex());
        eventDetails.setTransactionHash(log.getTransactionHash());
        eventDetails.setBlockNumber(log.getBlockNumber());
        eventDetails.setBlockHash(log.getBlockHash());
        eventDetails.setEventSpecificationSignature(Web3jUtil.getSignature(eventSpec));
        eventDetails.setNetworkName(this.networkName);
        eventDetails.setNodeName(eventFilter.getNode());

        if (log.isRemoved()) {
            eventDetails.setStatus(ContractEventStatus.INVALIDATED);
        } else if (node.getBlocksToWaitForConfirmation().equals(BigInteger.ZERO)) {
            //Set to confirmed straight away if set to zero confirmations
            eventDetails.setStatus(ContractEventStatus.CONFIRMED);
        } else {
            eventDetails.setStatus(ContractEventStatus.UNCONFIRMED);
        }

        return eventDetails;
    }

    private List<EventParameter> typeListToParameterList(List<Type> typeList) {
        if (isNullOrEmpty(typeList)) {
            return Collections.EMPTY_LIST;
        }

        return typeList
                .stream()
                .map(type -> parameterConverter.convert(type))
                .collect(Collectors.toList());
    }

    private List<Type> getNonIndexedParametersFromLog(ContractEventSpecification eventSpec, Log log) {
        if (isNullOrEmpty(eventSpec.getNonIndexedParameterDefinitions())) {
            return Collections.EMPTY_LIST;
        }

        List<ParameterDefinition> orderedParams = new ArrayList<>(eventSpec.getNonIndexedParameterDefinitions());
        orderedParams.sort((p1, p2) -> p1.getPosition().compareTo(p2.getPosition()));

        return FunctionReturnDecoder.decode(
                log.getData(),
                Utils.convert(Web3jUtil.getTypeReferencesFromParameterDefinitions(
                        orderedParams)));
    }

    private List<Type> getIndexedParametersFromLog(ContractEventSpecification eventSpec, Log log) {
        if (isNullOrEmpty(eventSpec.getIndexedParameterDefinitions())) {
            return Collections.EMPTY_LIST;
        }

        final List<String> encodedParameters = log.getTopics().subList(1, log.getTopics().size());
        final List<ParameterDefinition> definitions = eventSpec.getIndexedParameterDefinitions();

        return IntStream.range(0, encodedParameters.size())
                .mapToObj(i -> FunctionReturnDecoder.decodeIndexedValue(encodedParameters.get(i),
                        Web3jUtil.getTypeReferenceFromParameterType(definitions.get(i).getType())))
                .collect(Collectors.toList());
    }

    private boolean isNullOrEmpty(List<?> toCheck) {
        return toCheck == null || toCheck.isEmpty();
    }
}
