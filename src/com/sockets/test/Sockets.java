package com.sockets.test;

import java.net.InetSocketAddress;
import java.util.*;

import com.google.gson.Gson;
import com.sockets.test.model.Subscription;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Sockets extends WebSocketServer {

    Gson json = new Gson();
    static Map<String, HashSet<WebSocket>> channels = new HashMap<>();

    public Sockets(int port) {
        super(new InetSocketAddress(port));
    }


    int connections = 0;

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections++;
        String time = new Date().toString();
        System.out.println(connections + " connected" + time);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections--;
        String time = new Date().toString();
        System.out.println(connections + " connected" + time);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        Subscription sub = json.fromJson(message, Subscription.class);
        if (sub.subscribe != null) {
            if (!channels.containsKey(sub.subscribe)) {
                channels.put(sub.subscribe, new HashSet<>());
            }
            String[] topic = sub.subscribe.split(":");
            if (topic[0].equals("orderbook"))
                SpredBotJob.refreshTimer(topic[1]);
            channels.get(sub.subscribe).add(conn);
            System.out.println("subscribe: " + sub.subscribe);
        } else if (sub.unsubscribe != null) {
            if (channels.containsKey(sub.unsubscribe)) {
                channels.get(sub.unsubscribe).remove(conn);
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
    }

    @Override
    public void onStart() {
        System.out.println("WS started on port: " + getPort());
        setConnectionLostTimeout(100);
    }

}
