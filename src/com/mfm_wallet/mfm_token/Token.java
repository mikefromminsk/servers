package com.mfm_wallet.mfm_token;

import com.mfm_wallet.Contract;
import com.mfm_wallet.model.Account;

public class Token extends Contract {
    @Override
    protected void run() {
        String domain = getRequired("domain");
        String address = getString("address");
        com.mfm_wallet.model.Token token = tokensByDomain.get(domain);
        if (token == null) error("Token not found");
        response.put("token", token);
        if (address != null) {
            Account account = getAccount(domain, address);
            if (account != null)
                response.put("account", account);
        }
    }
}
