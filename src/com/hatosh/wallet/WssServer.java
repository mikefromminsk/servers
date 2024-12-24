package com.hatosh.wallet;

import com.google.gson.Gson;
import com.hatosh.servers.model.Message;
import com.hatosh.servers.model.Subscription;
import com.hatosh.utils.Request;
import com.hatosh.utils.SSLContextBuilder;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import javax.net.ssl.SSLContext;

import java.net.InetSocketAddress;
import java.util.*;

import static com.hatosh.wallet.Utils.getPortOffset;
import static com.hatosh.wallet.Utils.gson;


public class WssServer extends WebSocketServer {
    static final int WSS_START_RANGE = 8800;
    private final Callback callback;

    int connections = 0;
    static final Gson json = new Gson();
    public static final Map<String, HashSet<WebSocket>> channels = new HashMap<>();

    public WssServer(String site_domain, Callback callback) {
        super(new InetSocketAddress(WSS_START_RANGE + getPortOffset(site_domain)));
        this.callback = callback;
        SSLContext context = SSLContextBuilder.from(site_domain);
        if (context != null) {
            System.out.println("WSS context activated for " + site_domain + " on port " + getPort());
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
            callback.onMessage(sub);
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
        System.out.println(this.getClass().getSimpleName() + " started on " + getPort());
        setConnectionLostTimeout(100);
    }

    public void broadcast(String channel, Map<String, String> data) {
        if (channels.containsKey(channel)) {
            Iterator<WebSocket> iterator = channels.get(channel).iterator();
            Set<WebSocket> activeSubscribers = new HashSet<>();
            while (iterator.hasNext()) {
                WebSocket conn = iterator.next();
                if (conn.isOpen()) {
                    activeSubscribers.add(conn);
                } else {
                    iterator.remove();
                }
            }
            super.broadcast(gson.toJson(new Message(channel, data)), activeSubscribers);
        }
    }

    public interface Callback {
        void onMessage(Subscription sub);
    }

}
