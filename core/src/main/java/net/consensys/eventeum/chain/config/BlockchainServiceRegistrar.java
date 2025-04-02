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

package net.consensys.eventeum.chain.config;

import lombok.Setter;
import net.consensys.eventeum.chain.settings.NodeSettings;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

public class BlockchainServiceRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

  @Setter private Environment environment;

  @Override
  public void registerBeanDefinitions(
      AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    final NodeSettings nodeSettings = getNodeSettings();

    nodeSettings
        .getNodes()
        .forEach(
            (name, node) -> getNodeBeanRegistratioStrategy(nodeSettings).register(node, registry));
  }

  protected NodeBeanRegistrationStrategy getNodeBeanRegistratioStrategy(NodeSettings nodeSettings) {
    return new NodeBeanRegistrationStrategy(nodeSettings, new OkHttpClient());
  }

  protected NodeSettings getNodeSettings() {
    return new NodeSettings(environment);
  }
}
