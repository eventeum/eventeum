package net.consensys.eventeum.chain.converter;

import net.consensys.eventeum.dto.event.parameter.EventParameter;
import net.consensys.eventeum.dto.event.parameter.NumberParameter;
import net.consensys.eventeum.dto.event.parameter.StringParameter;
import net.consensys.eventeum.settings.EventeumSettings;
import org.springframework.stereotype.Component;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Web3j Type objects into Eventeum EventParameter objects.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Component("web3jEventParameterConverter")
public class Web3jEventParameterConverter implements EventParameterConverter<Type> {

    private Map<String, EventParameterConverter<Type>> typeConverters = new HashMap<String, EventParameterConverter<Type>>();

    private EventeumSettings settings;

    public Web3jEventParameterConverter(EventeumSettings settings) {
        typeConverters.put("address",
                (type) -> new StringParameter(type.getTypeAsString(), Keys.toChecksumAddress(type.toString())));
        typeConverters.put("uint8",
                (type) -> new NumberParameter(type.getTypeAsString(), (BigInteger) type.getValue()));
        typeConverters.put("uint256",
                (type) -> new NumberParameter(type.getTypeAsString(), (BigInteger) type.getValue()));
        typeConverters.put("int256",
                (type) -> new NumberParameter(type.getTypeAsString(), (BigInteger) type.getValue()));
        typeConverters.put("bytes16",
                (type) -> convertBytesType(type));
        typeConverters.put("bytes32",
                (type) -> convertBytesType(type));
        typeConverters.put("string",
                (type) -> new StringParameter(type.getTypeAsString(),
                        trim((String)type.getValue())));

        this.settings = settings;
    }

    @Override
    public EventParameter convert(Type toConvert) {
        final EventParameterConverter<Type> typeConverter = typeConverters.get(toConvert.getTypeAsString().toLowerCase());

        if (typeConverter == null) {
            throw new TypeConversionException("Unsupported type: " + toConvert.getTypeAsString());
        }

        return typeConverter.convert(toConvert);
    }

    private EventParameter convertBytesType(Type bytesType) {
        if (settings.isBytesToAscii()) {
            return new StringParameter(
                    bytesType.getTypeAsString(), trim(new String((byte[]) bytesType.getValue())));
        }

        return new StringParameter(
                bytesType.getTypeAsString(), trim(Numeric.toHexString((byte[]) bytesType.getValue())));
    }

    private String trim(String toTrim) {
        return toTrim
                .trim()
                .replace("\\u0000", "");
    }
}
