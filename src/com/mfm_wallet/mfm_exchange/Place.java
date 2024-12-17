package com.mfm_wallet.mfm_exchange;

public class Place extends ExchangeUtils {
    @Override
    protected void run() {
        String domain = getRequired("domain");
        Long is_sell = getLongRequired("is_sell");
        String address = getRequired("address");
        Double price = getDoubleRequired("price");
        Double amount = getDoubleRequired("amount");
        Double total = getDoubleRequired("total");
        String pass = getRequired("pass");

        /*if (!str_starts_with(address, "bot_spred_"))
            limitPassSec(1, domain);*/

        place(domain, address, is_sell, price, amount, total, pass);

        /*if (strpos(address, "bot_") == = FALSE) {
            topic = orderbook.":".domain;
            broadcast(topic,[ topic = > topic, ]);
        }*/
    }
}
