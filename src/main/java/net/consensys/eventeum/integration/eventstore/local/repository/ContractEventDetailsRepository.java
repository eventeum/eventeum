package net.consensys.eventeum.integration.eventstore.local.repository;

import net.consensys.eventeum.dto.event.ContractEventDetails;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("contractEventDetailRepository")
@ConditionalOnProperty(name = "eventStore.type", havingValue = "LOCAL")
public interface ContractEventDetailsRepository extends MongoRepository<ContractEventDetails, String> {
    Page<ContractEventDetails> findByEventSpecificationSignature(String signature, Pageable pagination);
}