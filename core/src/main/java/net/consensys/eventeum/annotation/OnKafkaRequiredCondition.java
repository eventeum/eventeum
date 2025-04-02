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

package net.consensys.eventeum.annotation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/** Mostly taken from org.springframework.boot.autoconfigure.condition.OnExpressionCondition */
@Order(Ordered.LOWEST_PRECEDENCE - 20)
class OnKafkaRequiredCondition extends OnMultiExpressionCondition {

  private static final String KAFKA_REQUIRED_EXPRESSION =
      "'${broadcaster.multiInstance}' == 'true' || '${broadcaster.type}' == 'KAFKA'";

  private static final String KAFKA_NOT_REQUIRED_EXPRESSION =
      "'${broadcaster.multiInstance}' == 'false' && '${broadcaster.type}' != 'KAFKA'";

  public OnKafkaRequiredCondition() {
    super(
        KAFKA_REQUIRED_EXPRESSION, KAFKA_NOT_REQUIRED_EXPRESSION, ConditionalOnKafkaRequired.class);
  }
}
