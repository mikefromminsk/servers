package org.vavilon.servers.model;

import java.util.Map;

public class Message {
    public String channel;
    public Map<String, String> data;

    public Message(String channel, Map<String, String> data) {
        this.channel = channel;
        this.data = data;
    }
}
