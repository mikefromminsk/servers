package com.mfm_wallet.mfm_exchange;

public class Cancel extends ExchangeUtils{

    @Override
    protected void run() {
        Long order_id = getLongRequired("order_id");
        cancel(order_id);
    }
}
