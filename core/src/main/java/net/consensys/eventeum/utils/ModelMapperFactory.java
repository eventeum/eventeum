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

package net.consensys.eventeum.utils;

import net.consensys.eventeum.chain.service.domain.converter.BlockResponseToHederaBlockConverter;
import net.consensys.eventeum.chain.service.domain.converter.ContractResultResponseToTransactionConverter;
import net.consensys.eventeum.chain.service.domain.converter.EthBlockToWeb3jBlockConverter;
import org.modelmapper.ModelMapper;

/**
 * A singleton factory for creating ModelMapper instances.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class ModelMapperFactory {

  private static ModelMapperFactory INSTANCE;
  private static ModelMapper MODEL_MAPPER_INSTANCE;

  private ModelMapperFactory() {}

  public static ModelMapperFactory getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ModelMapperFactory();
    }

    return INSTANCE;
  }

  public ModelMapper getModelMapper() {
    if (MODEL_MAPPER_INSTANCE == null) {
      MODEL_MAPPER_INSTANCE = new ModelMapper();
      MODEL_MAPPER_INSTANCE.addConverter(new EthBlockToWeb3jBlockConverter());
      MODEL_MAPPER_INSTANCE.addConverter(new BlockResponseToHederaBlockConverter());
      MODEL_MAPPER_INSTANCE.addConverter(new ContractResultResponseToTransactionConverter());
    }
    return MODEL_MAPPER_INSTANCE;
  }
}
