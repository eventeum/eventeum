package net.consensys.eventeum.integration.mixin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = SimplePageImpl.class)
public interface PageMixIn {
}
