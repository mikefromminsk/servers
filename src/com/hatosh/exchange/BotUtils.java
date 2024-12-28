package com.hatosh.exchange;


import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BotUtils extends ExchangeUtils {

    public static final String BOT_PREFIX = "bot_";

    @Override
    public double tokenPrice(String domain) {
        double price = super.tokenPrice(domain);
        return price == 0 ? 1 : price;
    }

    void fillOrderbook(String domain, double tokenPrice, double rangePercent, double gasAmount, int orderCount) {
        if (tokenBalance(domain, BOT_PREFIX + domain) < gasAmount) {
            cancelAll(domain, BOT_PREFIX + domain);
            commit();
        }

        placeRange(domain, tokenPrice, rangePercent, gasAmount, orderCount, true);
        placeRange(domain, tokenPrice, rangePercent, gasAmount, orderCount, false);
    }

    public List<Double> steps(double price, double percent, int count) {
        List<Double> result = new ArrayList<>();
        double stepDiff = price * percent / 100 / count;
        stepDiff = percent > 0 ? max(0.01, stepDiff) : min(-0.01, stepDiff);
        if (stepDiff == 0) {
            System.out.println();
        }
        for (int i = 0; i < count; i++) {
            double step = round(price + stepDiff * (i /*+ 1*/));
            if (step < 0.01) step = 0.01;
            result.add(step);
        }
        return result;
    }

    public void placeRange(String domain, double price, double rangePercent, double gasAmount, int orderCount, boolean isSell) {
        for (Double stepPrice : steps(price, (isSell ? 1 : -1) * rangePercent, orderCount)) {
            double stepAmount = round(gasAmount / orderCount / stepPrice);
            double stepTotal = round(stepPrice * stepAmount);
            placeAndCommit(domain,
                    BOT_PREFIX + domain,
                    isSell,
                    stepPrice,
                    stepAmount,
                    stepTotal,
                    tokenPass(domain, BOT_PREFIX + domain));
        }
    }

    @Override
    public void run() {
    }
}
