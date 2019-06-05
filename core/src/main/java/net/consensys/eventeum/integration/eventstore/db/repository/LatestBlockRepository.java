package net.consensys.eventeum.integration.eventstore.db.repository;

import net.consensys.eventeum.factory.EventStoreFactory;
import net.consensys.eventeum.model.LatestBlock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository("latestBlockRepository")
@ConditionalOnProperty(name = "eventStore.type", havingValue = "DB")
@ConditionalOnMissingBean(EventStoreFactory.class)
public interface LatestBlockRepository extends MongoRepository<LatestBlock, String> {
}
