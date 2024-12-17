package com.mfm_wallet.mfm_exchange;

import java.util.List;
import java.util.Random;

import static com.mfm_wallet.mfm_data.DataContract.GAS_DOMAIN;
import static com.sockets.test.utils.Params.map;

public class Spred extends ExchangeUtils {

    void placeAndCommit(String domain,
                        String address,
                        int isSell,
                        double price,
                        double amount,
                        double total,
                        String pass) {
        try {
            Place place = new Place();
            place.params = map(
                    "domain", domain,
                    "address", address,
                    "is_sell", "" + isSell,
                    "price", "" + price,
                    "amount", "" + amount,
                    "total", "" + total,
                    "pass", pass);
            place.run();
            place.commit();
        } catch (Exception e) {
        }
    }

    @Override
    protected void run() {
        String domain = getRequired("domain");
        String botAddress = "bot_" + getScriptName() + "_" + domain;

        if (botScriptReg(domain, botAddress)) commitAccounts();

        double coinBalance = tokenBalance(domain, botAddress);
        double gasBalance = tokenBalance(GAS_DOMAIN, botAddress);

        if (coinBalance < 5 || gasBalance < 5) {
            cancelAll(domain, botAddress);
            error("cancel all");
        }

        List<PriceLevel> sellPriceLevels = getPriceLevels(domain, 1, 20);
        List<PriceLevel> buyPriceLevels = getPriceLevels(domain, 0, 20);

        if (!sellPriceLevels.isEmpty() && !buyPriceLevels.isEmpty()) {
            boolean isSell = new Random().nextInt((int) (coinBalance * 100 + gasBalance * 100)) <= coinBalance * 100;
            double price = round(tokenPrice(domain) * (isSell ? 0.97 : 1.03), 2);
            double amount = round(1 / price, 2);
            placeAndCommit(domain,
                    botAddress,
                    isSell ? 1 : 0,
                    price,
                    amount,
                    amount * price,
                    isSell ? tokenPass(domain, botAddress) : tokenPass(GAS_DOMAIN, botAddress));
        }

        double bestSellPrice = !sellPriceLevels.isEmpty() ? sellPriceLevels.get(0).price : 0;
        double bestBuyPrice = !buyPriceLevels.isEmpty() ? buyPriceLevels.get(0).price : 0;
        double tokenPrice = bestSellPrice == 0 || bestBuyPrice == 0 ? Math.max(bestSellPrice, bestBuyPrice) : (bestSellPrice + bestBuyPrice) / 2;
        tokenPrice = tokenPrice == 0 ? 1 : tokenPrice;

        double orderUsdtBuy = 0;
        for (PriceLevel level : buyPriceLevels) {
            if (level.price >= tokenPrice * 0.97) {
                orderUsdtBuy += level.amount * level.price;
            }
        }

        double quoteNeed = 3;
        double amountBuy = round(quoteNeed - orderUsdtBuy, 2);
        int orderCount = 6;
        if (amountBuy > 0) {
            double orderMaxPrice = round(tokenPrice - 0.01, 2);
            double orderMinPrice = round(orderMaxPrice * (1 - 0.01 * orderCount), 2);
            placeRange(domain,
                    orderMinPrice,
                    orderMaxPrice,
                    orderCount,
                    amountBuy,
                    0,
                    botAddress,
                    tokenPass(domain, botAddress));
        }

        double orderUsdtSell = 0;
        for (PriceLevel level : sellPriceLevels) {
            if (level.price <= tokenPrice * 1.03) {
                orderUsdtSell += level.amount * level.price;
            }
        }

        double amountSell = round(quoteNeed - orderUsdtSell, 2);
        if (amountSell > 0) {
            double orderMinPrice = round(tokenPrice + 0.01, 2);
            double orderMaxPrice = round(orderMinPrice * (1 + 0.01 * orderCount), 2);
            placeRange(domain,
                    orderMinPrice,
                    orderMaxPrice,
                    orderCount,
                    amountSell,
                    1,
                    botAddress,
                    tokenPass(domain, botAddress));
        }

        //String topic = "orderbook:" + domain;
        //broadcast(topic, Map.of("topic", topic));
    }

    public void placeRange(String domain,
                           double minPrice,
                           double maxPrice,
                           int count,
                           double amountUsdt,
                           int isSell,
                           String address,
                           String pass) {
        if (minPrice <= 0) throw new RuntimeException("min_price less than 0");
        if (maxPrice <= 0) throw new RuntimeException("max_price less than 0");
        if (minPrice >= maxPrice) throw new RuntimeException("min_price is greater than max_price");
        if (count <= 0) throw new RuntimeException("count less than 0");
        if (amountUsdt <= 0) throw new RuntimeException("amount_usdt less than 0");

        if (amountUsdt < 0.01 * count) {
            double price = (isSell == 1) ? minPrice : maxPrice;
            double amountBase = round(amountUsdt / price, 2);
            if (amountBase > 0) {
                placeAndCommit(domain,
                        address,
                        isSell,
                        round(price, 2),
                        round(amountBase, 2),
                        round(price * amountBase, 2),
                        pass);
            }
        } else {
            double price = minPrice;
            double priceStep = round((maxPrice - minPrice) / (count - 1), 2);
            double amountStep = amountUsdt / count;
            double sumAmount = 0;

            for (int i = 0; i < count; i++) {
                price = round(price, 2);
                double amountBase = round(amountStep / price, 2);
                sumAmount += (price * amountBase);
                if (i == count - 1 && sumAmount < amountUsdt) {
                    while (sumAmount < amountUsdt) {
                        amountBase += 0.01;
                        sumAmount += price * 0.01;
                    }
                }
                if (amountBase > 0) {
                    placeAndCommit(domain,
                            address,
                            isSell,
                            round(price, 2),
                            round(amountBase, 2),
                            round(price * amountBase, 2),
                            pass);
                }
                price += priceStep;
            }
        }
    }


}
