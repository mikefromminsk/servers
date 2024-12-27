package com.hatosh.exchange;


import java.util.List;

import static com.hatosh.utils.Params.map;
import static java.lang.Math.max;

public class BotUtils extends ExchangeUtils {

    public static final String BOT_PREFIX = "bot_";

    void fillOrderbook(String domain, double tokenPrice, double rangePercent, double rangeGas, int orderCount) {
        double rangeStep = max(.01, round(tokenPrice * rangePercent / 100));

        double orderUsdtSell = sumOrders(domain, true, tokenPrice, rangePercent);
        double amountSell = round(rangeGas - orderUsdtSell);
        if (amountSell > 0) {
            double orderMinPrice = round(tokenPrice + rangeStep);
            double orderMaxPrice = round(orderMinPrice * (1 + rangeStep * orderCount));
            placeRange(domain,
                    orderMinPrice,
                    orderMaxPrice,
                    orderCount,
                    amountSell,
                    true);
        }

        double orderUsdtBuy = sumOrders(domain, false, tokenPrice, rangePercent);
        double amountBuy = round(rangeGas - orderUsdtBuy);
        if (amountBuy > 0) {
            double orderMaxPrice = round(tokenPrice - rangeStep);
            double orderMinPrice = round(orderMaxPrice * (1 - rangeStep * orderCount));
            placeRange(domain,
                    orderMinPrice,
                    orderMaxPrice,
                    orderCount,
                    amountBuy,
                    false);
        }
    }

    double sumOrders(String domain, boolean is_sell, double price, double percent) {
        double sumGas = 0;
        if (!is_sell) {
            List<PriceLevel> buyPriceLevels = getPriceLevels(domain, false, 20);
            for (PriceLevel level : buyPriceLevels) {
                if (level.price >= price * (1 - percent / 100)) {
                    sumGas += level.amount * level.price;
                }
            }
        } else {
            List<PriceLevel> sellPriceLevels = getPriceLevels(domain, true, 20);
            for (PriceLevel level : sellPriceLevels) {
                if (level.price <= price * (1 + percent / 100)) {
                    sumGas += level.amount * level.price;
                }
            }
        }
        return sumGas;
    }

    void placeAndCommit(String domain,
                        String address,
                        boolean isSell,
                        double price,
                        double amount,
                        double total,
                        String pass) {
        try {
            Place place = new Place();
            place.params = map(
                    "domain", domain,
                    "address", address,
                    "is_sell", "" + (isSell ? 1 : 0),
                    "price", "" + price,
                    "amount", "" + amount,
                    "total", "" + total,
                    "pass", pass);
            place.run();
            place.commit();
        } catch (Exception e) {
        }
    }

    public void placeRange(String domain,
                           double minPrice,
                           double maxPrice,
                           int count,
                           double amountUsdt,
                           boolean isSell) {
        if (minPrice <= 0) throw new RuntimeException("min_price less than 0");
        if (maxPrice <= 0) throw new RuntimeException("max_price less than 0");
        if (minPrice >= maxPrice) throw new RuntimeException("min_price is greater than max_price");
        if (count <= 0) throw new RuntimeException("count less than 0");
        if (amountUsdt <= 0) throw new RuntimeException("amount_usdt less than 0");
        String address = BOT_PREFIX + domain;

        if (amountUsdt < 0.01 * count) {
            double price = (isSell) ? minPrice : maxPrice;
            double amountBase = round(amountUsdt / price);
            if (amountBase > 0) {
                placeAndCommit(domain,
                        address,
                        isSell,
                        round(price),
                        round(amountBase),
                        round(price * amountBase),
                        tokenPass(domain, address));
            }
        } else {
            double price = minPrice;
            double priceStep = round((maxPrice - minPrice) / (count - 1));
            double amountStep = amountUsdt / count;
            double sumAmount = 0;

            for (int i = 0; i < count; i++) {
                price = round(price);
                double amountBase = round(amountStep / price);
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
                            round(price),
                            round(amountBase),
                            round(price * amountBase),
                            tokenPass(domain, address));
                }
                price += priceStep;
            }
        }
    }

    @Override
    public void run() {
    }
}
