package com.sockets.test;

import com.google.gson.Gson;
import com.mfm_wallet.mfm_exchange.SpredBotJob;
import com.sockets.test.model.Subscription;
import com.sockets.test.utils.SSLContextBuilder;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import javax.net.ssl.SSLContext;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.mfm_wallet.Utils.getPortOffset;


public class WssServer extends WebSocketServer {

    int connections = 0;
    static final Gson json = new Gson();
    public static final Map<String, HashSet<WebSocket>> channels = new HashMap<>();

    public WssServer(int port, String site_domain) {
        super(new InetSocketAddress(port + getPortOffset(site_domain)));
        SSLContext context = SSLContextBuilder.from(site_domain);
        if (context != null) {
            System.out.println("WSS context activated for " + site_domain + " on port " + port);
            setWebSocketFactory(new DefaultSSLWebSocketServerFactory(context));
        }
    }

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
