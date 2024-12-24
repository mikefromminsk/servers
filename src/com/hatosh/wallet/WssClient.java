package com.hatosh.wallet;

import com.hatosh.servers.model.Message;
import com.hatosh.servers.model.Subscription;
import com.hatosh.wallet.token.Send;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import static com.hatosh.wallet.Utils.gson;

public class WssClient extends WebSocketClient {

    public WssClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        send(gson.toJson(new Subscription("transactions")));
    }


    @Override
    public void onMessage(String messageStr) {
        Message message = gson.fromJson(messageStr, Message.class);
        if (message.channel.equals("transactions")) {
            try {
                Send send = new Send();
                send.run(null, message.data);
                send.commit();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed connection");
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
