package net.consensys.eventeum.chain.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.consensys.eventeum.dto.event.filter.ContractEventSpecification;
import net.consensys.eventeum.dto.event.filter.ParameterType;
import org.web3j.abi.TypeReference;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static List<TypeReference<?>> getTypeReferencesFromParameterTypes(List<ParameterType> parameterTypes) {
        if (parameterTypes == null || parameterTypes.isEmpty()) {
            return Collections.emptyList();
        }

        return parameterTypes
                .stream()
                .map(parameterType -> getTypeReferenceFromParameterType(parameterType))
                .collect(Collectors.toList());
    }

    public static TypeReference<?> getTypeReferenceFromParameterType(ParameterType parameterType) {
        return typeMappings.get(parameterType).getTypeReference();
    }

    public static Class<? extends Type> getClassForParameterType(ParameterType parameterType) {
        return typeMappings.get(parameterType).getClazz();
    }

    public static String getSignature(ContractEventSpecification spec) {
        final Event event = new Event(spec.getEventName(),
                Web3jUtil.getTypeReferencesFromParameterTypes(spec.getIndexedParameterTypes()),
                Web3jUtil.getTypeReferencesFromParameterTypes(spec.getNonIndexedParameterTypes()));

        return EventEncoder.encode(event);
    }

    @Data
    @AllArgsConstructor
    private static class TypeMapping {
        private TypeReference<?> typeReference;

        private Class<? extends Type> clazz;
    }
}
