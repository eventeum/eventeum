package net.consensys.eventeum.dto.event.parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * A number based EventParameter, represented by a BigInteger.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@NoArgsConstructor
public class NumberParameter extends AbstractEventParameter<BigInteger> {

    public NumberParameter(String type, BigInteger value) {
        super(type, value);
    }

    @Override
    public String getValueString() {
        return getValue().toString();
    }
}
