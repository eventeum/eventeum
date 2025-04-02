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

package net.consensys.eventeum.dto.event.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;
import java.util.HashMap;
import java.util.Map;
import lombok.*;
import net.consensys.eventeum.service.exception.ValidationException;
import org.springframework.data.annotation.PersistenceConstructor;

/**
 * Supported event parameter types.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class ParameterType {

  public static final String INT = "INT";
  public static final String UINT = "UINT";
  public static final String ADDRESS = "ADDRESS";
  public static final String BYTES = "BYTES";
  public static final String BYTE = "BYTE";
  public static final String BOOL = "BOOL";
  public static final String STRING = "STRING";

  private static final Map<String, SupportedType> SUPPORTED_TYPES = new HashMap<>();

  static {
    SUPPORTED_TYPES.put(UINT, new SupportedType(UINT, 256, 8));
    SUPPORTED_TYPES.put(INT, new SupportedType(INT, 256, 8));
    SUPPORTED_TYPES.put(UINT, new SupportedType(UINT));
    SUPPORTED_TYPES.put(INT, new SupportedType(INT));
    SUPPORTED_TYPES.put(ADDRESS, new SupportedType(ADDRESS));
    SUPPORTED_TYPES.put(BYTES, new SupportedType(BYTES, 32, 1));
    SUPPORTED_TYPES.put(BYTE, new SupportedType(BYTE));
    SUPPORTED_TYPES.put(BOOL, new SupportedType(BOOL));
    SUPPORTED_TYPES.put(STRING, new SupportedType(STRING));
  }

  @JsonValue @Getter private String type;

  @PersistenceConstructor
  public ParameterType(String type) {
    setType(type);
  }

  public void setType(String type) {
    validateType(type);
    this.type = type;
  }

  @JsonCreator
  public static ParameterType build(String type) {
    final ParameterType paramType = new ParameterType();
    paramType.setType(type);

    return paramType;
  }

  // TODO regexp
  private void validateType(String type) {
    final String prefix = getPrefix(type);

    if (SUPPORTED_TYPES.containsKey(prefix)) {
      final SupportedType supportedType = SUPPORTED_TYPES.get(prefix);

      // Get size suffix, ignoring arrays
      if (type.contains("[")) {
        final String sizeString = type.substring(0, type.indexOf("[")).replace(prefix, "");

        if (supportedType.getMaxSizeSuffix() != null) {
          try {
            final Integer sizeSuffix = Integer.parseInt(sizeString);

            if (sizeSuffix % supportedType.getSizeInterval() != 0
                || sizeSuffix > supportedType.getMaxSizeSuffix()) {
              throw new ValidationException("Size suffix specified is not supported");
            }
          } catch (NumberFormatException t) {
            throw new ValidationException("Size suffix is not a valid number");
          }
        }
      }
    }
  }

  private String getPrefix(String type) {
    return type.replaceAll("\\D", "").replace("[", "").replace("]", "");
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class SupportedType {

    public SupportedType(String prefix) {
      this.prefix = prefix;
    }

    private String prefix;

    private Integer maxSizeSuffix;

    private Integer sizeInterval;
  }
}
