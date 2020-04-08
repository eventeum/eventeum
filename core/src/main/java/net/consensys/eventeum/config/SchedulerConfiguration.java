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

package net.consensys.eventeum.config;

import lombok.extern.slf4j.Slf4j;
import net.consensys.eventeum.chain.service.container.NodeServices;
import net.consensys.eventeum.chain.settings.NodeSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Configuration
@EnableScheduling
@EnableAsync
@EnableRetry
@Slf4j
public class SchedulerConfiguration implements SchedulingConfigurer {


		private ScheduledExecutorService scheduledExecutorService;


		@Override
		public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
			taskRegistrar.setScheduler(scheduledExecutorService);
		}

		@Bean(destroyMethod="shutdown")
		public Executor taskScheduler(NodeSettings nodeSettings) {

			scheduledExecutorService = Executors.newScheduledThreadPool(nodeSettings.getNodes().size(), new CustomizableThreadFactory("eventeum-scheduler"));
			return  scheduledExecutorService;
		}
}
