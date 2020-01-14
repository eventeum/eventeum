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

package net.consensys.eventeum.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.consensys.eventeum.dto.block.BlockDetails;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigInteger;

import javax.persistence.Entity;

@Document
@Entity
@Data
@NoArgsConstructor
public class LatestBlock {

    public LatestBlock(BlockDetails blockDetails) {
        this.nodeName = blockDetails.getNodeName();
        this.number = blockDetails.getNumber();
        this.hash = blockDetails.getHash();
        this.timestamp = blockDetails.getTimestamp();
    }

    @javax.persistence.Id
    @Id
    private String nodeName;

    private BigInteger number;

    private String hash;

    private BigInteger timestamp;
}
