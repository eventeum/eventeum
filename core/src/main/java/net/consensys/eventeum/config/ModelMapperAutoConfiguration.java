package net.consensys.eventeum.config;

import java.util.List;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ModelMapper.class)
public class ModelMapperAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(ModelMapper.class)
  public ModelMapper modelMapper(List<Converter<?, ?>> converters) {
    ModelMapper modelMapper = new ModelMapper();
    converters.forEach(modelMapper::addConverter);
    return modelMapper;
  }
}
