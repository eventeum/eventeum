package net.consensys.eventeum.dto.event.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import lombok.*;
import net.consensys.eventeum.service.exception.ValidationException;

@Embeddable
@NoArgsConstructor
public class ParameterDefinition implements Comparable<ParameterDefinition>, Serializable {

    public static final String INT = "INT";
    public static final String UINT = "UINT";
    public static final String ADDRESS = "ADDRESS";
    public static final String BYTES = "BYTES";
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
        SUPPORTED_TYPES.put(BOOL, new SupportedType(BOOL));
        SUPPORTED_TYPES.put(STRING, new SupportedType(STRING));
    }

    @Getter @Setter
    private Integer position;

    @Getter
    private String type;

    public ParameterDefinition(Integer position, String type) {
        setType(type);
        setPosition(position);
    }

    public void setType(String type) {
        validateType(type);
        this.type = type;
    }

    @Override
    public int compareTo(ParameterDefinition o) {
        return this.position.compareTo(o.getPosition());
    }

    private void validateType(String type) {
        final String prefix = getPrefix(type);

        if (SUPPORTED_TYPES.containsKey(prefix)) {
            final SupportedType supportedType = SUPPORTED_TYPES.get(prefix);

            //Get size suffix, ignoring arrays
            if (type.contains("[")) {
                final String sizeString = type
                        .substring(0, type.indexOf("["))
                        .replace(prefix, "");

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
        return type
                .replaceAll("\\D", "")
                .replace("[", "")
                .replace("]", "");
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
