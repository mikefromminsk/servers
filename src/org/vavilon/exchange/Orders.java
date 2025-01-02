package org.vavilon.exchange;

public class Orders extends ExchangeUtils{
    @Override
    public void run() {
        String domain = getRequired("domain");
        String address = getRequired("address");

        response.put("active", ordersActive(domain, address));
        response.put("history", ordersHistory(domain, address));
    }
}
