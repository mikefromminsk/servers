package com.hatosh.exchange;

import java.util.List;

public class OrderBook extends ExchangeUtils{

    public static void fillAdditionalData(List<PriceLevel> levels) {
        double sum = 0;
        for (PriceLevel level : levels) {
            sum += level.amount;
        }
        double accumulateAmount = 0;
        for (PriceLevel level : levels) {
            accumulateAmount += level.amount;
            level.price = round(level.price);
            level.amount = round(level.amount);
            level.percent = round(accumulateAmount / sum * 100);
        }
    }

    @Override
    public void run() {
        String domain = getRequired("domain");
        List<PriceLevel> sellLevels = getPriceLevels(domain, true, 6);
        List<PriceLevel> buyLevels = getPriceLevels(domain, false, 6);

        fillAdditionalData(sellLevels);
        fillAdditionalData(buyLevels);

        response.put("sell", sellLevels);
        response.put("buy", buyLevels);
    }
}
