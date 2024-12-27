package com.hatosh.exchange;

import com.hatosh.utils.InfiniteTimer;

import java.util.List;
import java.util.Random;

import static com.hatosh.wallet.Node.broadcast;
import static com.hatosh.wallet.data.Contract.GAS_DOMAIN;
import static com.hatosh.utils.Params.map;

public class Bot extends BotUtils implements InfiniteTimer.Callback {

    private final String domain;
    private final InfiniteTimer secTimer;
    private final InfiniteTimer minTimer;
    private final InfiniteTimer dayTimer;

    public Bot(String domain) {
        this.domain = domain;
        secTimer = new InfiniteTimer(this, 1000, 1000 * 2);
        minTimer = new InfiniteTimer(this, 1000 * 60, 1000 * 60 * 60 * 24);
        dayTimer = new InfiniteTimer(this, 1000 * 60 * 60 * 24, 1000 * 60 * 60 * 24 * 7);
    }

    public void refreshTimers() {
        secTimer.restart();
        minTimer.restart();
        dayTimer.restart();
    }

    @Override
    public void onTimer() {
        String botAddress = BOT_PREFIX + domain;

        if (botScriptReg(domain, botAddress)) commitAccounts();

        double coinBalance = tokenBalance(domain, botAddress);
        double gasBalance = tokenBalance(GAS_DOMAIN, botAddress);

        if (coinBalance < 5 || gasBalance < 5) {
            cancelAll(domain, botAddress);
            error("cancel all");
        }

        List<PriceLevel> sellPriceLevels = getPriceLevels(domain, true, 20);
        List<PriceLevel> buyPriceLevels = getPriceLevels(domain, false, 20);

        if (!sellPriceLevels.isEmpty() && !buyPriceLevels.isEmpty()) {
            boolean isSell = new Random().nextInt((int) (coinBalance * 100 + gasBalance * 100)) <= coinBalance * 100;
            double price = round(tokenPrice(domain) * (isSell ? 0.97 : 1.03));
            double amount = round(1 / price);
            placeAndCommit(domain,
                    botAddress,
                    isSell,
                    price,
                    amount,
                    amount * price,
                    isSell ? tokenPass(domain, botAddress) : tokenPass(GAS_DOMAIN, botAddress));
        }

        // calc token need price
        double bestSellPrice = !sellPriceLevels.isEmpty() ? sellPriceLevels.get(0).price : 0;
        double bestBuyPrice = !buyPriceLevels.isEmpty() ? buyPriceLevels.get(0).price : 0;
        double tokenPrice = bestSellPrice == 0 || bestBuyPrice == 0 ? Math.max(bestSellPrice, bestBuyPrice) : (bestSellPrice + bestBuyPrice) / 2;
        tokenPrice = tokenPrice == 0 ? 1 : tokenPrice;

        fillOrderbook(domain, tokenPrice, 3, 3, 6);
        fillOrderbook(domain, tokenPrice, 300, 50, 5);

    }


}
