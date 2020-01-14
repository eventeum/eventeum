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

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@Configuration
@Import({DatabaseConfiguration.WithMongo.class, DatabaseConfiguration.WithJpa.class})
public class DatabaseConfiguration {

    @ConditionalOnProperty(name= "database.type", havingValue="MONGO")
    @EnableAutoConfiguration(
            exclude = {DataSourceAutoConfiguration.class,   DataSourceTransactionManagerAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
    @EnableMongoRepositories(basePackages = {BaseConfiguration.BASE_PACKAGE})
    static class WithMongo {

    }

    @ConditionalOnProperty(name= "database.type", havingValue="SQL")
    @EnableJpaRepositories(basePackages = {BaseConfiguration.BASE_PACKAGE})
    @EnableAutoConfiguration(
            exclude = {MongoAutoConfiguration.class,   MongoDataAutoConfiguration.class})
    static class WithJpa {

    }
}
