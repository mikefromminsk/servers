package com.hatosh.wallet.token;

import com.hatosh.servers.model.Endpoint;
import com.hatosh.wallet.token.model.Account;

public class Token extends TokenUtils {
    @Override
    public void run() {
        String domain = getRequired("domain");
        String address = getString("address");
        com.hatosh.wallet.token.model.Token token = tokensByDomain.get(domain);
        if (token == null) error("Token not found");
        response.put("token", token);
        if (address != null) {
            Account account = getAccount(domain, address);
            if (account != null)
                response.put("account", account);
        }
    }
}
