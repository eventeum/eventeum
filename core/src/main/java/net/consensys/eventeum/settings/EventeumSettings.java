package net.consensys.eventeum.settings;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class EventeumSettings {

    private boolean bytesToAscii;

    public EventeumSettings(@Value("${broadcaster.bytesToAscii:false}") boolean bytesToAscii) {
        this.bytesToAscii = bytesToAscii;
    }
}
