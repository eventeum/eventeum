package net.consensys.eventeum.chain.service.domain.io;

import com.fasterxml.jackson.annotation.*;
import jakarta.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "address",
  "bloom",
  "contract_id",
  "data",
  "index",
  "topics",
  "block_hash",
  "block_number",
  "root_contract_id",
  "timestamp",
  "transaction_hash",
  "transaction_index"
})
@Generated("jsonschema2pojo")
public class LogHederaMirrorNodeResponse {

  @JsonIgnore
  private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

  @JsonProperty("address")
  private String address;

  @JsonProperty("bloom")
  private String bloom;

  @JsonProperty("contract_id")
  private String contractId;

  @JsonProperty("data")
  private String data;

  @JsonProperty("index")
  private Integer index;

  @JsonProperty("topics")
  private List<String> topics;

  @JsonProperty("block_hash")
  private String blockHash;

  @JsonProperty("block_number")
  private Integer blockNumber;

  @JsonProperty("root_contract_id")
  private String rootContractId;

  @JsonProperty("timestamp")
  private String timestamp;

  @JsonProperty("transaction_hash")
  private String transactionHash;

  @JsonProperty("transaction_index")
  private Integer transactionIndex;

  @JsonProperty("address")
  public String getAddress() {
    return address;
  }

  @JsonProperty("address")
  public void setAddress(String address) {
    this.address = address;
  }

  @JsonProperty("bloom")
  public String getBloom() {
    return bloom;
  }

  @JsonProperty("bloom")
  public void setBloom(String bloom) {
    this.bloom = bloom;
  }

  @JsonProperty("contract_id")
  public String getContractId() {
    return contractId;
  }

  @JsonProperty("contract_id")
  public void setContractId(String contractId) {
    this.contractId = contractId;
  }

  @JsonProperty("data")
  public String getData() {
    return data;
  }

  @JsonProperty("data")
  public void setData(String data) {
    this.data = data;
  }

  @JsonProperty("index")
  public Integer getIndex() {
    return index;
  }

  @JsonProperty("index")
  public void setIndex(Integer index) {
    this.index = index;
  }

  @JsonProperty("topics")
  public List<String> getTopics() {
    return topics;
  }

  @JsonProperty("topics")
  public void setTopics(List<String> topics) {
    this.topics = topics;
  }

  @JsonProperty("block_hash")
  public String getBlockHash() {
    return blockHash;
  }

  @JsonProperty("block_hash")
  public void setBlockHash(String blockHash) {
    this.blockHash = blockHash;
  }

  @JsonProperty("block_number")
  public Integer getBlockNumber() {
    return blockNumber;
  }

  @JsonProperty("block_number")
  public void setBlockNumber(Integer blockNumber) {
    this.blockNumber = blockNumber;
  }

  @JsonProperty("root_contract_id")
  public String getRootContractId() {
    return rootContractId;
  }

  @JsonProperty("root_contract_id")
  public void setRootContractId(String rootContractId) {
    this.rootContractId = rootContractId;
  }

  @JsonProperty("timestamp")
  public String getTimestamp() {
    return timestamp;
  }

  @JsonProperty("timestamp")
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  @JsonProperty("transaction_hash")
  public String getTransactionHash() {
    return transactionHash;
  }

  @JsonProperty("transaction_hash")
  public void setTransactionHash(String transactionHash) {
    this.transactionHash = transactionHash;
  }

  @JsonProperty("transaction_index")
  public Integer getTransactionIndex() {
    return transactionIndex;
  }

  @JsonProperty("transaction_index")
  public void setTransactionIndex(Integer transactionIndex) {
    this.transactionIndex = transactionIndex;
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
