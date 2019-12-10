package net.consensys.eventeum.dto.message;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractMessage<T> implements EventeumMessage<T>, Serializable{

    private String id;

    private String type;

    private T details;

    private Integer retries = 0;

    public AbstractMessage(String id, String type, T details) {
        this.id = id;
        this.type = type;
        this.details = details;
    }
}