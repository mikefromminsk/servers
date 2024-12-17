package com.mfm_wallet.mfm_token;


import java.util.LinkedHashMap;
import java.util.Map;

public class TokenAnalytics extends Token {
    @Override
    protected void run() {
        super.run(params);
        String domain = getRequired("domain");
        response.put("owner", getAccount(domain, "owner"));

        Map<String, Float> analytics = new LinkedHashMap<>();
        analytics.put("trans", getCandleLastValue(domain + "_trans"));
        analytics.put("accounts", getCandleLastValue(domain + "_accounts"));
        analytics.put("trans_count", getCandleLastValue("trans_count"));
        analytics.put("accounts_count", getCandleLastValue("accounts_count"));
        analytics.put("tokens_count", getCandleLastValue("tokens_count"));
        response.put("analytics", analytics);
    }
}
