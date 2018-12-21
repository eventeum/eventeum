package net.consensys.eventeum.chain.converter;

import net.consensys.eventeum.dto.event.parameter.EventParameter;
import net.consensys.eventeum.dto.event.parameter.NumberParameter;
import net.consensys.eventeum.dto.event.parameter.StringParameter;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Type;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Web3j Type objects into Eventeum EventParameter objects.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component
public class Web3jEventParameterConverter implements EventParameterConverter<Type> {

    private Map<String, EventParameterConverter<Type>> typeConverters = new HashMap<String, EventParameterConverter<Type>>();

    public Web3jEventParameterConverter() {
        typeConverters.put("address",
                (type) -> new StringParameter(type.getTypeAsString(), type.toString()));
        typeConverters.put("uint8",
                (type) -> new NumberParameter(type.getTypeAsString(), (BigInteger) type.getValue()));
        typeConverters.put("uint256",
                (type) -> new NumberParameter(type.getTypeAsString(), (BigInteger) type.getValue()));
        typeConverters.put("int256",
                (type) -> new NumberParameter(type.getTypeAsString(), (BigInteger) type.getValue()));
        typeConverters.put("bytes32",
                (type) -> new StringParameter(type.getTypeAsString(),
                        trim(new String((byte[]) type.getValue()))));
        typeConverters.put("bytes32Hex",
                (type) -> new StringParameter(type.getTypeAsString(),
                        trim(Numeric.toHexString((byte[]) type.getValue()))));
        typeConverters.put("string",
                (type) -> new StringParameter(type.getTypeAsString(),
                        trim((String)type.getValue())));
    }

    @Override
    public EventParameter convert(Type toConvert) {
        final EventParameterConverter<Type> typeConverter = typeConverters.get(toConvert.getTypeAsString().toLowerCase());

        if (typeConverter == null) {
            throw new TypeConversionException("Unsupported type: " + toConvert.getTypeAsString());
        }

        return typeConverter.convert(toConvert);
    }

    private String trim(String toTrim) {
        return toTrim
                .trim()
                .replace("\\u0000", "");
    }
}
