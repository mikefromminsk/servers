package org.vavilon.exchange;

import static org.vavilon.exchange.BotUtils.BOT_PREFIX;
import static org.vavilon.utils.Params.map;
import static org.vavilon.wallet.Node.broadcast;

public class Place extends ExchangeUtils {
    @Override
    public void run() {
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

        if (address.indexOf(BOT_PREFIX) != 0) {
            String topic = "orderbook:" + getRequired("domain");
            broadcast(topic, map("topic", topic));
        }
    }
}
