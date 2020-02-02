package io.keyko.monitoring.agent.core.utils;

import org.modelmapper.ModelMapper;

/**
 * A singleton factory for creating ModelMapper instances.
 *
 * @author Craig Williams <craig.williams@consensys.net>
 */
public class ModelMapperFactory {

    private static ModelMapperFactory INSTANCE;

    private ModelMapperFactory() {
    }

    public static ModelMapperFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModelMapperFactory();
        }

        return INSTANCE;
    }

    public ModelMapper createModelMapper() {
        return new ModelMapper();
    }
}
