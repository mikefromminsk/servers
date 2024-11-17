package com.sockets.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sockets.test.model.Message;
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
        Subscription subscription = json.fromJson(message, Subscription.class);
        if (subscription.subscribe != null) {
            if (!channels.containsKey(subscription.subscribe)) {
                channels.put(subscription.subscribe, new HashSet<>());
            }
            channels.get(subscription.subscribe).add(conn);
        }
        if (subscription.unsubscribe != null) {
            if (channels.containsKey(subscription.unsubscribe)) {
                channels.get(subscription.unsubscribe).remove(conn);
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
