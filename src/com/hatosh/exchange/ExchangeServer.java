package com.hatosh.exchange;

import com.hatosh.servers.FtpServer;
import com.hatosh.servers.model.Endpoint;
import com.hatosh.servers.model.Subscription;
import com.hatosh.telegram.TelegramRedirector;
import com.hatosh.wallet.Node;
import com.hatosh.wallet.token.model.Transaction;

import java.io.IOException;
import java.util.Map;

public class ExchangeServer extends Node {

    Map<String, Spred> bots = new java.util.HashMap<>();
    public ExchangeServer(String domain, String masterNode) {
        super(domain, masterNode);
    }

    @Override
    public void start() throws IOException {
        super.start();
    }

    @Override
    public Endpoint getEndpoint(String uri) {
        switch (uri) {
            case "mfm-exchange/place.php":
                return new Place();
            case "mfm-exchange/cancel.php":
                return new Cancel();
            case "mfm-exchange/orderbook.php":
                return new OrderBook();
            case "mfm-exchange/orders.php":
                return new Orders();
        }
        return super.getEndpoint(uri);
    }

    public static void onTranSuccess(Transaction tran) {
        // place if prefix is == site_domain
    }

    @Override
    public void onMessage(Subscription sub) {
        super.onMessage(sub);
        String[] topic = sub.subscribe.split(":");
        if (topic[0].equals("orderbook"))
            bots.computeIfAbsent(topic[1], k -> new Spred(k)).refreshTimers();
    }


}
