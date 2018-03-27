package net.consensys.eventeum.chain.converter;

import net.consensys.eventeum.dto.event.parameter.EventParameter;
import org.junit.Before;
import org.junit.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class Web3jEventParameterConverterTest {
    private static String ADDRESS = "0xbb4f53c05e50574c5fedbfe89c13cc5feb634ae3";

    private Web3jEventParameterConverter underTest;

    @Before
    public void init() {
        underTest = new Web3jEventParameterConverter();
    }

    @Test
    public void testAddressConversion() {
        final EventParameter<String> result = underTest.convert(new Address(ADDRESS));

        assertEquals(ADDRESS, result.getValue());
    }

    @Test
    public void testUint256Conversion() {
        final EventParameter<BigInteger> result = underTest.convert(new Uint256(10));

        assertEquals(BigInteger.TEN, result.getValue());
    }

    @Test
    public void testBytes32Conversion() {
        final byte[] bytes = new BigInteger("61546f7069630000000000000000000000000000000000000000000000000000", 16).toByteArray();
        final Bytes32 value = new Bytes32(bytes);

        final EventParameter<String> result = underTest.convert(value);

        assertEquals("aTopic", result.getValue());
    }

    @Test(expected = TypeConversionException.class)
    public void testInvalidTypeConversion() {
        underTest.convert(new InvalidType());
    }

    private class InvalidType implements Type {

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public String getTypeAsString() {
            return "invalid";
        }
    }
}
