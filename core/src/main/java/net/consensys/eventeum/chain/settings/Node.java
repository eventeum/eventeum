package net.consensys.eventeum.chain.settings;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Node {

    private String name;

    private String url;
    private Long pollingInterval;
    private String username;
    private String password;
}
