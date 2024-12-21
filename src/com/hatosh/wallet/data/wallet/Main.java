package com.hatosh.wallet.data.wallet;

import com.hatosh.servers.model.Endpoint;
import com.hatosh.wallet.token.TokenUtils;
import com.hatosh.wallet.token.model.Token;

import java.util.*;

public class Main extends TokenUtils {
    @Override
    public void run() {
        Map<String, Set<String>> tops = new LinkedHashMap<>();
        for (Token token : topGainers)
            tops.computeIfAbsent("top_gainers", k -> new HashSet<>()).add(token.domain);
        for (Token token : topExchange)
            tops.computeIfAbsent("top_exchange", k -> new HashSet<>()).add(token.domain);
        response.put("tops", tops);
    }
}
