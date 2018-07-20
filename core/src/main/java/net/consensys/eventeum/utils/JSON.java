package net.consensys.eventeum.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Useful JSON based utility methods.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class JSON {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String stringify(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "<Unable to convert to JSON>";
        }
    }
}
