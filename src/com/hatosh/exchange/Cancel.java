package com.hatosh.exchange;

public class Cancel extends ExchangeUtils{

    @Override
    public void run() {
        Long order_id = getLongRequired("order_id");
        cancel(order_id);
    }
}
