package net.consensys.eventeum.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractMessage<T> implements Message<T> {

    private String id;

    private String type;

    private T details;
}