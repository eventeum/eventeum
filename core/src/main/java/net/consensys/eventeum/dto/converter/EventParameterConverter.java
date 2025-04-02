package net.consensys.eventeum.dto.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import net.consensys.eventeum.dto.event.parameter.EventParameter;

@Converter(autoApply = true)
public class EventParameterConverter implements AttributeConverter<EventParameter, byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(EventParameter attribute) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsBytes(attribute);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error serialising EventParameters", e);
    }
  }

  @Override
  public EventParameter convertToEntityAttribute(byte[] dbData) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(dbData, new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException("Error serialising EventParameters", e);
    }
  }
}
