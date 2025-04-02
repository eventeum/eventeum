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

package net.consensys.eventeum.chain.block.tx.criteria.factory;

import net.consensys.eventeum.chain.block.tx.criteria.*;
import net.consensys.eventeum.model.TransactionIdentifierType;
import net.consensys.eventeum.model.TransactionMonitoringSpec;
import org.springframework.stereotype.Component;

@Component
public class DefaultTransactionMatchingCriteriaFactory
    implements TransactionMatchingCriteriaFactory {

  @Override
  public TransactionMatchingCriteria build(TransactionMonitoringSpec spec) {
    if (spec.getType() == TransactionIdentifierType.HASH) {
      return new TxHashMatchingCriteria(
          spec.getNodeName(), spec.getTransactionIdentifierValue(), spec.getStatuses());
    }

    if (spec.getType() == TransactionIdentifierType.TO_ADDRESS) {
      return new ToAddressMatchingCriteria(
          spec.getNodeName(), spec.getTransactionIdentifierValue(), spec.getStatuses());
    }

    if (spec.getType() == TransactionIdentifierType.FROM_ADDRESS) {
      return new FromAddressMatchingCriteria(
          spec.getNodeName(), spec.getTransactionIdentifierValue(), spec.getStatuses());
    }

    if (spec.getType() == TransactionIdentifierType.TOPIC) {
      return new TopicMatchingCriteria(
          spec.getNodeName(), spec.getTransactionIdentifierValue(), spec.getStatuses());
    }

    throw new UnsupportedOperationException("Type: " + spec.getType() + " not currently supported");
  }
}
