package net.consensys.eventeum.chain.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterDefinition;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import org.web3j.abi.TypeReference;
import org.web3j.abi.Utils;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Useful Web3j related utility methods
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class Web3jUtil {

    private static Map<ParameterType, TypeMapping> typeMappings = new HashMap<ParameterType, TypeMapping>();

    static {
        //TODO need to add all the missing mappings
        typeMappings.put(ParameterType.UINT256, new TypeMapping(new TypeReference<Uint256>() {}, Uint256.class));
        typeMappings.put(ParameterType.ADDRESS, new TypeMapping(new TypeReference<Address>() {}, Address.class));
        typeMappings.put(ParameterType.BYTES32, new TypeMapping(new TypeReference<Bytes32>() {}, Bytes32.class));
        typeMappings.put(ParameterType.STRING, new TypeMapping(new TypeReference<Utf8String>() {}, Utf8String.class));
    }

    public static List<TypeReference<?>> getTypeReferencesFromParameterDefinitions(
            List<ParameterDefinition> parameterDefinitions) {
        if (parameterDefinitions == null || parameterDefinitions.isEmpty()) {
            return Collections.emptyList();
        }

        return parameterDefinitions
                .stream()
                .map(parameterDefinition -> getTypeReferenceFromParameterType(parameterDefinition.getType()))
                .collect(Collectors.toList());
    }

    public static TypeReference<?> getTypeReferenceFromParameterType(ParameterType parameterType) {
        return typeMappings.get(parameterType).getTypeReference();
    }

    public static Class<? extends Type> getClassForParameterType(ParameterType parameterType) {
        return typeMappings.get(parameterType).getClazz();
    }

    public static String getSignature(ContractEventSpecification spec) {

        final List<ParameterDefinition> allParameterDefinitions = new ArrayList<>();
        allParameterDefinitions.addAll(spec.getIndexedParameterDefinitions());
        allParameterDefinitions.addAll(spec.getNonIndexedParameterDefinitions());
        Collections.sort(allParameterDefinitions);

        return EventEncoder.encode(spec.getEventName(),
                Utils.convert(getTypeReferencesFromParameterDefinitions(allParameterDefinitions)));
    }

    @Data
    @AllArgsConstructor
    private static class TypeMapping {
        private TypeReference<?> typeReference;

        private Class<? extends Type> clazz;
    }
}
