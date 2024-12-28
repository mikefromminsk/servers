package com.hatosh.exchange;

import com.hatosh.utils.InfiniteTimer;

import java.util.List;
import java.util.Random;

import static com.hatosh.wallet.Node.broadcast;
import static com.hatosh.wallet.data.Contract.GAS_DOMAIN;
import static com.hatosh.utils.Params.map;

public class Bot extends BotUtils {

    private String domain;
    private String botAddress;
    private final InfiniteTimer secTimer;
    private final InfiniteTimer minTimer;

    public Bot(String domain) {
        this.domain = domain;
        this.botAddress = BOT_PREFIX + domain;
        if (botScriptReg(domain, botAddress)) commitAccounts();
        secTimer = new InfiniteTimer(secBot, 1000, 1000 * 60 * 5);
        minTimer = new InfiniteTimer(secBot, 1000 * 60, 1000 * 60 * 60 * 24 * 7);
    }

    public void refreshTimers() {
        secTimer.restart();
        minTimer.restart();
    }

    InfiniteTimer.Callback secBot = (timer) -> {
        params.put("domain", domain);
        double volatility = timer.periodMs == 1000 ? 0.01 : 0.02;
        boolean isSell = random() % 2 == 0;
        double price = round(tokenPrice(domain) * (1 + (isSell ? -1 : 1) * volatility));
        double gasBalance = tokenBalance(GAS_DOMAIN, botAddress);
        double needPrice = round(gasBalance / 100);
        price = price + (needPrice - price) * 0.1;
        cancelAllAndCommit(domain, botAddress);
        fillOrderbook(domain, price, 3, 3, 7);
        commit();

        String topic = "orderbook:" + getRequired("domain");
        broadcast(topic, map("topic", topic));
    };

}
