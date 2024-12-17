package com.mfm_wallet.mfm_analytics;

import com.mfm_wallet.Contract;

import java.util.Map;

public class Candles extends Contract {
    @Override
    protected void run() {
        String key = getRequired("key");
        String accomulate_key = getRequired("accomulate_key");
        String period_name = getRequired("period_name");

        response.put("candles", getCandles(key, period_name, 50));
        response.put("accomulate", getAccumulate(accomulate_key, period_name, 50));
        response.put("value", getCandleLastValue(key));
        response.put("change24", getCandleChange24(key));
    }
}
