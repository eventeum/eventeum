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

package net.consensys.eventeum.chain.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import net.consensys.eventeum.dto.event.parameter.EventParameter;
import net.consensys.eventeum.settings.EventeumSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

public class Web3jEventParameterConverterTest {
  private static String ADDRESS = "0xBb4F53C05e50574C5fEdbFE89c13Cc5fEb634ae3";

  private Web3jEventParameterConverter underTest;

  @BeforeEach
  public void init() {
    underTest = new Web3jEventParameterConverter(new EventeumSettings(true, "10000"));
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
  public void testUint8Conversion() {
    final EventParameter<BigInteger> result = underTest.convert(new Uint8(10));

    assertEquals(BigInteger.TEN, result.getValue());
  }

  @Test
  public void testBytes32Conversion() {
    final byte[] bytes =
        new BigInteger("61546f7069630000000000000000000000000000000000000000000000000000", 16)
            .toByteArray();
    final Bytes32 value = new Bytes32(bytes);

    final EventParameter<String> result = underTest.convert(value);

    assertEquals("aTopic", result.getValue());
  }

  @Test
  public void testInvalidTypeConversion() {
    Assertions.assertThrows(
        TypeConversionException.class, () -> underTest.convert(new InvalidType()));
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
