package net.consensys.eventeum.dto.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
/*
 * Class used to convert to JSON strings attributes that are Map<String, Object> like
 * , for example, the "extensions" attribute in ContractEventFilter.java or ContractEventDetails.java.
 * This is useful when using SQL DD.BBs. Other approaches could be used, like for example storing data in a
 * specific table of extensions.
 * */
public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {
  private static ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, Object> customerInfo) {

    String customerInfoJson = null;
    try {
      customerInfoJson = objectMapper.writeValueAsString(customerInfo);
    } catch (final JsonProcessingException e) {
      log.error("JSON writing error", e);
    }

    return customerInfoJson;
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String customerInfoJSON) {

    Map<String, Object> customerInfo = null;
    try {
      customerInfo = objectMapper.readValue(customerInfoJSON, Map.class);
    } catch (final IOException e) {
      log.error("JSON reading error", e);
    }

    return customerInfo;
  }
}
