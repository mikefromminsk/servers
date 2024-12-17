package com.mfm_wallet.mfm_exchange;

public class Spred extends ExchangeUtils{

    @Override
    protected void run() {
        String domain = getRequired("domain");

    }

    public void placeRange(String domain, double minPrice, double maxPrice, int count, double amountUsdt, int isSell, String address, String pass) {
        if (minPrice <= 0) throw new RuntimeException("min_price less than 0");
        if (maxPrice <= 0) throw new RuntimeException("max_price less than 0");
        if (minPrice >= maxPrice) throw new RuntimeException("min_price is greater than max_price");
        if (count <= 0) throw new RuntimeException("count less than 0");
        if (amountUsdt <= 0) throw new RuntimeException("amount_usdt less than 0");

        if (amountUsdt < 0.01 * count) {
            double price = (isSell == 1) ? minPrice : maxPrice;
            double amountBase = round(amountUsdt / price, 2);
            if (amountBase > 0) {
                placeAndCommit(domain, address, isSell, round(price, 2), round(amountBase, 2), round(price * amountBase, 2), pass);
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
                    placeAndCommit(domain, address, isSell, round(price, 2), round(amountBase, 2), round(price * amountBase, 2), pass);
                }
                price += priceStep;
            }
        }
    }


}
