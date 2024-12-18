package com.mfm_wallet.mfm_exchange;

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
            level.percent = accumulateAmount / sum * 100;
        }
    }

    @Override
    protected void run() {
        String domain = getRequired("domain");
        List<PriceLevel> sellLevels = getPriceLevels(domain, 1, 6);
        List<PriceLevel> buyLevels = getPriceLevels(domain, 0, 6);

        fillAdditionalData(sellLevels);
        fillAdditionalData(buyLevels);

        response.put("sell", sellLevels);
        response.put("buy", buyLevels);
    }
}
