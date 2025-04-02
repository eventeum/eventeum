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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Embeddable;
import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * An array EventParameter, backed by an ArrayList. Its ArrayList rather than List as ArrayList
 * implements Serializable.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArrayParameter<T extends EventParameter<?>>
    extends AbstractEventParameter<ArrayList<T>> {

  @JsonIgnore private String stringRepresentation;

  public ArrayParameter(String entryType, Class<T> arrayParameterType, ArrayList<T> value) {
    super(entryType + "[]", value);

    initStringRepresentation();
  }

  @Override
  public String getValueString() {
    return stringRepresentation;
  }

  private void initStringRepresentation() {
    final StringBuilder builder = new StringBuilder("[");

    getValue()
        .forEach(
            param -> {
              builder.append("\"");
              builder.append(param.getValueString());
              builder.append("\"");
              builder.append(",");
            });

    builder.deleteCharAt(builder.length() - 1);
    builder.append("]");

    stringRepresentation = builder.toString();
  }
}
