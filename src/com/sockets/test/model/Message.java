package com.sockets.test.model;

public class Message {

    public String channel;
    public Object data;

    public Message(String channel) {
        this.channel = channel;
    }
}
