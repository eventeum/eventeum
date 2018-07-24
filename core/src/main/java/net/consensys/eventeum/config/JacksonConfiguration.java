package net.consensys.eventeum.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import net.consensys.eventeum.dto.message.*;
import net.consensys.eventeum.integration.mixin.PageMixIn;
import net.consensys.kafkadl.message.RetryableMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

/**
 * Configures the jackson ObjectMapper bean.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(Page.class, PageMixIn.class);

        return mapper;
    }
}
