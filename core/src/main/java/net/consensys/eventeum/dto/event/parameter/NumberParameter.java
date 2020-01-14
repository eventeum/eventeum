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

package net.consensys.eventeum.dto.event.parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

import javax.persistence.Embeddable;

/**
 * A number based EventParameter, represented by a BigInteger.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Embeddable
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
