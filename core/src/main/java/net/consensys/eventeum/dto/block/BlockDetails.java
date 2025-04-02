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

package net.consensys.eventeum.dto.block;

import java.math.BigInteger;
import lombok.Data;

/**
 * Represents the details of an Ethereum block.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Data
public class BlockDetails {
  private BigInteger number;

  private String hash;

  private BigInteger timestamp;

  private String nodeName;
}
