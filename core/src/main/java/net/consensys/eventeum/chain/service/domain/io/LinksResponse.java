package net.consensys.eventeum.chain.service.domain.io;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"next"})
@Generated("jsonschema2pojo")
public class LinksResponse {

  @JsonIgnore
  private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

  @JsonProperty("next")
  private String next;

  @JsonProperty("next")
  public String getNext() {
    return next;
  }

  @JsonProperty("next")
  public void setNext(String next) {
    this.next = next;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {
    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {
    this.additionalProperties.put(name, value);
  }
}
