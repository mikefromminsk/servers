package com.mfm_wallet.mfm_wallet;

import com.mfm_wallet.Contract;
import com.mfm_wallet.model.Token;

import java.util.*;

public class Main extends Contract {
    @Override
    protected void run() {
        Map<String, Set<String>> tops = new LinkedHashMap<>();
        for (Token token : topGainers)
            tops.computeIfAbsent("top_gainers", k -> new HashSet<>()).add(token.domain);
        for (Token token : topExchange)
            tops.computeIfAbsent("top_exchange", k -> new HashSet<>()).add(token.domain);
        response.put("tops", tops);
    }
}
