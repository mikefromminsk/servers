package com.hatosh.exchange;

import com.hatosh.servers.model.Endpoint;
import com.hatosh.servers.model.Subscription;
import com.hatosh.wallet.Node;

import java.io.IOException;
import java.util.Map;

public class ExchangeServer extends Node {

    Map<String, Bot> bots = new java.util.HashMap<>();
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
            case "mfm-exchange/place":
                return new Place();
            case "mfm-exchange/cancel":
                return new Cancel();
            case "mfm-exchange/orderbook":
                return new OrderBook();
            case "mfm-exchange/orders":
                return new Orders();
        }
        return super.getEndpoint(uri);
    }

    @Override
    public void onMessage(Subscription sub) {
        super.onMessage(sub);
        String[] topic = sub.subscribe.split(":");
        if (topic[0].equals("orderbook"))
            bots.computeIfAbsent(topic[1], k -> new Bot(k)).refreshTimers();
    }


}
